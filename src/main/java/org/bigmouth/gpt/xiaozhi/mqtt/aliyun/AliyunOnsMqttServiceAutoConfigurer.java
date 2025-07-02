package org.bigmouth.gpt.xiaozhi.mqtt.aliyun;

import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.service.IDeviceService;
import org.bigmouth.gpt.xiaozhi.autoconfigure.XiaozhiAutoConfigurer;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiMqttConfig;
import org.bigmouth.gpt.xiaozhi.handler.MessageHandlerFactory;
import org.bigmouth.gpt.xiaozhi.mqtt.FrontEndHolder;
import org.bigmouth.gpt.xiaozhi.mqtt.MqttService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Allen Hu
 * @date 2025/3/17
 */
@Slf4j
@Configuration
@ConditionalOnBean(XiaozhiAutoConfigurer.class)
@ConditionalOnProperty(name = "xiaozhi.mqtt.type", havingValue = "aliyun_ons")
public class AliyunOnsMqttServiceAutoConfigurer  {

    private final XiaozhiMqttConfig xiaozhiMqttConfig;
    private final IDeviceService deviceService;
    private final MessageHandlerFactory messageHandlerFactory;
    private final FrontEndHolder frontEndHolder;

    public AliyunOnsMqttServiceAutoConfigurer(XiaozhiMqttConfig xiaozhiMqttConfig, IDeviceService deviceService, MessageHandlerFactory messageHandlerFactory, FrontEndHolder frontEndHolder) {
        this.xiaozhiMqttConfig = xiaozhiMqttConfig;
        this.deviceService = deviceService;
        this.messageHandlerFactory = messageHandlerFactory;
        this.frontEndHolder = frontEndHolder;
    }

    @Bean
    public MqttCloudProducer mqttCloudProducer() {
        return new MqttCloudProducer(xiaozhiMqttConfig);
    }

    @Bean
    @ConditionalOnMissingBean(MqttService.class)
    public MqttService aliyunOnsMqttService() {
        return new AliyunOnsMqttService(xiaozhiMqttConfig, deviceService, mqttCloudProducer());
    }

    @Bean
    public MqttCloudConsumerMessageListener mqttCloudConsumerMessageListener() {
        return new MqttCloudConsumerMessageListener(xiaozhiMqttConfig, messageHandlerFactory, mqttCloudProducer(), frontEndHolder);
    }

    @Bean
    public MqttCloudTalkXFrontEndConsumerMessageListener mqttCloudTalkXFrontEndConsumerMessageListener() {
        return new MqttCloudTalkXFrontEndConsumerMessageListener(mqttCloudProducer());
    }

    @Bean
    public MqttCloudConsumer mqttCloudConsumer() {
        return new MqttCloudConsumer(xiaozhiMqttConfig, mqttCloudConsumerMessageListener(), mqttCloudTalkXFrontEndConsumerMessageListener());
    }
}
