package org.bigmouth.gpt.xiaozhi.mqtt.aliyun;

import com.alibaba.mqtt.server.ServerConsumer;
import com.alibaba.mqtt.server.callback.MessageListener;
import com.alibaba.mqtt.server.config.ChannelConfig;
import com.alibaba.mqtt.server.config.ConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiMqttConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Allen Hu
 * @date 2025/2/21
 */
@Slf4j
public class MqttCloudConsumer implements InitializingBean, DisposableBean {

    private final XiaozhiMqttConfig xiaozhiMqttConfig;
    private final ServerConsumer serverConsumer;
    private final MessageListener messageListener;

    public MqttCloudConsumer(XiaozhiMqttConfig xiaozhiMqttConfig, MessageListener messageListener) {
        this.xiaozhiMqttConfig = xiaozhiMqttConfig;
        this.messageListener = messageListener;

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

    @Override
    public void afterPropertiesSet() throws Exception {
        this.init();
    }

    public void init() {
        try {
            this.serverConsumer.start();
            this.serverConsumer.subscribeTopic(xiaozhiMqttConfig.getTopicOfServer(), messageListener);
            log.info("MQTT cloud consumer started");
        } catch (IOException | TimeoutException e) {
            log.error("Failed to start MQTT cloud consumer", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        try {
            this.serverConsumer.stop();
            log.info("MQTT cloud consumer stopped");
        } catch (IOException e) {
            log.error("Failed to stop MQTT cloud consumer", e);
        }
    }
}
