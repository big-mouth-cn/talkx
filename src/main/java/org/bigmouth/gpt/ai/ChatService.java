package org.bigmouth.gpt.ai;

import org.bigmouth.gpt.Application;
import org.bigmouth.gpt.ai.entity.ApiKey;
import org.bigmouth.gpt.ai.entity.ChatServiceArgument;
import org.bigmouth.gpt.entity.AiModel;
import org.springframework.core.Ordered;

/**
 * @author allen
 * @date 2023/5/6
 * @since 1.0.0
 */
public interface ChatService extends Ordered {

    /**
     * 返回这个实现属于哪个平台
     * @return 平台类型
     * @see org.bigmouth.gpt.utils.Constants.AiPlatform#PLATFORM_TYPE_OPENAI
     */
    Integer platformType();

    /**
     * Chat
     * @param argument Argument
     */
    void chat(ChatServiceArgument argument);

    /**
     * 返回默认的请求地址
     * @return 请求地址
     */
    String getDefaultRequestUri();

    /**
     * 判断是否在访问密钥不足时进行通知
     * @return 如果在访问密钥不足时进行通知，则返回true；否则返回false
     */
    default boolean isNotifyIfAccessKeyNotEnough() {
        return true;
    }

    /**
     * 按照优先级获取请求地址
     * @param aiModel 模型
     * @param apiKey ApiKey
     * @return 请求地址
     */
    default String getRequestUri(AiModel aiModel, ApiKey apiKey) {
        String defaultRequestUri = getDefaultRequestUri();
        return Application.getRequestUri(aiModel, defaultRequestUri, apiKey.getApiUrl());
    }
}
