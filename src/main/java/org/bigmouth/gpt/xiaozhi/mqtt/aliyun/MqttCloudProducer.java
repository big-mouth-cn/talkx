package org.bigmouth.gpt.xiaozhi.mqtt.aliyun;

import com.alibaba.mqtt.server.ServerProducer;
import com.alibaba.mqtt.server.callback.SendCallback;
import com.alibaba.mqtt.server.common.SendResult;
import com.alibaba.mqtt.server.config.ChannelConfig;
import com.alibaba.mqtt.server.config.ProducerConfig;
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
public class MqttCloudProducer implements InitializingBean, DisposableBean {

    private final ServerProducer serverProducer;

    public MqttCloudProducer(XiaozhiMqttConfig xiaozhiMqttConfig) {
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

        this.serverProducer = new ServerProducer(channelConfig, new ProducerConfig());
    }

    public void sendMessageQuiet(String topic, byte[] payload)  {
        try {
            this.serverProducer.sendMessage(topic, payload);
        } catch (IOException ignored) {
        }
    }

    public SendResult sendMessage(String topic, byte[] payload) throws IOException {
        return this.serverProducer.sendMessage(topic, payload);
    }

    public void sendMessage(String topic, byte[] payload, SendCallback sendCallback) {
        try {
            this.serverProducer.sendMessage(topic, payload, sendCallback);
        } catch (IOException e) {
            log.error("Failed to send message", e);
            sendCallback.onFail();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.init();
    }

    public void init() {
        try {
            this.serverProducer.start();
            log.info("MQTT cloud producer started");
        } catch (IOException | TimeoutException e) {
            log.error("Failed to start MQTT cloud consumer", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        try {
            this.serverProducer.stop();
            log.info("MQTT cloud producer stopped");
        } catch (IOException e) {
            log.error("Failed to stop MQTT cloud consumer", e);
        }
    }
}
