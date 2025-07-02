package org.bigmouth.gpt.autoconfigure.message;

import com.bxm.warcar.message.MessageSender;
import com.bxm.warcar.message.sms.submail.SubmailSmsMessageSender;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Allen Hu
 * @date 2025/3/27
 */
@Configuration
@EnableConfigurationProperties(SubmailSmsProperties.class)
public class SubmailSmsMessageAutoConfiguration {

    private final SubmailSmsProperties smsProperties;

    public SubmailSmsMessageAutoConfiguration(SubmailSmsProperties smsProperties) {
        this.smsProperties = smsProperties;
    }

    @Bean
    public MessageSender submailSmsMessageSender() {
        return new SubmailSmsMessageSender(smsProperties.getAppid(), smsProperties.getAppkey(), smsProperties.getUrl());
    }
}
