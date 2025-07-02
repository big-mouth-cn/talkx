package org.bigmouth.gpt.xiaozhi.mqtt.talkx;

import com.alibaba.fastjson.JSONObject;
import com.bxm.warcar.utils.JsonHelper;
import com.bxm.warcar.utils.StringHelper;
import com.bxm.warcar.utils.UUIDHelper;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.utils.NetworkUtils;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiMqttConfig;
import org.bigmouth.gpt.xiaozhi.entity.DataPacket;
import org.bigmouth.gpt.xiaozhi.entity.MessageType;
import org.bigmouth.gpt.xiaozhi.forest.ForwardConnectRequest;
import org.bigmouth.gpt.xiaozhi.forest.ForwardConnectResponse;
import org.bigmouth.gpt.xiaozhi.forest.TalkXApi;
import org.bigmouth.gpt.xiaozhi.handler.MessageHandler;
import org.bigmouth.gpt.xiaozhi.handler.MessageHandlerFactory;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.bigmouth.gpt.service.impl.DeviceServiceImpl.parse2ClientIdSuffix;

/**
 * MQTT转发客户端，用于连接MQTT服务器并处理消息
 * @author Allen Hu
 * @author Claude 3.5
 * @author TongYi
 * @date 2025/3/18
 */
@Slf4j
public class TalkXMqttFrontEndClient implements DisposableBean {

    private final TalkXApi talkXApi;
    private final MessageHandlerFactory messageHandlerFactory;
    private final XiaozhiMqttConfig xiaozhiMqttConfig;
    private MqttAsyncClient mqttClient;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> reconnectTask;
    private static final int RECONNECT_DELAY = 5000; // 重连延迟5秒
    private static final int QOS = 0;

    public TalkXMqttFrontEndClient(TalkXApi talkXApi, MessageHandlerFactory messageHandlerFactory, XiaozhiMqttConfig xiaozhiMqttConfig) {
        this.talkXApi = talkXApi;
        this.messageHandlerFactory = messageHandlerFactory;
        this.xiaozhiMqttConfig = xiaozhiMqttConfig;
        this.scheduler = new ScheduledThreadPoolExecutor(1);
        connect();
    }

    @Override
    public void destroy() throws Exception {
        this.disconnect();
    }

    private static String getMacAddress() {
        String macAddress = NetworkUtils.getMacAddress();
        if (StringHelper.isNotBlank(macAddress)) {
            return macAddress;
        }
        return UUIDHelper.generate();
    }

    public String getClientId() {
        String macAddress = getMacAddress();
        String newMac = parse2ClientIdSuffix(macAddress);
        return xiaozhiMqttConfig.getTalkxMqttClientIdPrefixForDevice() + newMac;
    }

    public void connect() {
        try {
            String clientId = getClientId();
            ForwardConnectResponse forwardConnectResponse = fetchForwardConnectResponse(clientId);
            if (forwardConnectResponse == null) {
                return;
            }
            mqttClient = new MqttAsyncClient(forwardConnectResponse.getServerUrl(), clientId, new MemoryPersistence());

            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setCleanStart(true);
            connOpts.setUserName(forwardConnectResponse.getUsername());
            connOpts.setPassword(forwardConnectResponse.getPassword().getBytes());
            connOpts.setKeepAliveInterval(90);
            connOpts.setAutomaticReconnect(true);
            connOpts.setConnectionTimeout(5000);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void disconnected(MqttDisconnectResponse disconnectResponse) {
                    log.info("MQTT Connection lost: {}", disconnectResponse.getReasonString());
                    scheduleReconnect();
                }

                @Override
                public void mqttErrorOccurred(MqttException exception) {
                    log.error("MQTT error occurred: ", exception);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String json = new String(message.getPayload());
                    log.debug("Receive MQTT message, topic: {}, payload: {}", topic, json);

                    DataPacket dataPacket = JSONObject.parseObject(json, DataPacket.class);
                    MessageType messageType = dataPacket.of();
                    MessageHandler messageHandler = messageHandlerFactory.get(messageType);
                    if (messageHandler == null) {
                        log.warn("No message handler found for message type: {}", messageType);
                        return;
                    }
                    String p2pTopic = dataPacket.getP2pTopic();

                    DataPacket response = messageHandler.handle(dataPacket);
                    if (response == null) {
                        return;
                    }

                    ForwardMessage forwardMessage = new ForwardMessage();
                    forwardMessage.setId(UUIDHelper.generate());
                    forwardMessage.setAction(ForwardMessage.ACTION_SEND_MSG);
                    forwardMessage.setTopic(p2pTopic);
                    forwardMessage.setJson(StringHelper.convert(JsonHelper.convert2bytes(response)));
                    publish(xiaozhiMqttConfig.getTalkxMqttTopicOfServer(), forwardMessage);
                }

                @Override
                public void deliveryComplete(IMqttToken token) {
                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    log.info("MQTT {} Successful", reconnect ? "reconnect" : "connect");
                    if (reconnectTask != null) {
                        reconnectTask.cancel(false);
                    }
                }

                @Override
                public void authPacketArrived(int reasonCode, MqttProperties properties) {
                }
            });

            mqttClient.connect(connOpts).waitForCompletion();
        } catch (MqttException e) {
            log.error("MQTT connect failed", e);
            scheduleReconnect();
        }
    }

    private ForwardConnectResponse fetchForwardConnectResponse(String clientId) {
        try {
            String macAddress = getMacAddress();
            ForwardConnectRequest forwardConnectRequest = new ForwardConnectRequest();
            forwardConnectRequest.setMacAddress(macAddress);
            forwardConnectRequest.setClientId(clientId);
            return talkXApi.forwardConnect(forwardConnectRequest);
        } catch (Exception e) {
            log.error("Connect TalkX Center Server fail! {}", e.getMessage());
            System.exit(-1);
            return null;
        }
    }

    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            if (scheduler != null) {
                scheduler.shutdown();
            }
        } catch (MqttException e) {
            log.error("MQTT shutdown failed", e);
        }
    }

    /**
     * 向 TalkX 中心发送“转发消息”
     * @param topic 中心topic，不是客户端的topic
     * @param message 需要转发的消息体
     */
    public void publish(String topic, ForwardMessage message) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage(JsonHelper.convert2bytes(message));
                mqttMessage.setQos(QOS);
                mqttClient.publish(topic, mqttMessage);
                log.debug("MQTT message send success, topic: {}, payload: {}", topic, message);
            } else {
                log.warn("MQTT not connected, send fail. topic: {}, payload: {}", topic, message);
            }
        } catch (MqttException e) {
            log.error("MQTT message send fail, topic: {}, payload: {}", topic, message);
        }
    }

    private void scheduleReconnect() {
        if (reconnectTask != null && !reconnectTask.isDone()) {
            return;
        }

        reconnectTask = scheduler.schedule(() -> {
            log.info("MQTT try reconnect...");
            connect();
        }, RECONNECT_DELAY, TimeUnit.MILLISECONDS);
    }
}
