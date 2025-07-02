package org.bigmouth.gpt.ai.entity;

import com.theokanning.openai.completion.chat.ChatTool;
import com.theokanning.openai.completion.chat.ChatToolCall;
import com.theokanning.openai.completion.chat.ToolMessage;
import com.theokanning.openai.function.FunctionExecutorManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.catalina.connector.ClientAbortException;
import org.bigmouth.gpt.entity.*;
import org.springframework.lang.Nullable;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author allen
 * @date 2023/5/6
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
public class ChatServiceArgument {

    private String id;
    /**
     * 用户信息，游客时是null
     */
    private User user;
    /**
     * 对应的好友信息，有可能是null
     */
    private Friend friend;
    /**
     * 模型配置信息
     */
    private AiModel aiModel;
    /**
     * ApiKey
     */
    private ApiKey apiKey;
    /**
     * 提示词
     */
    private Prompt prompt;
    /**
     * 对话请求，由用户请求的
     */
    private ChatRequest chatRequest;
    /**
     * HttpServletResponse
     */
    private HttpServletResponse httpServletResponse;
    /**
     * 写入流接口
     */
    @Nullable
    private ByteWriter<byte[]> writeConsumer;
    /**
     * 刷新缓冲接口
     */
    @Nullable
    private SimpleHandler flushRunnable;
    /**
     * 完成接口
     */
    @Nullable
    private CompleteConsumer complete;
    /**
     * 用户取消接口
     */
    @Nullable
    private BiConsumer<ClientAbortException, String> clientAbortExceptionStringBiConsumer;
    /**
     * Tools
     */
    private List<ChatTool> chatTools;
    /**
     * Function Executor Manager
     */
    private FunctionExecutorManager functionExecutorManager;
    /**
     * 修改系统提示词
     */
    private Function<String, String> updateSystemPromptFunction;
    /**
     * 自定义IoT执行器
     */
    private Function<ChatToolCall, ToolMessage> customFunctionExecutor;
}
