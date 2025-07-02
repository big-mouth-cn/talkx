package org.bigmouth.gpt.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bigmouth.gpt.ai.entity.*;
import org.bigmouth.gpt.entity.AiModel;
import org.bigmouth.gpt.entity.Prompt;
import org.bigmouth.gpt.entity.User;

import java.util.EventObject;
import java.util.List;

/**
 * 聊天对话完成事件
 *
 * @author huxiao
 * @date 2023/11/16
 * @since 1.0.0
 */
public class ChatCompletionEvent extends EventObject {

    private final Parameter parameter;
    public ChatCompletionEvent(Object source, Parameter parameter) {
        super(source);
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class Parameter {

        /**
         * 本次 assistant 回复的完整内容
         */
        private String completion;
        /**
         * 请求消耗的token数
         */
        private Usage usage;
        /**
         * 开始请求的时间
         */
        private Long startNanoTime;
        /**
         * 本次调用的上下文消息列表，但是不包含本次的assistant回复。
         */
        private List<Message> messages;
        /**
         * 请求Chat服务的参数对象
         */
        private ChatServiceArgument argument;

        public Prompt getPrompt() {
            return argument.getPrompt();
        }

        public User getUser() {
            return argument.getUser();
        }

        public ApiKey getApiKey() {
            return argument.getApiKey();
        }

        public AiModel getAiModel() {
            return argument.getAiModel();
        }

        public Message getLastUserMessage() {
            Message userMessage = null;
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message msg = messages.get(i);
                if (GptRole.USER.getName().equals(msg.getRole())) {
                    userMessage = msg;
                    break;
                }
            }
            return userMessage;
        }
    }
}
