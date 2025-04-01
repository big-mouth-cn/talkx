package org.bigmouth.gpt.xiaozhi.handler;

import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.xiaozhi.entity.DataPacket;
import org.bigmouth.gpt.xiaozhi.entity.MessageType;
import org.bigmouth.gpt.xiaozhi.udp.UdpClientContextHolder;
import org.springframework.context.annotation.Configuration;

/**
 * @author Allen Hu
 * @date 2025/2/21
 */
@Slf4j
@Configuration
public class GoodbyeMessageHandler implements MessageHandler {

    private final UdpClientContextHolder udpClientContextHolder;

    public GoodbyeMessageHandler(UdpClientContextHolder udpClientContextHolder) {
        this.udpClientContextHolder = udpClientContextHolder;
    }

    @Override
    public MessageType onType() {
        return MessageType.GOODBYE;
    }

    /**
     * @param dataPacket 消息数据包
     * @return
     */
    @Override
    public DataPacket handle(DataPacket dataPacket) {
        String sessionId = dataPacket.getSessionId();
        try {
            udpClientContextHolder.goodbye(sessionId);
            log.info("[{}] - Goodbye!!", sessionId);
        } catch (Exception e) {
            log.error("{} goodbye error: ", sessionId, e);
        }
        return null;
    }
}
