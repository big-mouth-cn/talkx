package org.bigmouth.gpt.xiaozhi.event.listener;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bxm.warcar.integration.eventbus.EventListener;
import com.bxm.warcar.integration.eventbus.EventPark;
import com.bxm.warcar.integration.eventbus.core.AllowConcurrentEvents;
import com.bxm.warcar.integration.eventbus.core.Subscribe;
import com.bxm.warcar.utils.JsonHelper;
import com.bxm.warcar.utils.UUIDHelper;
import com.google.common.collect.Lists;
import com.theokanning.openai.completion.chat.ChatTool;
import com.theokanning.openai.completion.chat.ChatToolCall;
import com.theokanning.openai.completion.chat.ToolMessage;
import com.theokanning.openai.function.FunctionDefinition;
import com.theokanning.openai.function.FunctionExecutorManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.bigmouth.gpt.ai.ChatServiceFactory;
import org.bigmouth.gpt.ai.agent.AgentFactory;
import org.bigmouth.gpt.ai.entity.*;
import org.bigmouth.gpt.autoconfigure.OpenAiServiceAutoConfiguration;
import org.bigmouth.gpt.entity.*;
import org.bigmouth.gpt.exceptions.AiStatusException;
import org.bigmouth.gpt.service.*;
import org.bigmouth.gpt.utils.Constants;
import org.bigmouth.gpt.xiaozhi.audio.AudioCodec;
import org.bigmouth.gpt.xiaozhi.audio.OpusEncoderUtils;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiConfig;
import org.bigmouth.gpt.xiaozhi.entity.DataPacket;
import org.bigmouth.gpt.xiaozhi.entity.MessageType;
import org.bigmouth.gpt.xiaozhi.entity.Udp;
import org.bigmouth.gpt.xiaozhi.entity.UdpHello;
import org.bigmouth.gpt.xiaozhi.entity.memory.Memory;
import org.bigmouth.gpt.xiaozhi.entity.memory.MemorySearchResult;
import org.bigmouth.gpt.xiaozhi.entity.memory.SearchRequest;
import org.bigmouth.gpt.xiaozhi.event.*;
import org.bigmouth.gpt.xiaozhi.handler.IotConverter;
import org.bigmouth.gpt.xiaozhi.handler.IotMessageHandler;
import org.bigmouth.gpt.xiaozhi.memory.MemoryServiceFactory;
import org.bigmouth.gpt.xiaozhi.tts.TtsData;
import org.bigmouth.gpt.xiaozhi.udp.AudioResponseSequence;
import org.bigmouth.gpt.xiaozhi.udp.UdpClientContext;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Allen Hu
 * @date 2025/2/25
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class ChatCompletionEventListener implements EventListener<Speech2TextSuccessEvent> {

    public static final String PROMPT_SPLIT_START = "<!--extend-start-->";
    public static final String PROMPT_SPLIT_END = "<!--extend-end-->";

    private final XiaozhiConfig xiaozhiConfig;
    private final EventPark eventPark;
    private final IUserService userService;
    private final ISessionMessageService sessionMessageService;
    private final ChatServiceFactory chatServiceFactory;
    private final IDeviceService deviceService;
    private final IotMessageHandler iotMessageHandler;
    private final AgentFactory agentFactory;
    private final IAiModelService aiModelService;
    private final MemoryServiceFactory memoryServiceFactory;
    private final IFriendService friendService;

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void consume(Speech2TextSuccessEvent event) {
        UdpHello udpHello = event.getUdpHello();
        UdpClientContext context = event.getContext();
        String sessionId = context.getSessionId();
        String text = event.getText();
        try {
            // 检查设备
            String clientId = udpHello.getRequest().getClientId();
            Device device = deviceService.findByClientId(clientId);
            if (Objects.isNull(device)) {
                // 设备没有激活或已经解绑了
                throw new IllegalStateException("设备未激活");
            }
            Consumer<String> ttsStart = new Consumer<String>() {
                @Override
                public void accept(String o) {
                    // 发送 TTS | start 请求
                    DataPacket ttsStart = DataPacket.builder().type(MessageType.TTS.getValue()).state(DataPacket.STATE_START).sessionId(sessionId).build();
                    eventPark.post(new P2pMessageEvent(this, context, ttsStart));
                }
            };
            ChatCompleteEndHandler chatCompleteEndHandler = new ChatCompleteEndHandler() {
                @Override
                public void handle(ChatCompleteEntity endEntity) {
                    // 发送对话完成事件
                    eventPark.post(new ChatCompleteEvent(this, context, endEntity));

                    // 设置标记完成
                    context.doTtsMakeFinish();

                    while (true) {
                        if (context.getAudioSendFinish().get()) {
                            // 发送 TTS | stop 请求
                            DataPacket ttsStop = DataPacket.builder().type(MessageType.TTS.getValue()).state(DataPacket.STATE_STOP).sessionId(sessionId).build();
                            eventPark.post(new P2pMessageEvent(this, context, ttsStop));
                            log.info("[{}] - Speak completed.", endEntity.getSessionId());

                            // 发送对话完全完成事件
                            eventPark.post(new ChatCompletionEndEvent(this, udpHello));
                            break;
                        }
                        sleepInMillis(100);
                    }

                    // 如果用户主动不想继续对话，则发送 GOODBYE 请求
                    if (context.isSayGoodbye()) {
                        // 发送 GOODBYE 请求
                        DataPacket goodbye = DataPacket.builder().type(MessageType.GOODBYE.getValue()).sessionId(sessionId).build();
                        eventPark.post(new P2pMessageEvent(this, context, goodbye));
                    }
                }
            };

            // 开始调度音频发送服务
            context.startAudioSendingSchedule();

            // 开始
            ttsStart.accept("");

            // 对话
            this.doChatCompletion(device, text, chatCompleteEndHandler, context);
        } catch (Exception e) {
            log.error("doSpeakingEnd: ", e);

            // 发送 GOODBYE 请求
            DataPacket goodbye = DataPacket.builder().type(MessageType.GOODBYE.getValue()).text("-101").sessionId(sessionId).build();
            eventPark.post(new P2pMessageEvent(this, context, goodbye));

            // 发送对话完成事件
            eventPark.post(new ChatCompletionEndEvent(this, udpHello));
        }
    }

    private void doChatCompletion(Device device,
                                  String text,
                                  ChatCompleteEndHandler chatCompleteEndHandler,
                                  UdpClientContext context) {
        // GPT chat completions
        String sessionId = context.getSessionId();
        String reqId = UUIDHelper.generate();

        long userId = device.getUserId();
        String roleType = device.getRoleType();
        int productId = 0;
        User user = userService.getById(userId);
        Session session = context.getSession();

        List<Message> messages = queryHistoryMessages(session.getId());
        messages.add(new Message().setRole(GptRole.USER.getName()).setContent(text));

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setStream(true);
        chatRequest.setRoleType(roleType);
        chatRequest.setProductId(productId);
        chatRequest.setSessionId(session.getId());
        chatRequest.setMessages(messages);


        // 创建 AiModel
        AiModel aiModel = null;
        ApiKey apiKey = null;
        UserFriendMediaConfig userFriendMediaConfig = context.getUserFriendMediaConfig();
        if ((null != userFriendMediaConfig) && userFriendMediaConfig.isUseCustomModel()) {
            // 优先自定义模型
            String llmModel = userFriendMediaConfig.getLlmModel();
            String proxyBaseUrl = user.getProxyBaseUrl();
            Integer isSupportTool = userFriendMediaConfig.getIsSupportTool();
            aiModel = AiModel.ofBasic(llmModel, proxyBaseUrl, isSupportTool);
            apiKey = new ApiKey()
                    .setUserPrivate(true)
                    .setApiUrl(userFriendMediaConfig.getProxyBaseUrl())
                    .setApiKey(userFriendMediaConfig.getApiKey());
        }
        if (null == aiModel) {
            // 不是自定义模型
            Friend friend = friendService.getByRoleType(roleType);
            if (friend.isAgentFriend()) {
                // 如果是阿里云百炼应用，那么根据 workspace_id 和 app_id 来获取对应的模型。
                String modelName = friend.getAgentSpecialModelName();
                aiModel = aiModelService.get(modelName);
                if (Objects.isNull(aiModel)) {
                    // 如果没有配置对应的模型，那么使用一个空的模型。
                    aiModel = AiModel.ofBasic(friend.getAgentImplementAiPlatformType(), modelName, null, Constants.NO);
                }
            }
        }
        if (null == aiModel) {
            String modelName = Optional.ofNullable(userFriendMediaConfig)
                    .map(UserFriendMediaConfig::getLlmModel)
                    .orElse(xiaozhiConfig.getDefaultLlmModel());
            aiModel = aiModelService.get(modelName);
        }
        if (null == aiModel) {
            throw new AiStatusException(String.format("模型[%s]已经下线了，请切换到其他模型。", user.getModel()));
        }
        log.info("Select model: {}", aiModel.getModelName());
        boolean isSupportTool = Objects.equals(aiModel.getIsSupportTool(), Constants.YES);

        List<ChatTool> chatTools = Lists.newArrayList();
        FunctionExecutorManager functionExecutorManager = null;
        StringBuilder deviceStates = new StringBuilder();
        StringBuilder functionDef = new StringBuilder();
        if (isSupportTool) {
            // create system tools
            List<FunctionDefinition> chatFunctions = agentFactory.createTools();
            functionExecutorManager = OpenAiServiceAutoConfiguration.createFunctionExecutorManager(chatFunctions);
            chatTools = chatFunctions.stream().map(ChatTool::new).collect(Collectors.toList());

            // create xiaozhi iot tools
            List<ChatTool> xiaozhiIotTools = null;
            Map<String, DataPacket> functions = iotMessageHandler.get(sessionId);

            if (MapUtils.isNotEmpty(functions)) {
                DataPacket states = functions.get(IotMessageHandler.FIELD_STATES);
                if (null != states) {
                    deviceStates.append(JsonHelper.convert(states));
                }
                DataPacket descriptions = functions.get(IotMessageHandler.FIELD_DESCRIPTIONS);
                if (null != descriptions) {
                    xiaozhiIotTools = IotConverter.convertTools(JsonHelper.convert(descriptions));
                }
            }
            if (null != xiaozhiIotTools) {
                chatTools.addAll(xiaozhiIotTools);
            }

            // 将函数定义也写到提示词里，因为有些模型可能支持不够完善
            for (ChatTool chatTool : chatTools) {
                Object function = chatTool.getFunction();
                String name;
                String description;
                if (function instanceof FunctionDefinition) {
                    name = ((FunctionDefinition) function).getName();
                    description = ((FunctionDefinition) function).getDescription();
                } else {
                    JSONObject funJson = JSONObject.parseObject(JSONObject.toJSONString(function));
                    name = funJson.getString("name");
                    description = funJson.getString("description");
                }

                functionDef.append("- ").append("**").append(name).append("**:").append(" ").append(description).append("\n");
            }
        }

        StringBuilder accumulatedContent = new StringBuilder();

        ChatServiceArgument argument = ChatServiceArgument.builder()
                .id(reqId)
                .user(user)
                .chatRequest(chatRequest)
                .aiModel(aiModel)
                .apiKey(apiKey)
                .writeConsumer(bytes -> {
                    // 将字节转换为字符串并累积
                    String chunk = new String(bytes);

                    accumulatedContent.append(chunk);

                    // 检查是否包含断句符号
                    while (accumulatedContent.length() > 0) {
                        int endIndex = -1;
                        for (char delimiter : new char[]{'。', '！', '？', '.', '!', '?'}) {
                            int index = accumulatedContent.indexOf(String.valueOf(delimiter));
                            if (index != -1 && (endIndex == -1 || index < endIndex)) {
                                endIndex = index;
                            }
                        }

                        if (endIndex != -1) {
                            // 找到断句符号，处理分段
                            String segment = accumulatedContent.substring(0, endIndex + 1);
                            accumulatedContent.delete(0, endIndex + 1);

                            sendSegmentAudio2Client(segment, context);
                        } else {
                            // 没有找到断句符号，退出循环
                            break;
                        }
                    }
                })
                .complete(new CompleteConsumer() {
                    @Override
                    public void accept(List<Message> messages) {
                        // 处理剩余的累积内容
                        if (accumulatedContent.length() > 0) {
                            String segment = accumulatedContent.toString();
                            accumulatedContent.setLength(0);

                            sendSegmentAudio2Client(segment, context);
                        }

                        String completion = null;

                        // 从 messages 从后往前查到第一个 assistant role的消息为止
                        for (int i = messages.size() - 1; i >= 0; i--) {
                            Message message = messages.get(i);
                            if (message.getRole().equals(GptRole.ASSISTANT.getName())) {
                                completion = message.getContent();
                                break;
                            }
                        }

                        ChatCompleteEntity chatCompleteEntity = ChatCompleteEntity.builder()
                                .sessionId(sessionId)
                                .session(session)
                                .messages(messages)
                                .userPrompt(text)
                                .completion(completion)
                                .build();
                        chatCompleteEndHandler.handle(chatCompleteEntity);
                    }
                })
                .chatTools(chatTools)
                .functionExecutorManager(functionExecutorManager)
                .updateSystemPromptFunction(new Function<String, String>() {
                    @Override
                    public String apply(String prompt) {
                        StringBuilder systemPrompt = new StringBuilder(prompt);
                        systemPrompt.append("\n")
                                .append("\n")
                                .append(PROMPT_SPLIT_START)
                                .append("\n");
                        if (isSupportTool) {
                            systemPrompt.append("[tools definition]").append("\n").append(functionDef).append("\n");
                            systemPrompt.append("[device states]").append("\n").append(deviceStates).append("\n");
                        }
                        systemPrompt.append("[session info]").append("\n")
                                .append("**Now**: ").append(LocalDateTime.now()).append("\n")
                                .append("**SessionId**: ").append(sessionId);

                        String memoryPrompt = createMemoryPrompt(context, text);
                        if (null != memoryPrompt) {
                            systemPrompt.append("\n")
                                    .append(memoryPrompt);
                        }
                        systemPrompt.append(PROMPT_SPLIT_END);
                        return systemPrompt.toString();
                    }
                })
                .customFunctionExecutor(new Function<ChatToolCall, ToolMessage>() {
                    @Override
                    public ToolMessage apply(ChatToolCall chatToolCall) {
                        // 发送 IoT 命令
                        String command = IotConverter.convertCommand(sessionId, chatToolCall);
                        eventPark.post(new P2pMessageEvent(this, context, command));
                        return new ToolMessage("Command send success", chatToolCall.getId());
                    }
                })
                .build();
        chatServiceFactory.chat(argument);
    }


    private String createMemoryPrompt(UdpClientContext context, String prompt) {
        UserFriend userFriend = context.getUserFriend();
        if (Objects.isNull(userFriend)) {
            return null;
        }
        boolean isSupportMemory = Objects.equals(Constants.YES, userFriend.getIsSupportMemory());
        if (!isSupportMemory) {
            return null;
        }
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(prompt)
                .setAgentId(userFriend.getId().toString())
                .setUserId(userFriend.getUserId().toString());
        MemorySearchResult searchResult;
        try {
            searchResult = memoryServiceFactory.getDefault().searchMemories(searchRequest);
        } catch (Exception e) {
            log.error("search memories error", e);
            return null;
        }
        if (Objects.isNull(searchResult)) {
            return null;
        }
        List<Memory> results = searchResult.getResults();
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        StringBuilder memoryPrompt = new StringBuilder();
        memoryPrompt.append("[About user from memories]").append("\n");
        for (Memory result : results) {
            String memory = result.getMemory();
            Double score = Optional.ofNullable(result.getScore()).orElse(0D);
            memoryPrompt.append("- ").append(memory).append("(score:").append(score).append(")").append("\n");
        }
        return memoryPrompt.toString();
    }

    private List<Message> queryHistoryMessages(String sId) {
        List<Message> messages = Lists.newArrayList();
        IPage<SessionMessage> page = sessionMessageService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 64),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SessionMessage>()
                        .eq(SessionMessage::getSessionId, sId)
                        .orderByDesc(SessionMessage::getCreateTime)
        );
        List<SessionMessage> sessionMessages = page.getRecords();
        if (CollectionUtils.isEmpty(sessionMessages)) {
            return messages;
        }
        Collections.reverse(sessionMessages);
        if (CollectionUtils.isNotEmpty(sessionMessages)) {
            for (SessionMessage sessionMessage : sessionMessages) {
                Message msg = new Message();
                msg.setRole(sessionMessage.getRole());
                msg.setContent(sessionMessage.getContent());
                messages.add(msg);
            }
        }
        return messages;
    }

    /**
     * @param segment 句子
     * @param context 上下文
     */
    private void sendSegmentAudio2Client(String segment, UdpClientContext context) {
        try {
            String sessionId = context.getSessionId();
            AtomicBoolean isFirst = new AtomicBoolean(true);
            log.info("[{}] TTS >> {}", sessionId, segment);
            context.getTtsService().stream(segment, pcmBytes -> {
                try {
                    List<byte[]> opus = this.sendAudioDataWithEncrypted(pcmBytes, false, context);
                    TtsData ttsData = TtsData.builder().pcm(pcmBytes).text(segment).opus(opus).sendTtsSentenceStart(isFirst.compareAndSet(true, false)).build();
                    context.offerSentence(ttsData);
                } catch (Exception e) {
                    log.error("Processing error!", e);
                }
            });
            // 发送未解码完剩余部分
            List<byte[]> opus = this.sendAudioDataWithEncrypted(new byte[0], true, context);
            TtsData ttsData = TtsData.builder().pcm(new byte[0]).text(segment).opus(opus).sendTtsSentenceStart(isFirst.compareAndSet(true, false)).build();
            context.offerSentence(ttsData);
        } catch (Exception e) {
            log.error("sendMessageToClient error", e);
        }
    }

    private List<byte[]> sendAudioDataWithEncrypted(byte[] pcmBytes,
                                            boolean endOfStream,
                                            UdpClientContext context) throws Exception {
        OpusEncoderUtils opusEncoderUtils = context.getOpusEncoderUtils();
        AudioResponseSequence audioResponseSequence = context.getAudioResponseSequence();
        Udp udp = context.getUdpHello().getResponse().getUdp();
        String key = udp.getKey();
        String nonce = udp.getNonce();
        List<byte[]> opusList;
        if (endOfStream) {
            opusList = opusEncoderUtils.encodePcmToOpus(new byte[0], true);
        } else {
            opusList = opusEncoderUtils.encodePcmToOpus(pcmBytes, false);
        }
        List<byte[]> encryptedOpusList = Lists.newArrayList();
        for (byte[] bytes : opusList) {
            byte[] encrypted = AudioCodec.encrypt(bytes, key, nonce, audioResponseSequence.get());
            encryptedOpusList.add(encrypted);
            audioResponseSequence.incrementAndGet();
        }
        return encryptedOpusList;
    }

    private void sleepInMillis(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 计算 PCM 数据的播放时长
     *
     * @param dataLength    PCM 数据的字节数
     * @param sampleRate    采样率（Hz）
     * @param channels      声道数
     * @param bitsPerSample 每样本的比特数
     * @return 播放时长（毫秒）
     */
    public static double calculateDuration(int dataLength, int sampleRate, int channels, int bitsPerSample) {
        // 计算每秒字节数
        double bytesPerSecond = sampleRate * channels * (bitsPerSample / 8.0);
        // 计算播放时长（毫秒）
        return (dataLength / bytesPerSecond) * 1000;
    }
}
