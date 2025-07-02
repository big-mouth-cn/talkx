package org.bigmouth.gpt.autoconfigure.id;

import com.bxm.warcar.id.IdGenerator;
import com.bxm.warcar.id.random.RandomIdGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author allen
 * @date 2022-03-22
 * @since 1.0
 */
@Configuration
@ConditionalOnProperty(name = "config.enable-redis-cache", havingValue = "false")
public class RandomIdGeneratorConfiguration {

    @Bean
    @Primary
    public IdGenerator randomIdGenerator() {
        return new RandomIdGenerator();
    }

    @Bean(name = "orderIdGenerator")
    public IdGenerator orderIdGenerator() {
        return new RandomIdGenerator(22, null);
    }
}
