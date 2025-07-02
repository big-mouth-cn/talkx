package org.bigmouth.gpt.ai.entity.voice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.catalina.connector.ClientAbortException;
import org.bigmouth.gpt.ai.entity.ApiKey;
import org.bigmouth.gpt.ai.entity.SimpleHandler;
import org.bigmouth.gpt.entity.User;

import java.io.OutputStream;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author allen
 * @date 2023/5/6
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
public class Text2SpeechArgument {

    private String id;
    /**
     * 用户信息，游客时是null
     */
    private User user;
    /**
     * ApiKey
     */
    private ApiKey apiKey;
    /**
     * 内容
     */
    private Tts1Request tts1;
    /**
     * 明确本次访问需要排除的 ApiKey
     */
    private Set<String> excludeApiKey;
    /**
     * 写入流接口
     */
    private OutputStream outputStream;
    /**
     * 结束流处理逻辑
     */
    private SimpleHandler completeRunnable;
    /**
     * 用户取消接口
     */
    private BiConsumer<ClientAbortException, String> clientAbortExceptionStringBiConsumer;
}
