package org.bigmouth.gpt.xiaozhi.udp;


import com.bxm.warcar.cache.Fetcher;
import com.bxm.warcar.cache.KeyGenerator;
import com.bxm.warcar.cache.Updater;
import com.bxm.warcar.integration.eventbus.EventPark;
import com.bxm.warcar.utils.KeyBuilder;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.bigmouth.gpt.entity.Session;
import org.bigmouth.gpt.service.ISessionService;
import org.bigmouth.gpt.xiaozhi.entity.DataPacket;
import org.bigmouth.gpt.xiaozhi.entity.MessageType;
import org.bigmouth.gpt.xiaozhi.event.P2pMessageEvent;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 *
 *
 * @author Allen Hu
 * @date 2025/2/25
 */
@Slf4j
@Configuration
public class UdpClientContextHolder implements DisposableBean {

    private final ConcurrentMap<String, UdpClientContext> clientContextConcurrentMap = new ConcurrentHashMap<>();
    private final Fetcher fetcher;
    private final Updater updater;
    private final ISessionService sessionService;
    private final ScheduledThreadPoolExecutor nonLocalGoodbyeScheduled = new ScheduledThreadPoolExecutor(1);
    private final ScheduledThreadPoolExecutor listenTimeOutScheduled = new ScheduledThreadPoolExecutor(1);
    private final LoadingCache<String, Optional<Integer>> goodbyeCache = CacheBuilder.newBuilder()
            .initialCapacity(200)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Optional<Integer>>() {
                @Override
                public Optional<Integer> load(String key) {
                    return Optional.empty();
                }
            });

    public UdpClientContextHolder(Fetcher fetcher, Updater updater, ISessionService sessionService, EventPark eventPark) {
        this.fetcher = fetcher;
        this.updater = updater;
        this.sessionService = sessionService;
        this.nonLocalGoodbyeScheduled.scheduleWithFixedDelay(() -> {
            Map<String, Integer> goodbye = fetchGoodbye();
            if (MapUtils.isEmpty(goodbye)) {
                return;
            }
            goodbye.forEach((sessionId, value) -> {
                boolean exists = destroyContextIfExists(sessionId);
                if (exists) {
                    removeGoodbye(sessionId);
                }
            });
        },0, 10, TimeUnit.MINUTES);

        this.listenTimeOutScheduled.scheduleWithFixedDelay(new Runnable() {

            private boolean is1(UdpClientContext context) {
                // 如果收到了音频时间，且超过10秒，且没有STT时间，说明用户按了开始但没有说话
                LocalDateTime lastTimeOnReceiveAudio = context.getLastTimeOnReceiveAudio();
                LocalDateTime lastTimeOnStt = context.getLastTimeOnStt();
                return lastTimeOnReceiveAudio != null
                        && lastTimeOnReceiveAudio.plusSeconds(10).isBefore(LocalDateTime.now())
                        && lastTimeOnStt == null;
            }

            private boolean is2(UdpClientContext context) {
                // 如果TTS|STOP时间超过10秒，并且音频时间早于TTS|STOP时间，说明用户没有继续按下对话
                LocalDateTime lastTimeOnTtsStop = context.getLastTimeOnTtsStop();
                LocalDateTime lastTimeOnReceiveAudio = context.getLastTimeOnReceiveAudio();
                return lastTimeOnReceiveAudio != null
                        && lastTimeOnTtsStop != null
                        && lastTimeOnReceiveAudio.isBefore(lastTimeOnTtsStop)
                        && lastTimeOnTtsStop.plusSeconds(10).isBefore(LocalDateTime.now());
            }

            @Override
            public void run() {
                for (Map.Entry<String, UdpClientContext> entry : clientContextConcurrentMap.entrySet()) {
                    try {
                        UdpClientContext context = entry.getValue();
                        if (is1(context) || is2(context)) {
                            // 主动设置状态为 Goodbye
                            String sessionId = context.getSessionId();
                            DataPacket dataPacket = DataPacket.builder().type(MessageType.GOODBYE.getValue()).sessionId(sessionId).build();
                            eventPark.post(new P2pMessageEvent(this, context, dataPacket));
                            log.info("[{}] Listen manual timeout, Say goodbye!", sessionId);
                            goodbye(sessionId);
                        }
                    } catch (Exception e) {
                        log.error("listen time process error: ", e);
                    }
                }
            }

        }, 0, 10, TimeUnit.SECONDS);
    }

    public UdpClientContext get(String sessionId) {
        return clientContextConcurrentMap.get(sessionId);
    }

    public UdpClientContext computeIfAbsent(String sessionId, Function<String, UdpClientContext> supplier) {
        return clientContextConcurrentMap.computeIfAbsent(sessionId, supplier);
    }

    public void goodbye(String sessionId) {
        this.goodbyeCache.cleanUp();
        this.goodbyeCache.put(sessionId, Optional.of(1));
        // 这样做，是因为在集群环境下，goodbye的消息只会被任意一个节点消费到，
        // 所以需要把goodbye消息放入redis的list中，让所有节点都能收到goodbye消息
        boolean exists = this.destroyContextIfExists(sessionId);
        if (!exists) {
            this.putGoodbye(sessionId);
        }
    }

    public boolean isGoodbye(String sessionId) {
        try {
            Optional<Integer> exists = this.goodbyeCache.get(sessionId);
            return exists.isPresent();
        } catch (ExecutionException ignored) {
            return false;
        }
    }

    private boolean destroyContextIfExists(String sessionId) {
        UdpClientContext context = clientContextConcurrentMap.get(sessionId);
        if (Objects.nonNull(context)) {
            context.destroy();
            Session session = context.getSession();
            if (null != session) {
                sessionService.removeKeepSession(session.getUserId(), session.getFriendId(), session.getProductId());
            }
            clientContextConcurrentMap.remove(sessionId);
            return true;
        }
        return false;
    }

    private void putGoodbye(String sessionId) {
        updater.hupdate(hashGoodbyeKeyGenerator(), sessionId, 0);
    }

    private Map<String, Integer> fetchGoodbye() {
        return fetcher.hfetchall(hashGoodbyeKeyGenerator(), Integer.class);
    }

    private void removeGoodbye(String sessionId) {
        updater.hremove(hashGoodbyeKeyGenerator(), sessionId);
    }

    @Override
    public void destroy() throws Exception {
        nonLocalGoodbyeScheduled.shutdownNow();
        listenTimeOutScheduled.shutdownNow();
    }

    private KeyGenerator hashGoodbyeKeyGenerator() {
        // key = sessionId
        // value = 0 表示没有销毁、1 表示已销毁
        return () -> KeyBuilder.build("talkx", "goodble_session");
    }
}
