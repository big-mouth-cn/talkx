package org.bigmouth.gpt.xiaozhi.handler;

import com.bxm.warcar.integration.eventbus.EventPark;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.xiaozhi.entity.DataPacket;
import org.bigmouth.gpt.xiaozhi.entity.MessageType;
import org.bigmouth.gpt.xiaozhi.entity.UdpHello;
import org.bigmouth.gpt.xiaozhi.event.ListenStartEvent;
import org.bigmouth.gpt.xiaozhi.event.P2pMessageEvent;
import org.bigmouth.gpt.xiaozhi.event.Speech2TextSuccessEvent;
import org.bigmouth.gpt.xiaozhi.event.VadEndEvent;
import org.bigmouth.gpt.xiaozhi.udp.NettyUDPServer;
import org.bigmouth.gpt.xiaozhi.udp.UdpClientContext;
import org.bigmouth.gpt.xiaozhi.udp.UdpClientContextBuilder;
import org.bigmouth.gpt.xiaozhi.udp.UdpClientContextHolder;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * @author Allen Hu
 * @date 2025/2/21
 */
@Configuration
@AllArgsConstructor
public class ListenMessageHandler implements MessageHandler {

    private final UdpClientContextHolder udpClientContextHolder;
    private final EventPark eventPark;
    private final HelloMessageHandler helloMessageHandler;
    private final UdpClientContextBuilder udpClientContextBuilder;
    private final NettyUDPServer nettyUDPServer;
    private final ListenMessageHolder listenMessageHolder;

    @Override
    public MessageType onType() {
        return MessageType.LISTEN;
    }

    @Override
    public DataPacket handle(DataPacket dataPacket) {
        String sessionId = dataPacket.getSessionId();

        // Save Listen DataPacket
        listenMessageHolder.saveListenDataPacket(sessionId, dataPacket);
        String mode = dataPacket.getMode();
        if (StringUtils.isNotBlank(mode)) {
            listenMessageHolder.saveListenMode(sessionId, mode);
        }

        UdpHello udpHello = helloMessageHandler.getUdpHelloBySessionId(sessionId);

        if (dataPacket.isStateStart()) {
            // 客户端开始监听说话，并将发送声音。
            eventPark.post(new ListenStartEvent(this, dataPacket));
        } else if (dataPacket.isStateStop()) {
            // 客户端停止监听说话，并停止发送声音。并开始识别音频。
            UdpClientContext context = udpClientContextHolder.get(sessionId);
            if (null == context) {
                context = createIfAbsent(udpHello);
            }
            eventPark.post(new VadEndEvent(this, context));
        }

        // 从小智 v1.5.0 开始又不再使用唤醒后等待响应了，而是又变回了 detect > start。所以在 start 接收后处理用户的语音。

//        else if (dataPacket.isStateDetect()) {
//            UdpClientContext context = udpClientContextHolder.get(sessionId);
//            if (null == context) {
//                context = createIfAbsent(udpHello);
//            }
//
//            // 如果是唤醒词唤起的，就不需要处理 VAD了。
//            String text = dataPacket.getText();
//
//            // 发送 STT 数据
//            DataPacket stt = DataPacket.builder().type(MessageType.STT.getValue()).text(text).sessionId(sessionId).build();
//            eventPark.post(new P2pMessageEvent(this, context, stt));
//
//            // 发送 STT 事件
//            eventPark.post(new Speech2TextSuccessEvent(this, context, udpHello, text));
//        }

        return null;
    }

    public UdpClientContext createIfAbsent(UdpHello udpHello) {
        String sessionId = udpHello.getSessionId();
        return udpClientContextBuilder.createIfAbsent(udpHello, new Consumer<String>() {
                    @Override
                    public void accept(String sessionId) {
                        nettyUDPServer.loadClientAddressCache(sessionId);
                    }
                })
                .setLastTimeOnReceiveAudio(LocalDateTime.now())
                .setAudioResponseSender(bytes -> nettyUDPServer.sendMessage(bytes, sessionId));
    }
}
