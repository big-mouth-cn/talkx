package org.bigmouth.gpt.xiaozhi.event.listener;

import com.bxm.warcar.integration.eventbus.EventListener;
import com.bxm.warcar.integration.eventbus.EventPark;
import com.bxm.warcar.integration.eventbus.core.Subscribe;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.xiaozhi.asr.AsrServiceFactory;
import org.bigmouth.gpt.xiaozhi.audio.PcmToWavUtil;
import org.bigmouth.gpt.xiaozhi.entity.DataPacket;
import org.bigmouth.gpt.xiaozhi.entity.MessageType;
import org.bigmouth.gpt.xiaozhi.entity.UdpHello;
import org.bigmouth.gpt.xiaozhi.event.P2pMessageEvent;
import org.bigmouth.gpt.xiaozhi.event.Speech2TextSuccessEvent;
import org.bigmouth.gpt.xiaozhi.event.VadEndEvent;
import org.bigmouth.gpt.xiaozhi.handler.ListenMessageHolder;
import org.bigmouth.gpt.xiaozhi.udp.UdpClientContext;
import org.springframework.context.annotation.Configuration;

/**
 * 语音转文本
 *
 * @author Allen Hu
 * @date 2025/2/25
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class VadEndSpeech2TextEventListener implements EventListener<VadEndEvent> {

    private final AsrServiceFactory asrServiceFactory;
    private final ListenMessageHolder listenMessageHolder;
    private final EventPark eventPark;

    @Override
    @Subscribe
    public void consume(VadEndEvent event) {
        String sessionId = event.getSessionId();
        UdpClientContext context = event.getContext();
        UdpHello udpHello = context.getUdpHello();
        try {
            byte[] pcm = context.getPcmAudioBytes();
            log.info("[{}] - Start transcription.", sessionId);
            byte[] wav = PcmToWavUtil.pcmToWav(pcm);
            String text = asrServiceFactory.get().asr(System.currentTimeMillis() + ".wav", wav);

            // 如果识别为空，并且是手动模式，那认为用户不想说话，就不要强行走后续的chat流程了。
            if (StringUtils.isBlank(text)) {
                String listenMode = listenMessageHolder.getListenMode(sessionId);
                if (DataPacket.MODE_MANUAL.equals(listenMode)) {
                    log.info("[{}] - No speak any content", sessionId);
                    return;
                }
            }
            log.info("[{}] - Say: {}", sessionId, text);

            // 发送 STT 数据
            DataPacket stt = DataPacket.builder().type(MessageType.STT.getValue()).text(text).sessionId(sessionId).build();
            eventPark.post(new P2pMessageEvent(this, context, stt));

            // 发送 STT 事件
            eventPark.post(new Speech2TextSuccessEvent(this, context, udpHello, text));
        } catch (Exception e) {
            log.error("[{}] - Voice transcription failed.", sessionId, e);

            // 发送 GOODBYE 请求
            DataPacket goodbye = DataPacket.builder().type(MessageType.GOODBYE.getValue()).text("-102").sessionId(sessionId).build();
            eventPark.post(new P2pMessageEvent(this, context, goodbye));
        }
    }
}
