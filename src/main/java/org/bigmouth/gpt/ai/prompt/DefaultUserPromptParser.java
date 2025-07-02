package org.bigmouth.gpt.ai.prompt;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bigmouth.gpt.ai.entity.Message;
import org.bigmouth.gpt.entity.Prompt;
import org.bigmouth.gpt.entity.User;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Allen Hu
 * @date 2025/3/13
 */
@Deprecated
@Slf4j
@Configuration
public class DefaultUserPromptParser implements UserPromptParser {
    @Override
    public void parser(User user, Prompt prompt, List<Message> messages) {
        if (prompt.isEmptyContentPrompt()) {
            return;
        }
        String contentPrompt = prompt.getContentPrompt();
        if (StringUtils.isBlank(contentPrompt)) {
            return;
        }
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        Message last = messages.get(messages.size() - 1);
        String content = last.getContent();
        // 动态宏
        String macro = prompt.getMacro();
        if (StringUtils.isBlank(macro)) {
            return;
        }
        last.setContent(contentPrompt.replaceAll(Pattern.quote(macro), content));
    }
}
