package org.bigmouth.gpt.autoconfigure.event;

import com.bxm.warcar.integration.eventbus.EventListener;
import com.bxm.warcar.integration.eventbus.micrometer.EventListenerMeter;
import com.bxm.warcar.integration.eventbus.micrometer.EventListenerMeterAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author allen
 * @date 2019/6/20
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass(EventListener.class)
public class EventListenerMeterAutoConfiguration {

    @Bean
    public EventListenerMeter eventListenerMeter(ApplicationContext applicationContext) {
        return new EventListenerMeter(applicationContext.getBeansOfType(EventListener.class).values());
    }

    @Bean
    public EventListenerMeterAspect eventListenerMeterAspect(EventListenerMeter eventListenerMeter) {
        return new EventListenerMeterAspect(eventListenerMeter);
    }
}
