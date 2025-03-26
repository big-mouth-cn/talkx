package org.bigmouth.gpt.autoconfigure.cache;

import com.bxm.warcar.cache.Counter;
import com.bxm.warcar.cache.Fetcher;
import com.bxm.warcar.cache.Updater;
import com.bxm.warcar.cache.impls.redis.JedisCounter;
import com.bxm.warcar.cache.impls.redis.JedisFetcher;
import com.bxm.warcar.cache.impls.redis.JedisUpdater;
import org.bigmouth.gpt.autoconfigure.JedisConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ConditionalOnProperty(name = "config.enable-redis-cache", havingValue = "true")
@EnableConfigurationProperties(JedisConfiguration.class)
public class JedisAutoConfiguration {

    private final JedisConfiguration configuration;

    public JedisAutoConfiguration(JedisConfiguration configuration) {
        this.configuration = configuration;
    }

    public static JedisPool createJedisPool(JedisConfiguration configuration) {
        // If you register JedisPoolConfig or some other class extends BaseGenericObjectPool
        // or it's subclass GenericObjectPoolConfig ( which in apache commons pool ) instances to spring .
        // this problem will happen 。becase the same instance registered to jmx twice .
        // https://github.com/redis/jedis/issues/2781#issuecomment-1092744636
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxWait(configuration.getMaxWaitTime());
        config.setMinEvictableIdleDuration(configuration.getMinEvictableIdleTime());
        config.setSoftMinEvictableIdleDuration(configuration.getSoftMinEvictableIdleTime());
        config.setJmxNamePrefix("jedis-general-pool-");
        return new JedisPool(config, configuration.getHost(), configuration.getPort(), configuration.getTimeout(), configuration.getPassword(), configuration.getDatabase());
    }

    @Bean
    @Primary
    public Fetcher jedisFetcher() {
        JedisPool jedisPool = createJedisPool(configuration);
        return new JedisFetcher(jedisPool);
    }

    @Bean
    @Primary
    public Updater jedisUpdater() {
        JedisPool jedisPool = createJedisPool(configuration);
        return new JedisUpdater(jedisPool);
    }

    @Bean
    @Primary
    public Counter jedisCounter() {
        JedisPool jedisPool = createJedisPool(configuration);
        return new JedisCounter(jedisPool);
    }
}
