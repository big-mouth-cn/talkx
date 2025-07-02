package org.bigmouth.gpt.xiaozhi.mqtt.aliyun;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bigmouth.gpt.xiaozhi.entity.DataPacket;

/**
 * @author Allen Hu
 * @date 2025/3/18
 */
@Data
@Builder
@AllArgsConstructor
public class ForwardMessageFromDevice {

    private String p2pTopic;
    private DataPacket dataPacket;
}
