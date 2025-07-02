package org.bigmouth.gpt.ai.prompt;

import org.bigmouth.gpt.ai.entity.Message;
import org.bigmouth.gpt.entity.Prompt;
import org.bigmouth.gpt.entity.User;

import java.util.List;

public interface UserPromptParser {

    /**
     * 解析用户输入的提示，并返回解析后的提示
     *
     * @param user     用户
     * @param prompt   提示对象
     * @param messages 消息列表
     * @return 解析后的提示
     */
    void parser(User user, Prompt prompt, List<Message> messages);
}
