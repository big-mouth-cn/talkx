package org.bigmouth.gpt.ai.prompt;

import com.bxm.warcar.utils.AbstractBeanBus;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.prompt.prompt.VelocitySystemPromptParser;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

import java.util.Map;

/**
 * @author Allen Hu
 * @date 2025/3/12
 */
@Slf4j
@Configuration
public class SystemPromptParserFactory extends AbstractBeanBus<String, SystemPromptParser> {

    private final ApplicationConfig applicationConfig;

    public SystemPromptParserFactory(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    protected Class<SystemPromptParser> getInstanceClazz() {
        return SystemPromptParser.class;
    }

    @Override
    protected String getKey(String beanName, SystemPromptParser bean) {
        return ClassUtils.getUserClass(bean).getName();
    }

    public SystemPromptParser getDefault() {
        return get(DefaultSystemPromptParser.class.getName());
    }

    public SystemPromptParser getVelocitySystemPromptParser() {
        return get(VelocitySystemPromptParser.class.getName());
    }

    public SystemPromptParser getByRoleType(String roleType) {
        try {
            Map<String, Class<? extends SystemPromptParser>> systemPromptParserMap = applicationConfig.getSystemPromptParserMap();
            Class<? extends SystemPromptParser> aClass = systemPromptParserMap.get(roleType);
            if (aClass == null) {
                return null;
            }
            return get(aClass.getName());
        } catch (Exception e) {
            log.error("get: ", e);
            return null;
        }
    }
}
