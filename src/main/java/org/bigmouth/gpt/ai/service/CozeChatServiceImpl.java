package org.bigmouth.gpt.ai.service;

import com.alibaba.dashscope.utils.JsonUtils;
import com.bxm.warcar.integration.eventbus.EventPark;
import com.coze.openapi.client.chat.CreateChatReq;
import com.coze.openapi.client.chat.model.Chat;
import com.coze.openapi.client.chat.model.ChatEvent;
import com.coze.openapi.client.chat.model.ChatEventType;
import com.coze.openapi.client.chat.model.ChatUsage;
import com.coze.openapi.client.connversations.message.model.MessageContentType;
import com.coze.openapi.client.connversations.message.model.MessageObjectString;
import com.coze.openapi.client.connversations.message.model.MessageRole;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.service.CozeAPI;
import com.coze.openapi.service.utils.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import io.reactivex.functions.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.ai.ChatService;
import org.bigmouth.gpt.ai.entity.*;
import org.bigmouth.gpt.entity.*;
import org.bigmouth.gpt.event.ChatCompletionEvent;
import org.bigmouth.gpt.event.ChatRequestEvent;
import org.bigmouth.gpt.exceptions.AiAccountException;
import org.bigmouth.gpt.utils.Constants;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.coze.openapi.service.config.Consts.COZE_CN_BASE_URL;
import static com.coze.openapi.service.config.Consts.COZE_COM_BASE_URL;

/**
 * @author Allen Hu
 * @date 2025/4/25
 */
@Slf4j
@Configuration
public class CozeChatServiceImpl implements ChatService {

    private final ApplicationConfig applicationConfig;
    private final EventPark eventPark;

    public CozeChatServiceImpl(ApplicationConfig applicationConfig, EventPark eventPark) {
        this.applicationConfig = applicationConfig;
        this.eventPark = eventPark;
    }

    @Override
    public Integer platformType() {
        return Constants.AiPlatform.PLATFORM_TYPE_COZE;
    }

    @Override
    public void chat(ChatServiceArgument argument) {
        ChatRequest chatRequest = argument.getChatRequest();
        Friend friend = argument.getFriend();
        Prompt prompt = argument.getPrompt();
        User user = argument.getUser();

        List<Message> messages = chatRequest.getMessages();

        List<com.coze.openapi.client.connversations.message.model.Message> messageList = convert2Message(messages);
        int messageContextSize = prompt.getMessageContextSize();
        if (messageContextSize > 0 && messageList.size() > messageContextSize) {
            messageList = messageList.stream().skip(messageList.size() - messageContextSize).collect(Collectors.toList());
        }

        final StringBuilder msgBuilder = new StringBuilder();
        final FlowableState state = new FlowableState();
        Usage usage = new Usage();

        try {
            String token = Optional.ofNullable(friend.getCozeAccessToken()).filter(StringUtils::isNotBlank).orElse(applicationConfig.getCozeCnAccessToken());
            String botId = friend.getCozeBotId();
            String userId = "TALKX-" + user.getId();

            TokenAuth tokenAuth = new TokenAuth(token);

            String baseUrl = COZE_CN_BASE_URL;
            if (Objects.equals(Constants.Friend.COZE_COM, friend.getFriendType())) {
                baseUrl = COZE_COM_BASE_URL;
            }

            CozeAPI coze = new CozeAPI.Builder()
                    .baseURL(baseUrl)
                    .auth(tokenAuth)
                    .readTimeout((int) Constants.DEFAULT_TIMEOUT.toMillis())
                    .build();

            String variables = prompt.getVariables();
            Map<String, String> varMaps = convert2Map(variables);

            CreateChatReq chatReq = CreateChatReq.builder()
                    .botID(botId)
                    .userID(userId)
                    .customVariables(varMaps)
                    .messages(messageList)
                    .build();

            AtomicBoolean firstPacket = new AtomicBoolean(true);
            AtomicBoolean firstReasoning = new AtomicBoolean(false);
            AtomicBoolean firstContent = new AtomicBoolean(false);

            ChatEvent chatEvent = coze.chat().stream(chatReq)
                    .doOnRequest(t -> state.initFirstByteTimeInNanoTime())
                    .doOnNext(new Consumer<ChatEvent>() {
                        @Override
                        public void accept(ChatEvent chatEvent) throws Exception {
                            if (firstPacket.compareAndSet(true, false)) {
                                eventPark.post(new ChatRequestEvent(this, chatRequest, null, user));
                            }
                            ChatEventType event = chatEvent.getEvent();

                            if (ChatEventType.CONVERSATION_MESSAGE_DELTA.equals(event)) {
                                com.coze.openapi.client.connversations.message.model.Message message = chatEvent.getMessage();
                                String reasoningContent = message.getReasoningContent();
                                if (StringUtils.isNotBlank(reasoningContent)) {
                                    if (firstReasoning.compareAndSet(false, true)) {
                                        String thinkStart = "<think>\n";
                                        writeAndFlushContent(msgBuilder, thinkStart, argument);
                                    }
                                    writeAndFlushContent(msgBuilder, reasoningContent, argument);
                                }
                                String content = message.getContent();
                                if (StringUtils.isNotEmpty(content)) {
                                    if (firstContent.compareAndSet(false, true) && firstReasoning.get()) {
                                        String thinkFinish = "\n</think>\n";
                                        writeAndFlushContent(msgBuilder, thinkFinish, argument);
                                    }
                                    writeAndFlushContent(msgBuilder, content, argument);
                                }
                            } else if (ChatEventType.CONVERSATION_CHAT_COMPLETED.equals(event)) {
                                Chat chat = chatEvent.getChat();
                                ChatUsage chatUsage = chat.getUsage();
                                usage.setPromptTokens(chatUsage.getInputTokens());
                                usage.setCompletionTokens(chatUsage.getOutputTokens());
                                usage.setTotalTokens(chatUsage.getTokenCount());
                            } else if (ChatEventType.CONVERSATION_CHAT_FAILED.equals(event)) {
                                Chat chat = chatEvent.getChat();
                                log.error("call coze error: {}", chat);
                            } else {
                                log.info("other event: {}", event.getValue());
                            }
                        }
                    })
                    .lastElement()
                    .blockingGet();
        } catch (Exception e) {
            log.error("call coze error: ", e);

            state.setThrowable(e);

            throw new AiAccountException(e);
        } finally {
            CompleteConsumer complete = argument.getComplete();
            if (Objects.nonNull(complete)) {
                messages.add(new Message().setRole(GptRole.ASSISTANT.getName()).setContent(msgBuilder.toString()));
                complete.accept(messages);
            }

            if (null == state.getThrowable()) {
                // 发送请求完成的事件
                ChatCompletionEvent.Parameter parameter = ChatCompletionEvent.Parameter.builder()
                        .completion(msgBuilder.toString())
                        .startNanoTime(state.getFirstByteTimeInNanoTime())
                        .usage(usage)
                        .messages(chatRequest.getMessages())
                        .argument(argument)
                        .build();
                eventPark.post(new ChatCompletionEvent(this, parameter));
                if (log.isDebugEnabled()) {
                    log.debug("Post ChatCompletionEvent!");
                }
            }
        }
    }

    private void writeAndFlushContent(StringBuilder msgBuilder, String content, ChatServiceArgument argument) throws IOException {
        msgBuilder.append(content);
        ByteWriter<byte[]> writeConsumer = argument.getWriteConsumer();
        if (null != writeConsumer) {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            writeConsumer.write(bytes);
        }
        SimpleHandler flushRunnable = argument.getFlushRunnable();
        if (null != flushRunnable) {
            flushRunnable.execute();
        }
    }

    @NotNull
    private List<com.coze.openapi.client.connversations.message.model.Message> convert2Message(List<Message> messages) {
        return messages.stream().map(message -> {
            com.coze.openapi.client.connversations.message.model.Message e = new com.coze.openapi.client.connversations.message.model.Message();
            MessageRole messageRole = MessageRole.fromString(message.getRole());
            e.setRole(messageRole);

            String content = message.getContent();
            List<AttachVo> attachVos = message.getAttachments();
            if (CollectionUtils.isNotEmpty(attachVos)) {
                // 多模态内容
                List<MessageObjectString> contents = Lists.newArrayList();
                MessageObjectString text = MessageObjectString.buildText(content);
                contents.add(text);

                for (AttachVo attachVo : attachVos) {
                    MessageObjectString image = MessageObjectString.buildImageByURL(attachVo.getUrl());
                    contents.add(image);
                }

                e.setContentType(MessageContentType.OBJECT_STRING);
                e.setContent(Utils.toJson(contents));
            } else {
                // 纯文本
                e.setContentType(MessageContentType.TEXT);
                e.setContent(content);
            }
            return e;
        }).collect(Collectors.toList());
    }

    @Override
    public String getDefaultRequestUri() {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    static JsonObject convert2JsonObject(String variables) {
        return StringUtils.isBlank(variables) ? new JsonObject() : JsonUtils.parse(variables);
    }

    static Map<String, String> convert2Map(String variables) {
        Map<String, String> result = Maps.newHashMap();
        JsonObject jsonObject = convert2JsonObject(variables);
        jsonObject.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue().getAsString()));
        return result;
    }
}
