package org.bigmouth.gpt.prompt.autoconfigure;

import org.bigmouth.gpt.prompt.ChatChainConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Allen Hu
 * @date 2025/3/12
 */
@Configuration
@EnableConfigurationProperties({
        ChatChainConfig.class
})
public class ChatChainAutoConfiguration {
}
