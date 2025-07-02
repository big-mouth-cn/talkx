package org.bigmouth.gpt.ai.prompt;

import org.apache.commons.lang.StringUtils;
import org.bigmouth.gpt.entity.Prompt;
import org.bigmouth.gpt.entity.User;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Allen Hu
 * @date 2025/3/12
 */
@Deprecated
@Configuration
public class DefaultSystemPromptParser implements SystemPromptParser {

    @Override
    public void parse(User user, Prompt prompt) {
        String systemPrompt = prompt.getSystemPrompt();
        if (StringUtils.isBlank(systemPrompt)) {
            return;
        }
        systemPrompt = systemPrompt.replaceAll("__DATE__", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        prompt.setSystemPrompt(systemPrompt);
    }
}
