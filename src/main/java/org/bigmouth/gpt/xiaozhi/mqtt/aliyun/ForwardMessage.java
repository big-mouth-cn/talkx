package org.bigmouth.gpt.xiaozhi.mqtt.aliyun;

import lombok.Data;

/**
 * @author Allen Hu
 * @date 2025/3/17
 */
@Data
public class ForwardMessage {

    public static final String ACTION_SEND_MSG = "send_msg";

    private String id;
    private String action;
    private String topic;
    private String json;

    public boolean isSendMsgAction() {
        return ACTION_SEND_MSG.equals(action);
    }
}
