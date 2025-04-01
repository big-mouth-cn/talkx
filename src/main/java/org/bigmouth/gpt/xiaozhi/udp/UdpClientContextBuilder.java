package org.bigmouth.gpt.xiaozhi.udp;

import com.bxm.warcar.cache.Counter;
import com.bxm.warcar.integration.eventbus.EventPark;
import com.bxm.warcar.utils.KeyBuilder;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.entity.Device;
import org.bigmouth.gpt.entity.Session;
import org.bigmouth.gpt.entity.UserFriend;
import org.bigmouth.gpt.entity.UserFriendMediaConfig;
import org.bigmouth.gpt.entity.response.AudioConfigVo;
import org.bigmouth.gpt.service.IDeviceService;
import org.bigmouth.gpt.service.ISessionService;
import org.bigmouth.gpt.service.IUserFriendMediaConfigService;
import org.bigmouth.gpt.service.IUserFriendService;
import org.bigmouth.gpt.utils.BatchBlockingQueue;
import org.bigmouth.gpt.utils.BatchQueue;
import org.bigmouth.gpt.utils.RedisBatchBlockingQueue;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiSileroConfig;
import org.bigmouth.gpt.xiaozhi.entity.UdpHello;
import org.bigmouth.gpt.xiaozhi.tts.TtsPlatformType;
import org.bigmouth.gpt.xiaozhi.tts.TtsService;
import org.bigmouth.gpt.xiaozhi.tts.TtsServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Allen Hu
 * @date 2025/3/5
 */
@Slf4j
@Configuration
public class UdpClientContextBuilder {

    private final EventPark eventPark;
    private final UdpClientContextHolder udpClientContextHolder;
    private final XiaozhiSileroConfig xiaozhiSileroConfig;
    private final IDeviceService deviceService;
    private final IUserFriendMediaConfigService userFriendMediaConfigService;
    private final TtsServiceFactory ttsServiceFactory;
    private final ISessionService sessionService;
    private final IUserFriendService userFriendService;
    private final Counter counter;
    private final ApplicationConfig applicationConfig;

    private JedisPool jedisPool;

    public UdpClientContextBuilder(EventPark eventPark, UdpClientContextHolder udpClientContextHolder, XiaozhiSileroConfig xiaozhiSileroConfig, IDeviceService deviceService, IUserFriendMediaConfigService userFriendMediaConfigService, TtsServiceFactory ttsServiceFactory, ISessionService sessionService, IUserFriendService userFriendService, Counter counter, ApplicationConfig applicationConfig) {
        this.eventPark = eventPark;
        this.udpClientContextHolder = udpClientContextHolder;
        this.xiaozhiSileroConfig = xiaozhiSileroConfig;
        this.deviceService = deviceService;
        this.userFriendMediaConfigService = userFriendMediaConfigService;
        this.ttsServiceFactory = ttsServiceFactory;
        this.sessionService = sessionService;
        this.userFriendService = userFriendService;
        this.counter = counter;
        this.applicationConfig = applicationConfig;
    }

    @Autowired(required = false)
    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public UdpClientContext createIfAbsent(UdpHello udpHello, @Nullable Consumer<String> ifAbsentHandler) {
        String sessionId = udpHello.getSessionId();
        return udpClientContextHolder.computeIfAbsent(sessionId,
                s -> {
                    log.info("[{}] - Started new UdpClientContext.", sessionId);
                    AudioVadListeningConsumer audioBufferConsumer = new AudioVadListeningConsumer(xiaozhiSileroConfig, eventPark, udpHello, udpClientContextHolder);

                    BatchQueue<byte[]> batchQueue;
                    if (applicationConfig.isEnableRedisCache()) {
                        String key = KeyBuilder.build("talkx", "queue", "audio", sessionId);

                        Function<byte[], String> serializer = Hex::encodeHexString;
                        Function<String, byte[]> deserializer = string -> {
                            try {
                                return Hex.decodeHex(string);
                            } catch (DecoderException e) {
                                log.error("Hex decode error", e);
                                return new byte[0];
                            }
                        };
                        batchQueue = new RedisBatchBlockingQueue<>(jedisPool, key, 10, audioBufferConsumer, 100, serializer, deserializer);
                    } else {
                        batchQueue = new BatchBlockingQueue<>(10, audioBufferConsumer, 100);
                    }

                    UdpClientContext.Builder builder = new UdpClientContext.Builder(udpHello)
                            .eventPark(eventPark)
                            .encoderUtils()
                            .decoderUtils()
                            .audioBufferQueue(batchQueue)
                            .sileroVadListener(xiaozhiSileroConfig)
                            .audioResponseSequence(new AudioResponseSequence(counter, sessionId));

                    String clientId = udpHello.getRequest().getClientId();
                    Device device = deviceService.findByClientId(clientId);
                    if (Objects.nonNull(device)) {
                        // 创建 Session 话题
                        long userId = device.getUserId();
                        long friendId = device.getFriendId();
                        String roleType = device.getRoleType();
                        int productId = 0;
                        Session session = sessionService.createKeepSessionIfNecessary(sessionId, userId, friendId, productId, Duration.ofHours(8));
                        builder.session(session);

                        UserFriend userFriend = userFriendService.getById(device.getUserFriendId());
                        builder.userFriend(userFriend);

                        // 创建 TTS 服务
                        TtsPlatformType platformType = TtsPlatformType.TalkX;
                        String model = null, voice = null;
                        UserFriendMediaConfig friendMediaConfig = userFriendMediaConfigService.getByUserFriendId(device.getUserFriendId());
                        if (null != friendMediaConfig) {
                            platformType = TtsPlatformType.of(friendMediaConfig.getAudioPlatformType(), platformType);
                            model = friendMediaConfig.getAudioModel();
                            voice = friendMediaConfig.getAudioRole();

                            builder.userFriendMediaConfig(friendMediaConfig);
                        } else {
                            AudioConfigVo defConfig = userFriendMediaConfigService.getDefConfig(roleType);
                            if (null != defConfig) {
                                platformType = TtsPlatformType.of(defConfig.getPlatformType().name());
                                model = defConfig.getModel();
                                voice = defConfig.getRole();
                            }
                        }

                        TtsService ttsService = ttsServiceFactory.createInstance(sessionId, platformType, model, voice);
                        ttsService.init();
                        builder.ttsService(ttsService);
                    }

                    if (null != ifAbsentHandler) {
                        ifAbsentHandler.accept(sessionId);
                    }
                    return builder.build();
                });
    }
}
