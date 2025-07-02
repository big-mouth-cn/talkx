package org.bigmouth.gpt.ai.prompt;

import com.bxm.warcar.utils.AbstractBeanBus;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.prompt.prompt.VelocityMergedUserPromptParser;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

import java.util.Map;

/**
 * @author Allen Hu
 * @date 2025/3/12
 */
@Slf4j
@Configuration
public class UserPromptParserFactory extends AbstractBeanBus<String, UserPromptParser> {

    private final ApplicationConfig applicationConfig;

    public UserPromptParserFactory(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    protected Class<UserPromptParser> getInstanceClazz() {
        return UserPromptParser.class;
    }

    @Override
    protected String getKey(String beanName, UserPromptParser bean) {
        return ClassUtils.getUserClass(bean).getName();
    }

    public UserPromptParser getDefault() {
        return get(DefaultUserPromptParser.class.getName());
    }

    public UserPromptParser getVelocityMergedUserPromptParser() {
        return get(VelocityMergedUserPromptParser.class.getName());
    }

    public UserPromptParser getByRoleType(String roleType) {
        try {
            Map<String, Class<? extends UserPromptParser>> UserPromptParserMap = applicationConfig.getUserPromptParserMap();
            Class<? extends UserPromptParser> aClass = UserPromptParserMap.get(roleType);
            if (null == aClass) {
                return null;
            }
            return get(aClass.getName());
        } catch (Exception e) {
            log.error("get: ", e);
            return null;
        }
    }
}
