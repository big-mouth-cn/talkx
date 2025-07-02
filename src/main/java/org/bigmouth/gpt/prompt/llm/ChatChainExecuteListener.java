package org.bigmouth.gpt.prompt.llm;

import com.bxm.warcar.integration.eventbus.EventListener;
import com.bxm.warcar.integration.eventbus.core.AllowConcurrentEvents;
import com.bxm.warcar.integration.eventbus.core.Subscribe;
import com.bxm.warcar.utils.DateHelper;
import com.bxm.warcar.utils.NamedThreadFactory;
import com.bxm.warcar.utils.UUIDHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.bigmouth.gpt.ai.ChatServiceFactory;
import org.bigmouth.gpt.ai.entity.ChatServiceArgument;
import org.bigmouth.gpt.ai.entity.CompleteConsumer;
import org.bigmouth.gpt.ai.entity.GptRole;
import org.bigmouth.gpt.ai.entity.Message;
import org.bigmouth.gpt.entity.*;
import org.bigmouth.gpt.event.ChatCompletionEvent;
import org.bigmouth.gpt.prompt.ChatChainConfig;
import org.bigmouth.gpt.service.IFriendService;
import org.bigmouth.gpt.service.ISessionMessageService;
import org.bigmouth.gpt.service.ISessionService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Allen Hu
 * @date 2025/3/13
 */
@Configuration
public class ChatChainExecuteListener implements EventListener<ChatCompletionEvent>, DisposableBean {

    private final ChatChainConfig chatChainConfig;
    private final ChatServiceFactory chatServiceFactory;
    private final ISessionService sessionService;
    private final ISessionMessageService sessionMessageService;
    private final IFriendService friendService;
    private final ExecutorService chainExecutor = new ThreadPoolExecutor(10, 20, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), new NamedThreadFactory("chain-exec"));

    public ChatChainExecuteListener(ChatChainConfig chatChainConfig, ChatServiceFactory chatServiceFactory, ISessionService sessionService, ISessionMessageService sessionMessageService, IFriendService friendService) {
        this.chatChainConfig = chatChainConfig;
        this.chatServiceFactory = chatServiceFactory;
        this.sessionService = sessionService;
        this.sessionMessageService = sessionMessageService;
        this.friendService = friendService;
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void consume(ChatCompletionEvent event) {
        ChatCompletionEvent.Parameter parameter = event.getParameter();
        User user = parameter.getUser();
        Prompt prompt = parameter.getPrompt();
        String roleType = prompt.getRoleType();
        ChatServiceArgument argument = parameter.getArgument();

        Map<String, List<String>> chatChainMap = chatChainConfig.getChainMap();
        if (chatChainMap.isEmpty()) {
            return;
        }

        List<String> nextAis = chatChainMap.get(roleType);
        if (CollectionUtils.isEmpty(nextAis)) {
            return;
        }

        ChatCallback callback = new ChatCallback() {
            @Override
            public void callback(List<Message> messages, ChatRequest chatRequest) {
                if (messages.isEmpty()) {
                    return;
                }
                String prompt = null, completion = null;
                for (int i = messages.size(); i > 0; i--) {
                    Message message = messages.get(i - 1);
                    if (null == completion && message.getRole().equals(GptRole.ASSISTANT.getName())) {
                        completion = message.getContent();
                    } else if (null == prompt && message.getRole().equals(GptRole.USER.getName())) {
                        prompt = message.getContent();
                    }
                }
                // 当前AI的RoleType
                String roleType = chatRequest.getRoleType();
                Friend friend = friendService.getByRoleType(roleType);
                int productId = argument.getChatRequest().getProductId();
                String sessionId = sessionService.createSessionId();
                Session session = new Session()
                        .setId(sessionId)
                        .setUserId(user.getId())
                        .setFriendId(friend.getId())
                        .setProductId(productId)
                        .setTitle(DateHelper.format("yyyy-MM-dd HH:mm"))
                        .setCreateTime(LocalDateTime.now());

                sessionMessageService.save(new SessionMessage().setSessionId(session.getId()).setRole(GptRole.USER.getName()).setContent(prompt));
                sessionMessageService.save(new SessionMessage().setSessionId(session.getId()).setRole(GptRole.ASSISTANT.getName()).setContent(completion));
                sessionService.saveOrUpdate(session);
            }
        };

        for (String nextAi : nextAis) {
            chainExecutor.submit(new ChatRunnable(chatServiceFactory, parameter, nextAi, callback));
        }
    }

    @Override
    public void destroy() throws Exception {
        chainExecutor.shutdown();
        chainExecutor.awaitTermination(1, TimeUnit.MINUTES);
    }

    interface ChatCallback {

        void callback(List<Message> messages, ChatRequest chatRequest);
    }

    @Slf4j
    static final class ChatRunnable implements Runnable {

        private final ChatServiceFactory chatServiceFactory;
        private final ChatCompletionEvent.Parameter parameter;
        private final String nextAi;
        private final ChatCallback callback;

        ChatRunnable(ChatServiceFactory chatServiceFactory, ChatCompletionEvent.Parameter parameter, String nextAi, ChatCallback callback) {
            this.chatServiceFactory = chatServiceFactory;
            this.parameter = parameter;
            this.nextAi = nextAi;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                String completion = parameter.getCompletion();
                List<Message> removeMessages = Lists.newArrayList(parameter.getMessages());
                // 删除 messages 中包含 system 的 message
                removeMessages.removeIf(message -> message.getRole().equals(GptRole.SYSTEM.getName()));

                ChatRequest chatRequest = new ChatRequest();
                chatRequest.setRoleType(nextAi);

                List<Message> messages = Lists.newArrayList();
                messages.addAll(removeMessages);
                messages.add(new Message().setRole(GptRole.ASSISTANT.getName()).setContent(completion));
                chatRequest.setMessages(messages);

                ChatServiceArgument argument = ChatServiceArgument.builder()
                        .id(UUIDHelper.generate())
                        .user(parameter.getUser())
                        .chatRequest(chatRequest)
                        .complete(new CompleteConsumer() {
                            @Override
                            public void accept(List<Message> messages) {
                                callback.callback(messages, chatRequest);
                            }
                        })
                        .build();
                chatServiceFactory.chat(argument);
            } catch (Exception e) {
                log.error(" >>> {}", e.getMessage(), e);
            }
        }
    }
}
