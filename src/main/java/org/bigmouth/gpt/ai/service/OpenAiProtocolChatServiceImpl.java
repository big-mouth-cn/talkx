package org.bigmouth.gpt.ai.service;

import com.alibaba.fastjson.JSONObject;
import com.bxm.warcar.integration.eventbus.EventPark;
import com.bxm.warcar.utils.JsonHelper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.assistants.run.ToolChoice;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.function.FunctionDefinition;
import com.theokanning.openai.function.FunctionExecutorManager;
import com.theokanning.openai.service.ChatMessageAccumulator;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.Flowable;
import io.reactivex.exceptions.CompositeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.ai.ChatService;
import org.bigmouth.gpt.ai.OpenAiModelAdapter;
import org.bigmouth.gpt.ai.agent.AgentExecuteException;
import org.bigmouth.gpt.ai.entity.*;
import org.bigmouth.gpt.autoconfigure.OpenAiServiceAutoConfiguration;
import org.bigmouth.gpt.entity.*;
import org.bigmouth.gpt.event.ChatCompletionEvent;
import org.bigmouth.gpt.event.ChatRequestEvent;
import org.bigmouth.gpt.exceptions.AiAccountException;
import org.bigmouth.gpt.exceptions.AiNetworkException;
import org.bigmouth.gpt.utils.Constants;
import org.bigmouth.gpt.utils.ImageDownloader;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author allen
 * @date 2023/5/6
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class OpenAiProtocolChatServiceImpl implements ChatService, Ordered, OpenAiModelAdapter {

    private final ApplicationConfig config;
    private final EventPark eventPark;

    public OpenAiProtocolChatServiceImpl(ApplicationConfig config, EventPark eventPark) {
        this.config = config;
        this.eventPark = eventPark;
    }


    @Override
    public String getRoleType() {
        return null;
    }

    @Override
    public Integer platformType() {
        return Constants.AiPlatform.PLATFORM_TYPE_OPENAI;
    }

    @Override
    public String getDefaultRequestUri() {
        return UriComponentsBuilder.fromUriString(config.getLlmApiHost())
                .path("/v1/chat/completions")
                .build()
                .toString();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private List<ToolMessage> executeFunctions(FunctionExecutorManager functionExecutorManager,
                                               Function<ChatToolCall, ToolMessage> xiaozhiIotFunctionExecutor,
                                               List<ChatToolCall> functionCalls) {
        List<ToolMessage> chatMessages = new ArrayList<>();
        for (ChatToolCall functionCall : functionCalls) {
            ChatFunctionCall function = functionCall.getFunction();
            String name = function.getName();
            JsonNode arguments = function.getArguments();
            log.info("execution tool: {} - {}", name, arguments);
            String toolId = functionCall.getId();
            ToolMessage toolMessage = null;
            try {
                FunctionDefinition functionDefinition = null == functionExecutorManager ? null : functionExecutorManager.getFunctionDefinition(name);
                if (null == functionDefinition) {
                    // 执行小智IoT 工具
                    toolMessage = xiaozhiIotFunctionExecutor.apply(functionCall);
                } else {
                    toolMessage = functionExecutorManager.executeAndConvertToChatMessage(name, arguments, toolId);
                }
                log.info("executed: {}", toolMessage.getTextContent());
            } catch (AgentExecuteException e) {
                toolMessage = new ToolMessage(e.getMessage(), toolId);
                log.warn("executed: {}", e.getMessage());
            } catch (Exception e) {
                String content = "execute tool error: " + e.getMessage();
                toolMessage = new ToolMessage(content, toolId);
                log.error("execute tool error: {}", e.getMessage());
            }

            chatMessages.add(toolMessage);
        }
        return chatMessages;
    }

    @Override
    public void chat(ChatServiceArgument argument) {
        ChatRequest chatRequest = argument.getChatRequest();
        ApiKey ak = argument.getApiKey();
        AiModel aiModel = argument.getAiModel();
        Prompt prompt = argument.getPrompt();
        User user = argument.getUser();
        boolean isSupportTool = Objects.equals(aiModel.getIsSupportTool(), Constants.YES);

        String requestUrl = getRequestUri(aiModel, ak);

        OpenApiRequest openApiRequest = prompt.getRequest()
                .setModel(aiModel.getModelName())
                .setStream(chatRequest.isStream());

        if (openApiRequest.isStream()) {
            StreamOptions streamOptions = new StreamOptions();
            streamOptions.setIncludeUsage(true);
            openApiRequest.setStreamOptions(streamOptions);
        }

        createMessageToOpenApiRequest(aiModel, openApiRequest, chatRequest, prompt);

        Function<String, String> updateSystemPromptFunction = argument.getUpdateSystemPromptFunction();
        if (updateSystemPromptFunction != null) {
            List<Message> messages = openApiRequest.getMessages();
            for (Message chatMessage : messages) {
                if (chatMessage.getRole().equals(GptRole.SYSTEM.getName())) {
                    String apply = updateSystemPromptFunction.apply(chatMessage.getContent());
                    chatMessage.setContent(apply);
                    break;
                }
            }
        }

        String accessKey = ak.getApiKey();

        final StringBuilder msgBuilder = new StringBuilder();
        final FlowableState state = new FlowableState();
        Usage usage = new Usage();
        try {
            ChatCompletionRequest requestBody = OpenAiServiceAutoConfiguration.MAPPER.readValue(toJSONString(openApiRequest), ChatCompletionRequest.class);

            List<ChatMessage> chatMessages = requestBody.getMessages();
            Function<ChatToolCall, ToolMessage> customFunctionExecutor = argument.getCustomFunctionExecutor();
            FunctionExecutorManager functionExecutorManager = argument.getFunctionExecutorManager();
            List<ChatTool> chatTools = argument.getChatTools();
            if (!CollectionUtils.isEmpty(chatTools) && isSupportTool) {
                requestBody.setTools(chatTools);
                requestBody.setToolChoice(ToolChoice.AUTO);
            }

            // requestUrl = http://47.98.242.33:7880/v1/chat/completions
            // baseUrl = http://47.98.242.33:7880/v1/
            String baseUrl = requestUrl.replace("chat/completions", "");

            Duration timeout = Duration.ofMinutes(5);
            OpenAiService openAiService = new OpenAiService(accessKey, timeout, baseUrl);

//            log.info("request >>> {}", OpenAiServiceAutoConfiguration.MAPPER.writeValueAsString(requestBody));

            AtomicBoolean firstPacket = new AtomicBoolean(true);
            AtomicBoolean firstReasoning = new AtomicBoolean(false);
            AtomicBoolean firstContent = new AtomicBoolean(false);
            while (true) {
                Flowable<ChatCompletionChunk> streamedChatCompletion = openAiService.streamChatCompletion(requestBody);

                AssistantMessage accumulatedMessage = openAiService.mapStreamToAccumulatorWrapper(streamedChatCompletion)
                        .doOnRequest(n -> {
                            state.initFirstByteTimeInNanoTime();
                        })
                        .doOnNext((chatMessageAccumulatorWrapper -> {
                            if (firstPacket.compareAndSet(true, false)) {
                                // 发送请求成功的事件
                                eventPark.post(new ChatRequestEvent(this, chatRequest, openApiRequest, user));
                            }
                            ChatMessageAccumulator chatMessageAccumulator = chatMessageAccumulatorWrapper.getChatMessageAccumulator();
                            if (!chatMessageAccumulator.isFunctionCall()) {
                                com.theokanning.openai.Usage accumulatorUsage = chatMessageAccumulator.getUsage();
                                if (Objects.nonNull(accumulatorUsage)) {
                                    usage.setPromptTokens((int) accumulatorUsage.getPromptTokens());
                                    usage.setCompletionTokens((int) accumulatorUsage.getCompletionTokens());
                                    usage.setTotalTokens((int) accumulatorUsage.getTotalTokens());
                                }
                                AssistantMessage messageChunk = chatMessageAccumulator.getMessageChunk();
                                if (Objects.nonNull(messageChunk)) {
                                    String reasoningContent = messageChunk.getReasoningContent();
                                    if (StringUtils.isNotBlank(reasoningContent)) {
                                        if (firstReasoning.compareAndSet(false, true)) {
                                            writeAndFlushContent(argument, msgBuilder, "<think>\n");
                                        }
                                        writeAndFlushContent(argument, msgBuilder, reasoningContent);
                                    }

                                    String source = messageChunk.getContent();
                                    if (StringUtils.isNotEmpty(source)) {
                                        if (firstContent.compareAndSet(false, true) && firstReasoning.get()) {
                                            writeAndFlushContent(argument, msgBuilder, "\n</think>\n");
                                        }
                                        writeAndFlushContent(argument, msgBuilder, source);
                                    }
                                }
                            }
                        }))
                        .doOnError(throwable -> {
                            if (throwable instanceof OpenAiHttpException) {
                                OpenAiHttpException e = (OpenAiHttpException) throwable;
                                AiAccountException exception = new AiAccountException(e.getMessage(), ak);
                                exception.setSc(e.statusCode);
                                throw exception;
                            } else if (throwable instanceof JsonParseException) {
                                throw new AiNetworkException(throwable);
                            } else {
                                throw new IOException(throwable);
                            }
                        })
                        .lastElement()
                        .blockingGet()
                        .getChatMessageAccumulator()
                        .getAccumulatedMessage();

                chatMessages.add(accumulatedMessage);

                List<ChatToolCall> functionCalls = accumulatedMessage.getToolCalls();
                if (Objects.isNull(functionCalls)) {
                    break;
                }

                List<ToolMessage> executed = this.executeFunctions(functionExecutorManager, customFunctionExecutor, functionCalls);
                chatMessages.addAll(executed);
            }

        } catch (Exception e) {
            state.setThrowable(e);

            if (e instanceof IOException || e instanceof AiNetworkException) {
                throw new AiNetworkException(e);
            } else if (e instanceof AiAccountException) {
                throw ((AiAccountException) e);
            } else if (e instanceof CompositeException) {
                List<Throwable> exceptions = ((CompositeException) e).getExceptions();
                for (Throwable exception : exceptions) {
                    if (exception instanceof AiAccountException) {
                        throw ((AiAccountException) exception);
                    }
                }
            } else {
                throw new RuntimeException(e);
            }
        } finally {
            CompleteConsumer complete = argument.getComplete();
            if (Objects.nonNull(complete)) {
                List<Message> messages = openApiRequest.getMessages();
                messages.add(new Message().setRole(GptRole.ASSISTANT.getName()).setContent(msgBuilder.toString()));
                complete.accept(messages);
            }

            Throwable throwable = state.getThrowable();
            if (throwable == null || throwable instanceof ClientAbortException) {
                // 发送请求完成的事件
                ChatCompletionEvent.Parameter parameter = ChatCompletionEvent.Parameter.builder()
                        .completion(msgBuilder.toString())
                        .startNanoTime(state.getFirstByteTimeInNanoTime())
                        .usage(usage)
                        .messages(openApiRequest.getMessages())
                        .argument(argument)
                        .build();
                eventPark.post(new ChatCompletionEvent(this, parameter));
                if (log.isDebugEnabled()) {
                    log.debug("Post ChatCompletionEvent!");
                }
            }
        }
    }

    private void writeAndFlushContent(ChatServiceArgument argument, StringBuilder msgBuilder, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        msgBuilder.append(content);
        ByteWriter<byte[]> writeConsumer = argument.getWriteConsumer();
        if (null != writeConsumer) {
            writeConsumer.write(bytes);
        }
        SimpleHandler flushRunnable = argument.getFlushRunnable();
        if (null != flushRunnable) {
            flushRunnable.execute();
        }
    }

    public static String toJSONString(OpenApiRequest request) {
        Model model = Model.ofName(request.getModel());
        if (Objects.isNull(model)) {
            return toGpt4VisionPreviewJson(request, model);
        }
        switch (model) {
            case GPT_4_ALL:
            case GPT_4_GIZMO:
                return toGptAllJson(request);
            case GPT_4_VISION_PREVIEW:
            case GPT_4O:
            case GPT_4O_MINI:
            case O1:
            case O1_MINI:
            case O1_PREVIEW:
                return toGpt4VisionPreviewJson(request, model);
            default:
                return JsonHelper.convert(request);
        }
    }

    private static String toGptAllJson(OpenApiRequest request) {
        List<Message> messages = request.getMessages();
        for (Message message : messages) {
            List<AttachVo> attachments = message.getAttachments();
            if (CollectionUtils.isNotEmpty(attachments)) {
                StringBuilder att = new StringBuilder();
                for (AttachVo attachment : attachments) {
                    String url = attachment.getUrl();
                    att.append(url).append("\n");
                }
                message.setContent(att + message.getContent());
            }
        }
        return JsonHelper.convert(request);
    }

    private static String toGpt4VisionPreviewJson(OpenApiRequest request, Model model) {
        List<Message> messages = request.getMessages();
        List<Object> visionMessages = Lists.newArrayListWithExpectedSize(messages.size());
        for (Message message : messages) {
            // 如果不是用户的消息，就不能转换成 vision message
            if (!GptRole.USER.getName().equals(message.getRole())) {
                visionMessages.add(message);
                continue;
            }
            VisionMessage visionMessage = new VisionMessage();
            visionMessage.setName(message.getName()).setRole(message.getRole());

            List<VisionMessage.Type> list = Lists.newArrayList();
            visionMessage.setContent(list);

            // 普通消息的方式
            String content = message.getContent();

            // 不再支持普通消息方式 2024-09-24
//            Matcher matcher = URL_PATTERN.matcher(content);
//            while (matcher.find()) {
//                String url = matcher.group();
//                VisionMessage.UrlOnImageUrl urlOnImageUrl = new VisionMessage.UrlOnImageUrl().setUrl(url);
//                VisionMessage.ImageUrl imageUrl = new VisionMessage.ImageUrl();
//                imageUrl.setImage_url(urlOnImageUrl);
//                imageUrl.setType(VisionMessage.Type.TYPE_IMAGE_URL);
//                list.add(imageUrl);
//                content = content.replaceAll(Pattern.quote(url), StringUtils.EMPTY);
//            }

            // 附件的方式
            List<AttachVo> attachments = message.getAttachments();
            if (CollectionUtils.isNotEmpty(attachments)) {
                for (AttachVo attachment : attachments) {
                    String url = attachment.getUrl();
                    if (model == Model.O1 || model == Model.O1_MINI || model == Model.O1_PREVIEW) {
                        try {
                            url = ImageDownloader.downloadImageAsBase64(url);
                        } catch (IOException e) {
                            log.error("downloadImageAsBase64: {}", e.getMessage());
                        }
                    }
                    VisionMessage.UrlOnImageUrl urlOnImageUrl = new VisionMessage.UrlOnImageUrl()
                            .setUrl(url);
                    VisionMessage.ImageUrl imageUrl = new VisionMessage.ImageUrl();
                    imageUrl.setImage_url(urlOnImageUrl);
                    imageUrl.setType(VisionMessage.Type.TYPE_IMAGE_URL);
                    list.add(imageUrl);
                }
            }

            VisionMessage.Text text = new VisionMessage.Text();
            text.setType(VisionMessage.Type.TYPE_TEXT);
            text.setText(content);

            list.add(0, text);
            visionMessages.add(visionMessage);
        }

        JSONObject jsonObject = JSONObject.parseObject(JsonHelper.convert(request));
        jsonObject.remove("messages");
        jsonObject.put("messages", visionMessages);
        return jsonObject.toJSONString();
    }

    /**
     * 将请求的消息列表添加提示词，然后根据模型的token限制，保留有效的消息数量。设置到OpenApiRequest中。
     *
     * @param aiModel 模型
     * @param openApiRequest OpenApiRequest
     * @param chatRequest 请求的消息列表
     * @param prompt 提示词
     */
    public static void createMessageToOpenApiRequest(AiModel aiModel, OpenApiRequest openApiRequest, ChatRequest chatRequest, Prompt prompt) {
        List<Message> messages;
        messages = addSystemPromptMessage2FirstIndexIfExists(prompt, chatRequest);
        messages = truncateMessageList(messages, prompt.getMessageContextSize());
        openApiRequest.setMessages(messages);
        removeMessageIfOutOfMaxTokens(openApiRequest, aiModel);
        replaceSystemPromptForModel(aiModel, messages);
    }

    private static void replaceSystemPromptForModel(AiModel aiModel, List<Message> messages) {
        // replace messages[0]'s role
        if (StringUtils.equals(aiModel.getModelName(), "o1")) {
            for (Message message : messages) {
                if (message.getRole().equals(GptRole.SYSTEM.getName())) {
                    message.setRole(GptRole.DEVELOPER.getName());
                    break;
                }
            }
        } else if (StringUtils.startsWith(aiModel.getModelName(), "o1")) {
            messages.removeIf(message -> (message.getRole().equals(GptRole.SYSTEM.getName())));
        }
    }

    /**
     * 消息列表从第一个非system类型的开始递归删除，直到消息列表的token符合模型的最大限制的80%。
     * 如果排除非system消息后的消息长度是1，则不做任何处理。
     *
     * @param openApiRequest OpenApiRequest
     * @param model 模型
     */
    private static void removeMessageIfOutOfMaxTokens(OpenApiRequest openApiRequest, AiModel model) {
        int tokens = openApiRequest.getTokens();
        // 默认预留模型最大tokens的20%作为completion
        Integer modelMaxToken = model.getMaxToken();
        int remainToken = BigDecimal.valueOf(modelMaxToken).multiply(BigDecimal.valueOf(0.8)).intValue();
        Integer completionMaxTokens = openApiRequest.getMaxTokens();
        if (Objects.nonNull(completionMaxTokens)) {
            // 如果设置了最大completion，那么裁剪消息的token=maxTokens-completionTokens
            remainToken = modelMaxToken - completionMaxTokens;
        }
        if (tokens >= remainToken) {
            List<Message> messages = openApiRequest.getMessages();
            long count = messages.stream()
                    .filter(message -> !message.getRole().equals(GptRole.SYSTEM.getName()))
                    .count();
            if (count == 1) {
                // 如果删除系统提示词后，只剩一条消息了。
                // 判断一下token+completionToken是否大于maxToken
                // 如果超过了，则删除completionMaxTokens，这样确保是有回复了
                if (tokens + Optional.ofNullable(completionMaxTokens).orElse(0) >= modelMaxToken) {
                    openApiRequest.setMaxTokens(null);
                    openApiRequest.setMaxCompletionTokens(null);
                }
                return;
            }
            // 遍历消息列表，找到第一条Message类型不是system的索引
            int indexToRemove = -1;
            for (int i = 0; i < messages.size(); i++) {
                Message message = messages.get(i);
                if (!message.getRole().equals(GptRole.SYSTEM.getName())) {
                    indexToRemove = i;
                    break;
                }
            }
            // 如果找到了符合条件的索引，则移除对应的消息
            if (indexToRemove != -1) {
                messages.remove(indexToRemove);
            }
            removeMessageIfOutOfMaxTokens(openApiRequest, model);
        }
    }

    /**
     * Add system prompt message to the first index of the ChatRequest messages if exists.
     *
     * @param request The ChatRequest sent to be handled.
     * @return Updated set of messages from ChatRequest.
     */
    private static List<Message> addSystemPromptMessage2FirstIndexIfExists(Prompt prompt, ChatRequest request) {
        String systemPrompt = prompt.getSystemPrompt();
        List<Message> messages = request.getMessages();
        if (StringUtils.isNotBlank(systemPrompt)) {
            messages.add(0, new Message().setRole(GptRole.SYSTEM.getName()).setContent(systemPrompt));
        }
        return messages;
    }

    /**
     * 从消息列表中获取指定数量的消息子列表。
     *
     * @param messages 消息列表，List<Message>类型。
     * @param maxSize   指定子列表长度，int类型，需满足2<=maxSize<=36，若不满足则取最大/最小值。
     * @return 消息子列表，List<Message>类型。
     * 如果messages为null，返回空列表。
     * 如果messages的长度小于maxSize，则返回messages本身。
     * 如果maxSize为奇数，则修正为偶数。
     * 新的子列表保留messages的第一个元素，其余的从messages中取最后maxSize-1个元素。
     */
    public static List<Message> truncateMessageList(List<Message> messages, int maxSize) {
        if (Objects.isNull(messages)) {
            return Collections.emptyList();
        }
        // Fixed n >= 2
        if (maxSize < 2) {
            maxSize = 2;
        }

        // The repair value is in singular form.
        if (maxSize % 2 != 0) {
            maxSize ++;
        }

        if (maxSize >= messages.size()) {
            return messages;
        }

        List<Message> newList = messages.subList(messages.size() - (maxSize - 1), messages.size());
        newList.add(0, messages.get(0));
        return newList;
    }

    public static byte[] getBytes(String string) {
        if (StringUtils.isEmpty(string)) {
            return null;
        }
        return string.getBytes(StandardCharsets.UTF_8);
    }
}
