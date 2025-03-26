package org.bigmouth.gpt.autoconfigure.id;

import com.bxm.warcar.id.IdGenerator;
import com.bxm.warcar.id.redis.RedisIdGenerator;
import org.bigmouth.gpt.autoconfigure.JedisConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.JedisPool;

import static org.bigmouth.gpt.autoconfigure.cache.JedisAutoConfiguration.createJedisPool;

/**
 * @author allen
 * @since 1.0.0
 */
@Configuration
@ConditionalOnBean({JedisPool.class})
@ConditionalOnProperty(name = "config.enable-redis-cache", havingValue = "true")
public class RedisIdGeneratorConfiguration {

    @Primary
    @Bean
    public IdGenerator redisIdGenerator(JedisConfiguration configuration) {
        JedisPool jedisPool = createJedisPool(configuration);
        return new RedisIdGenerator(jedisPool);
    }
}
