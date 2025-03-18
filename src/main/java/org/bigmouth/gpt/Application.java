package org.bigmouth.gpt;

import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.entity.AiModel;
import org.bigmouth.gpt.integration.wechat.WeChatConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * @author allen
 * @date 2023-04-20
 * @since 1.0
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableConfigurationProperties({ApplicationConfig.class, WeChatConfig.class})
@MapperScan({"org.bigmouth.gpt.mapper.talkx"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * 按照优先级获取请求地址
     * @param aiModel 模型
     * @param defaultRequestUri 默认请求地址
     * @return 请求地址
     */
    public static String getRequestUri(AiModel aiModel, String defaultRequestUri) {
        String modelUri = Optional.ofNullable(aiModel)
                .map(AiModel::getRequestUrl)
                .orElse(null);
        if (StringUtils.isNotBlank(modelUri)) {
            return modelUri;
        }
        return defaultRequestUri;
    }

    /**
     * 按照优先级获取请求地址
     * @param aiModel 模型
     * @param defaultRequestUri 默认请求地址
     * @param proxyBaseUrl 代理地址
     * @return 请求地址
     */
    public static String getRequestUri(AiModel aiModel, String defaultRequestUri,
                                       String proxyBaseUrl) {
        String requestUri = getRequestUri(aiModel, defaultRequestUri);
        if (StringUtils.isBlank(proxyBaseUrl)) {
            return requestUri;
        }
        UriComponents proxy = UriComponentsBuilder.fromUriString(proxyBaseUrl).build();
        return UriComponentsBuilder.fromUriString(requestUri)
                .scheme(proxy.getScheme())
                .host(proxy.getHost())
                .port(proxy.getPort())
                .build()
                .toString();
    }
}
