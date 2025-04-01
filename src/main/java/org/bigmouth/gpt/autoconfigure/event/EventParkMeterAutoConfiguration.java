package org.bigmouth.gpt.autoconfigure.event;

import com.bxm.warcar.integration.eventbus.EventPark;
import com.bxm.warcar.integration.eventbus.micrometer.EventParkMeter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author allen
 * @date 2019/6/20
 * @since 1.0.0
 */
@Configuration
@ConditionalOnBean(EventPark.class)
public class EventParkMeterAutoConfiguration {

    @Bean
    public EventParkMeter eventParkMeter(ApplicationContext applicationContext) {
        return new EventParkMeter(applicationContext.getBeansOfType(EventPark.class).values());
    }
}
