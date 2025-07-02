package org.bigmouth.gpt.prompt.prompt;


import com.bxm.warcar.utils.AbstractBeanBus;

/**
 * @author Allen Hu
 * @date 2025/3/12
 */
public class PromptGeneratorFactory extends AbstractBeanBus<Integer, PromptGenerator> {

    @Override
    protected Class<PromptGenerator> getInstanceClazz() {
        return PromptGenerator.class;
    }

    @Override
    protected Integer getKey(String beanName, PromptGenerator bean) {
        return bean.ofType();
    }
}
