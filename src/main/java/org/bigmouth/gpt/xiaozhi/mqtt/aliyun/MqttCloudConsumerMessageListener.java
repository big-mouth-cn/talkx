package org.bigmouth.gpt.xiaozhi.mqtt.aliyun;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.mqtt.server.callback.MessageListener;
import com.alibaba.mqtt.server.callback.SendCallback;
import com.alibaba.mqtt.server.model.MessageProperties;
import com.bxm.warcar.utils.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiMqttConfig;
import org.bigmouth.gpt.xiaozhi.entity.DataPacket;
import org.bigmouth.gpt.xiaozhi.entity.MessageType;
import org.bigmouth.gpt.xiaozhi.handler.MessageHandler;
import org.bigmouth.gpt.xiaozhi.handler.MessageHandlerFactory;
import org.bigmouth.gpt.xiaozhi.mqtt.FrontEndHolder;

/**
 * @author Allen Hu
 * @date 2025/2/21
 */
@Slf4j
public class MqttCloudConsumerMessageListener implements MessageListener {

    private final XiaozhiMqttConfig xiaozhiMqttConfig;
    private final MessageHandlerFactory messageHandlerFactory;
    private final MqttCloudProducer mqttCloudProducer;
    private final FrontEndHolder frontEndHolder;

    public MqttCloudConsumerMessageListener(XiaozhiMqttConfig xiaozhiMqttConfig,
                                            MessageHandlerFactory messageHandlerFactory,
                                            MqttCloudProducer mqttCloudProducer, FrontEndHolder frontEndHolder) {
        this.xiaozhiMqttConfig = xiaozhiMqttConfig;
        this.messageHandlerFactory = messageHandlerFactory;
        this.mqttCloudProducer = mqttCloudProducer;
        this.frontEndHolder = frontEndHolder;
    }

    @Override
    public void process(String msgId, MessageProperties messageProperties, byte[] payload) {
        String json = new String(payload);
        String firstTopic = messageProperties.getFirstTopic();
        String secondTopic = messageProperties.getSecondTopic();
        String clientId = messageProperties.getClientId();

        log.info("[{}/{}] Receive message: {} from: {}", firstTopic, secondTopic, json, clientId);

        try {
            DataPacket dataPacket = JSONObject.parseObject(json, DataPacket.class);
            String p2pTopic = xiaozhiMqttConfig.createP2pTopic(clientId);
            dataPacket.setClientId(clientId);
            dataPacket.setP2pTopic(p2pTopic);

            String frontEndClientId = frontEndHolder.get(clientId);
            if (null != frontEndClientId) {
                // 转发给前置机
                String frontP2pClient = xiaozhiMqttConfig.createFrontP2pClient(frontEndClientId);
                mqttCloudProducer.sendMessageQuiet(frontP2pClient, JsonHelper.convert2bytes(dataPacket));
                log.info("Send message to frontEndClientId: {} - {}", frontP2pClient, dataPacket);
            } else {
                MessageType messageType = dataPacket.of();
                MessageHandler messageHandler = messageHandlerFactory.get(messageType);
                if (messageHandler == null) {
                    log.warn("No message handler found for message type: {}", messageType);
                    return;
                }

                DataPacket response = messageHandler.handle(dataPacket);
                if (response == null) {
                    return;
                }

                mqttCloudProducer.sendMessage(p2pTopic, JsonHelper.convert2bytes(response), new SendCallback() {
                    @Override
                    public void onSuccess(String msgId) {
                    }

                    @Override
                    public void onFail() {
                        log.error("Failed to send message: {} - {}", p2pTopic, response);
                    }
                });
            }
        } catch (IllegalArgumentException | JSONException e) {
            log.error("Failed to parse message: {}", json, e);
        } catch (Exception e) {
            log.error("Failed to process message: {}", json, e);
        }
    }
}
