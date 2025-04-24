package org.bigmouth.gpt.xiaozhi.mqtt.aliyun;

import com.alibaba.mqtt.server.ServerConsumer;
import com.alibaba.mqtt.server.callback.MessageListener;
import com.alibaba.mqtt.server.config.ChannelConfig;
import com.alibaba.mqtt.server.config.ConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiMqttConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Allen Hu
 * @date 2025/2/21
 */
@Slf4j
public class MqttCloudConsumer {

    private final XiaozhiMqttConfig xiaozhiMqttConfig;
    private final ServerConsumer serverConsumer;
    private final MqttCloudConsumerMessageListener mqttCloudConsumerMessageListener;
    private final MqttCloudTalkXFrontEndConsumerMessageListener mqttCloudTalkXFrontEndConsumerMessageListener;

    public MqttCloudConsumer(XiaozhiMqttConfig xiaozhiMqttConfig, MqttCloudConsumerMessageListener mqttCloudConsumerMessageListener, MqttCloudTalkXFrontEndConsumerMessageListener mqttCloudTalkXFrontEndConsumerMessageListener) {
        this.xiaozhiMqttConfig = xiaozhiMqttConfig;
        this.mqttCloudConsumerMessageListener = mqttCloudConsumerMessageListener;
        this.mqttCloudTalkXFrontEndConsumerMessageListener = mqttCloudTalkXFrontEndConsumerMessageListener;

        String domain = xiaozhiMqttConfig.getEndpoint();
        int port = xiaozhiMqttConfig.getCloudPort();
        String instanceId = xiaozhiMqttConfig.getInstanceId();
        String accessKey = xiaozhiMqttConfig.getAccessKey();
        String secretKey = xiaozhiMqttConfig.getSecretKey();

        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.setDomain(domain);
        channelConfig.setPort(port);
        channelConfig.setInstanceId(instanceId);
        channelConfig.setAccessKey(accessKey);
        channelConfig.setSecretKey(secretKey);

        this.serverConsumer = new ServerConsumer(channelConfig, new ConsumerConfig());
    }

    @PostConstruct
    public void init() {
        try {
            this.serverConsumer.start();
            this.serverConsumer.subscribeTopic(xiaozhiMqttConfig.getTopicOfServer(), mqttCloudConsumerMessageListener);
            this.serverConsumer.subscribeTopic(xiaozhiMqttConfig.getTalkxMqttTopicOfServer(), mqttCloudTalkXFrontEndConsumerMessageListener);
            log.info("MQTT cloud consumer started");
        } catch (IOException | TimeoutException e) {
            log.error("Failed to start MQTT cloud consumer", e);
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            this.serverConsumer.stop();
            log.info("MQTT cloud consumer stopped");
        } catch (IOException e) {
            log.error("Failed to stop MQTT cloud consumer", e);
        }
    }
}
