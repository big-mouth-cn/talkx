package org.bigmouth.gpt.xiaozhi.mqtt.aliyun;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.mqtt.server.callback.MessageListener;
import com.alibaba.mqtt.server.model.MessageProperties;
import com.bxm.warcar.utils.StringHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Allen Hu
 * @date 2025/2/21
 */
@Slf4j
public class MqttCloudTalkXFrontEndConsumerMessageListener implements MessageListener {

    private final MqttCloudProducer mqttCloudProducer;

    public MqttCloudTalkXFrontEndConsumerMessageListener(MqttCloudProducer mqttCloudProducer) {
        this.mqttCloudProducer = mqttCloudProducer;
    }

    @Override
    public void process(String msgId, MessageProperties messageProperties, byte[] payload) {
        String json = new String(payload);
        String firstTopic = messageProperties.getFirstTopic();
        String secondTopic = messageProperties.getSecondTopic();
        String clientId = messageProperties.getClientId();

        log.info("[{}/{}] Receive message: {} from: {}", firstTopic, secondTopic, json, clientId);

        try {
            ForwardMessage forwardMessage = JSONObject.parseObject(json, ForwardMessage.class);
            if (forwardMessage.isSendMsgAction()) {
                String topic = forwardMessage.getTopic();
                mqttCloudProducer.sendMessageQuiet(topic, StringHelper.convert(forwardMessage.getJson()));
            }
        } catch (IllegalArgumentException | JSONException e) {
            log.error("Failed to parse message: {}", json, e);
        } catch (Exception e) {
            log.error("Failed to process message: {}", json, e);
        }
    }
}
