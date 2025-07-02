package org.bigmouth.gpt.prompt.prompt;

import org.apache.commons.lang.StringUtils;
import org.bigmouth.gpt.ai.prompt.SystemPromptParser;
import org.bigmouth.gpt.entity.Prompt;
import org.bigmouth.gpt.entity.User;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Allen Hu
 * @date 2025/4/24
 */
@Configuration
public class VelocitySystemPromptParser implements SystemPromptParser {

    @Override
    public void parse(User user, Prompt prompt) {
        Map<String, Object> variables = convert2Variables(user, prompt);
        PromptParser parser = new VelocityPromptParser(variables);
        String systemPrompt = prompt.getSystemPrompt();
        if (StringUtils.isBlank(systemPrompt)) {
            return;
        }
        String processed = parser.process(systemPrompt);
        prompt.setSystemPrompt(processed);
    }
}
