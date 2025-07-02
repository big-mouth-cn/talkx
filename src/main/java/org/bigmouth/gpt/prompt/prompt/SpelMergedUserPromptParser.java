package org.bigmouth.gpt.prompt.prompt;

import org.apache.commons.lang.StringUtils;
import org.bigmouth.gpt.ai.entity.GptRole;
import org.bigmouth.gpt.ai.entity.Message;
import org.bigmouth.gpt.ai.prompt.UserPromptParser;
import org.bigmouth.gpt.entity.Prompt;
import org.bigmouth.gpt.entity.User;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通过设置 Prompt.contentPrompt 来动态生成消息。
 * 将入参对象传入解析器，解析后得到的字符串，作为新的消息保存到消息列表中，并且清空之前的消息列表。
 * 因此 messages 即便是多个，但是只保留最后一个。
 *
 * @author Allen Hu
 * @date 2025/3/13
 */
@Configuration
public class SpelMergedUserPromptParser implements UserPromptParser {

    @Override
    public void parser(User user, Prompt prompt, List<Message> messages) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("prompt", prompt);
        variables.put("messages", messages);
        PromptParser parser = new SpelExpressionPromptParser(variables);
        String contentPrompt = prompt.getContentPrompt();
        if (StringUtils.isBlank(contentPrompt)) {
            return;
        }
        String processed = parser.process(contentPrompt);
        messages.clear();
        messages.add(new Message().setRole(GptRole.USER.getName()).setContent(processed));
    }
}
