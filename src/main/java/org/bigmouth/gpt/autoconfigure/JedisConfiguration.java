package org.bigmouth.gpt.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.time.Duration;

/**
 * @author allen
 * @date 2017-11-27
 */
@Data
@ConfigurationProperties("config.jedis")
public class JedisConfiguration {

    private String host = Protocol.DEFAULT_HOST;
    private String password;
    private int port = Protocol.DEFAULT_PORT;
    private int timeout = Protocol.DEFAULT_TIMEOUT;
    private int database = Protocol.DEFAULT_DATABASE;
    private Duration maxWaitTime = Duration.ofSeconds(1);
    private Duration minEvictableIdleTime = Duration.ofSeconds(60);
    private Duration softMinEvictableIdleTime = Duration.ofSeconds(60);
}
