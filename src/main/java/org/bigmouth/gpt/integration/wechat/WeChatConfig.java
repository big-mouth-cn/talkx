package org.bigmouth.gpt.integration.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author huxiao
 */
@Data
@ConfigurationProperties(prefix = "config.wechat")
public class WeChatConfig {

    private String appId;
    private String secret;

    private String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";
}
