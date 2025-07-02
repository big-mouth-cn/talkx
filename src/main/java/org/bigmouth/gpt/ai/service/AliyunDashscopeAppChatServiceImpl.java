package org.bigmouth.gpt.ai.service;

import com.alibaba.dashscope.app.*;
import com.alibaba.dashscope.common.ImageURL;
import com.alibaba.dashscope.common.MessageContentBase;
import com.alibaba.dashscope.common.MessageContentImageURL;
import com.alibaba.dashscope.common.MessageContentText;
import com.bxm.warcar.integration.eventbus.EventPark;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.ai.ChatService;
import org.bigmouth.gpt.ai.entity.*;
import org.bigmouth.gpt.entity.AttachVo;
import org.bigmouth.gpt.entity.ChatRequest;
import org.bigmouth.gpt.entity.Friend;
import org.bigmouth.gpt.entity.Prompt;
import org.bigmouth.gpt.event.ChatCompletionEvent;
import org.bigmouth.gpt.event.ChatRequestEvent;
import org.bigmouth.gpt.exceptions.AiAccountException;
import org.bigmouth.gpt.utils.Constants;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Allen Hu
 * @date 2025/4/22
 */
@Slf4j
@Configuration
public class AliyunDashscopeAppChatServiceImpl implements ChatService {

    private final ApplicationConfig applicationConfig;
    private final EventPark eventPark;
    private final Application application = new Application();

    public AliyunDashscopeAppChatServiceImpl(ApplicationConfig applicationConfig, EventPark eventPark) {
        this.applicationConfig = applicationConfig;
        this.eventPark = eventPark;
    }

    @Override
    public Integer platformType() {
        return Constants.AiPlatform.PLATFORM_TYPE_ALIYUN_DASHSCOPE;
    }

    @Override
    public void chat(ChatServiceArgument argument) {
        ChatRequest chatRequest = argument.getChatRequest();
        Friend friend = argument.getFriend();
        Prompt prompt = argument.getPrompt();

        List<Message> messages = chatRequest.getMessages();

        List<com.alibaba.dashscope.common.Message> messageList = convert2Message(messages);
        int messageContextSize = prompt.getMessageContextSize();
        if (messageContextSize > 0 && messageList.size() > messageContextSize) {
            messageList = messageList.stream().skip(messageList.size() - messageContextSize).collect(Collectors.toList());
        }

        String systemPrompt = prompt.getSystemPrompt();
        if (StringUtils.isNotBlank(systemPrompt)) {
            com.alibaba.dashscope.common.Message system = new com.alibaba.dashscope.common.Message();
            system.setRole(GptRole.SYSTEM.getName());
            system.setContent(systemPrompt);
            messageList.add(0, system);
        }

        final StringBuilder msgBuilder = new StringBuilder();
        final FlowableState state = new FlowableState();
        Usage usage = new Usage();

        try {
            String variables = prompt.getVariables();
            JsonObject jsonObject = CozeChatServiceImpl.convert2JsonObject(variables);

            ApplicationParam param = ApplicationParam.builder()
                    .apiKey(Optional.ofNullable(friend.getAliyunDashscopeApiKey()).filter(StringUtils::isNotBlank).orElse(applicationConfig.getAliyunDashscopeApiKey()))
                    .appId(friend.getAliyunDashscopeAppId())
                    .workspace(friend.getAliyunDashscopeWorkspaceId())
                    .sessionId(chatRequest.getSessionId())
                    .messages(messageList)
                    .incrementalOutput(true)
                    .hasThoughts(true)
                    .bizParams(jsonObject)
                    .build();

            AtomicBoolean firstPacket = new AtomicBoolean(true);
            Flowable<ApplicationResult> result = application.streamCall(param);
            result.blockingForEach(data -> {
                if (firstPacket.compareAndSet(true, false)) {
                    state.initFirstByteTimeInNanoTime();
                    // 发送请求成功的事件
                    eventPark.post(new ChatRequestEvent(this, chatRequest, null, argument.getUser()));
                }
                ApplicationOutput output = data.getOutput();
                ApplicationUsage appUsage = data.getUsage();
                String text = output.getText();
                String sessionId = output.getSessionId();

                if (StringUtils.isNotEmpty(text)) {
                    msgBuilder.append(text);
                    ByteWriter<byte[]> writeConsumer = argument.getWriteConsumer();
                    if (null != writeConsumer) {
                        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
                        writeConsumer.write(bytes);
                    }
                    SimpleHandler flushRunnable = argument.getFlushRunnable();
                    if (null != flushRunnable) {
                        flushRunnable.execute();
                    }
                }
                if (Objects.nonNull(appUsage)) {
                    List<ApplicationUsage.ModelUsage> models = appUsage.getModels();
                    if (CollectionUtils.isNotEmpty(models)) {
                        for (ApplicationUsage.ModelUsage model : models) {
                            Integer inputTokens = model.getInputTokens();
                            Integer outputTokens = model.getOutputTokens();
                            usage.setPromptTokens(usage.getPromptTokens() + inputTokens);
                            usage.setCompletionTokens(usage.getCompletionTokens() + outputTokens);
                            usage.setTotalTokens(usage.getTotalTokens() + (inputTokens + outputTokens));
                        }
                    }
                }
            });
        } catch (Exception e) {
            log.error("call aliyun dashscope app error: ", e);

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

    @NotNull
    private List<com.alibaba.dashscope.common.Message> convert2Message(List<Message> messages) {
        return messages.stream().map(message -> {
            com.alibaba.dashscope.common.Message e = new com.alibaba.dashscope.common.Message();
            e.setRole(message.getRole());
            e.setContent(message.getContent());
            List<AttachVo> attachVos = message.getAttachments();
            if (CollectionUtils.isNotEmpty(attachVos)) {
                List<MessageContentBase> contents = Lists.newArrayList();
                MessageContentBase text = MessageContentText.builder().text(message.getContent()).build();
                contents.add(text);

                for (AttachVo attachVo : attachVos) {
                    ImageURL imageURL = ImageURL.builder().url(attachVo.getUrl()).build();
                    MessageContentImageURL image = MessageContentImageURL.builder().imageURL(imageURL).build();
                    contents.add(image);
                }
                e.setContents(contents);
            } else {
                e.setContent(message.getContent());
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
}
