package com.bxm.warcar.cache.impls.redis;

import com.bxm.warcar.cache.Counter;
import com.bxm.warcar.cache.KeyGenerator;
import com.bxm.warcar.utils.TypeHelper;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.List;
import java.util.Map;

/**
 * @author allen
 * @date 2017-12-04
 */
public class JedisCounter implements Counter {

    private final JedisPool jedisPool;

    public JedisCounter(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public Object getClientOriginal() {
        return jedisPool;
    }

    @Override
    public Long incrementAndGet(KeyGenerator keyGenerator) {
        return incrementAndGet(keyGenerator, 0);
    }

    @Override
    public Long incrementAndGet(KeyGenerator keyGenerator, int expireTimeInSecond) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            Long incr = jedis.incr(key);

            setExpire(jedis, key, expireTimeInSecond);

            return incr;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public Long incrementByAndGet(KeyGenerator keyGenerator, long inc) {
        return incrementByAndGet(keyGenerator, inc, 0);
    }

    @Override
    public Long incrementByAndGet(KeyGenerator keyGenerator, long inc, int expireTimeInSecond) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            Long incr = jedis.incrBy(key, inc);

            setExpire(jedis, key, expireTimeInSecond);

            return incr;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public Double incrFloatByAndGet(KeyGenerator keyGenerator, double inc) {
        return incrFloatByAndGet(keyGenerator, inc, 0);
    }

    @Override
    public Double incrFloatByAndGet(KeyGenerator keyGenerator, double inc, int expireTimeInSecond) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            Double incr = jedis.incrByFloat(key, inc);

            setExpire(jedis, key, expireTimeInSecond);

            return incr;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public Long decrementAndGet(KeyGenerator keyGenerator) {
        return decrementAndGet(keyGenerator, 0);
    }

    @Override
    public Long decrementAndGet(KeyGenerator keyGenerator, int expireTimeInSecond) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            Long decr = jedis.decr(key);

            setExpire(jedis, key, expireTimeInSecond);

            return decr;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public Long decrementByAndGet(KeyGenerator keyGenerator, long dec) {
        return decrementByAndGet(keyGenerator, dec, 0);
    }

    @Override
    public Long decrementByAndGet(KeyGenerator keyGenerator, long dec, int expireTimeInSecond) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            Long decr = jedis.decrBy(key, dec);

            setExpire(jedis, key, expireTimeInSecond);

            return decr;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public void set(KeyGenerator keyGenerator, long value) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.set(key, String.valueOf(value));
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public void expire(KeyGenerator keyGenerator, int seconds) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.expire(key, seconds);
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public Long get(KeyGenerator keyGenerator) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            return TypeHelper.castToLong(jedis.get(key));
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public Long hincrementAndGet(KeyGenerator keyGenerator, String field) {
        return hincrementAndGet(keyGenerator, field, 0);
    }

    @Override
    public Long hincrementAndGet(KeyGenerator keyGenerator, String field, int expireTimeInSecond) {
        return hincrementByAndGet(keyGenerator, field, 1, expireTimeInSecond);
    }

    @Override
    public Long hincrementByAndGet(KeyGenerator keyGenerator, String field, long inc) {
        return hincrementByAndGet(keyGenerator, field, inc, 0);
    }

    @Override
    public Long hincrementByAndGet(KeyGenerator keyGenerator, String field, long inc, int expireTimeInSecond) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            Long decr = jedis.hincrBy(key, field, inc);

            setExpire(jedis, key, expireTimeInSecond);

            return decr;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public Double hincrFloatByAndGet(KeyGenerator keyGenerator, String field, double inc) {
        return hincrFloatByAndGet(keyGenerator, field, inc, 0);
    }

    @Override
    public Double hincrFloatByAndGet(KeyGenerator keyGenerator, String field, double inc, int expireTimeInSecond) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            Double decr = jedis.hincrByFloat(key, field, inc);

            setExpire(jedis, key, expireTimeInSecond);

            return decr;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public Long hget(KeyGenerator keyGenerator, String field) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            String v = jedis.hget(key, field);

            return NumberUtils.toLong(v, 0);
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public Map<String, Long> hgetall(KeyGenerator keyGenerator) {
        String key = getKey(keyGenerator);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            Map<String, Long> rst = Maps.newHashMap();
            final String startCursor = "0";
            String cursor = startCursor;
            do {
                ScanResult<Map.Entry<String, String>> hscan = jedis.hscan(key, cursor, new ScanParams().count(10000));
                cursor = hscan.getCursor();
                List<Map.Entry<String, String>> result = hscan.getResult();
                if (CollectionUtils.isNotEmpty(result)) {
                    for (Map.Entry<String, String> entry : result) {
                        String k = entry.getKey();
                        String v = entry.getValue();
                        rst.put(k, NumberUtils.toLong(v));
                    }
                }
            } while (! cursor.equals(startCursor));

            return rst;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    private void setExpire(Jedis jedis, String key, int expireTimeInSecond) {
        if (expireTimeInSecond > 0)
            jedis.expire(key, expireTimeInSecond);
    }

    private String getKey(KeyGenerator keyGenerator) {
        if (null == keyGenerator) {
            throw new NullPointerException("getKeyGenerator");
        }

        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }
        return key;
    }
}
