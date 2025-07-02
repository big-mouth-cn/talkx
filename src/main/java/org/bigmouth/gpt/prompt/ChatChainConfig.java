package org.bigmouth.gpt.prompt;

import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * @author Allen Hu
 * @date 2025/3/12
 */
@Data
@ConfigurationProperties(prefix = "config.chat")
public class ChatChainConfig {

    /**
     * 对话链，指任意AI对话完成后，将找到下一批链异步执行。
     * 指：roleType
     */
    private Map<String, List<String>> chainMap = Maps.newHashMap();
}
