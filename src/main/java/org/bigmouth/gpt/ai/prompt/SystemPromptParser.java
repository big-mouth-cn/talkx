package org.bigmouth.gpt.ai.prompt;

import com.alibaba.dashscope.utils.JsonUtils;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.bigmouth.gpt.entity.Prompt;
import org.bigmouth.gpt.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public interface SystemPromptParser {

    /**
     * 解析提示词
     *
     * @param user 用户信息
     * @param prompt 提示词
     */
    void parse(User user, Prompt prompt);

    default Map<String, Object> convert2Variables(User user, Prompt prompt) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("prompt", prompt);
        variables.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        String customVariables = prompt.getVariables();
        if (StringUtils.isNotBlank(customVariables)) {
            try {
                JsonObject jsonObject = JsonUtils.parse(customVariables);
                for (String key : jsonObject.keySet()) {
                    variables.put(key, jsonObject.get(key));
                }
            } catch (Exception ignored) {
            }
        }
        return variables;
    }
}
