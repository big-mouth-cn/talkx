package org.bigmouth.gpt.ai.entity;

import java.util.List;

/**
 * @author Allen Hu
 * @date 2025/3/27
 */
public interface CompleteConsumer {

    /**
     * 本次模型调用完成后的消息列表，包括 system / user / assistant。
     * 有可能存在因 max_tokens 的配置导致截取的情况，与请求时可能不一致。
     *
     * @param messages   消息列表
     */
    void accept(List<Message> messages);
}
