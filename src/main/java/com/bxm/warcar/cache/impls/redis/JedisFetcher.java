package com.bxm.warcar.cache.impls.redis;

import com.bxm.warcar.cache.DataExtractor;
import com.bxm.warcar.cache.Fetcher;
import com.bxm.warcar.cache.KeyGenerator;
import com.bxm.warcar.cache.serialization.JSONSerialization;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JedisFetcher implements Fetcher {

    private final JedisPool jedisPool;
    private final JSONSerialization serialization;

    public JedisFetcher(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.serialization = new JSONSerialization();
    }

    @Override
    public Object getClientOriginal() {
        return jedisPool;
    }

    @Override
    public Long ttl(KeyGenerator keyGenerator) {
        return ttl(keyGenerator, -1);
    }

    @Override
    public Long ttl(KeyGenerator keyGenerator, int selector) {
        if (null == keyGenerator) {
            throw new NullPointerException("getKeyGenerator");
        }

        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (selector >= 0) {
                jedis.select(selector);
            }
            Long ttl = jedis.ttl(key);

            return ttl;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> T fetch(KeyGenerator keyGenerator, Class<T> cls) {
        return fetch(keyGenerator, null, cls);
    }

    @Override
    public <T> T fetch(KeyGenerator keyGenerator, Class<T> cls, int expireTimeInSecond) {
        return fetch(keyGenerator, null, cls, expireTimeInSecond);
    }

    @Override
    public <T> T fetch(KeyGenerator keyGenerator, DataExtractor<T> dataExtractor, Class<T> cls) {
        return fetch(keyGenerator, dataExtractor, cls, 0);
    }

    @Override
    public <T> T fetch(KeyGenerator keyGenerator, DataExtractor<T> dataExtractor, Class<T> cls, int expireTimeInSecond) {
        return fetchWithSelector(keyGenerator, dataExtractor, cls, expireTimeInSecond, -1);
    }

    @Override
    public <T> List<T> fetchList(KeyGenerator keyGenerator, Class<T> cls) {
        return fetchList(keyGenerator, null, cls);
    }

    @Override
    public <T> List<T> fetchList(KeyGenerator keyGenerator, DataExtractor<List<T>> dataExtractor, Class<T> cls) {
        return fetchList(keyGenerator, dataExtractor, cls, 0);
    }

    @Override
    public <T> List<T> fetchList(KeyGenerator keyGenerator, Class<T> cls, int expireTimeInSecond) {
        return fetchList(keyGenerator, null, cls, expireTimeInSecond);
    }

    @Override
    public <T> List<T> fetchList(KeyGenerator keyGenerator, DataExtractor<List<T>> dataExtractor, Class<T> cls, int expireTimeInSecond) {
        return fetchListWithSelector(keyGenerator, dataExtractor, cls, expireTimeInSecond, -1);
    }

    @Override
    public <T> T hfetch(KeyGenerator keyGenerator, String field, Class<T> cls) {
        return hfetch(keyGenerator, field, null, cls);
    }

    @Override
    public <T> T hfetch(KeyGenerator keyGenerator, String field, Class<T> cls, int expireTimeInSecond) {
        return hfetch(keyGenerator, field, null, cls, expireTimeInSecond);
    }

    @Override
    public <T> T hfetch(KeyGenerator keyGenerator, String field, DataExtractor<T> dataExtractor, Class<T> cls) {
        return hfetch(keyGenerator, field, dataExtractor, cls, 0);
    }

    @Override
    public <T> T hfetch(KeyGenerator keyGenerator, String field, DataExtractor<T> dataExtractor, Class<T> cls, int expireTimeInSecond) {
        return hfetchWithSelector(keyGenerator, field, dataExtractor, cls, expireTimeInSecond, -1);
    }

    @Override
    public <T> List<T> hfetchList(KeyGenerator keyGenerator, String field, Class<T> cls) {
        return hfetchList(keyGenerator, field, null, cls);
    }

    @Override
    public <T> List<T> hfetchList(KeyGenerator keyGenerator, String field, Class<T> cls, int expireTimeInSecond) {
        return hfetchList(keyGenerator, field, null, cls, expireTimeInSecond);
    }

    @Override
    public <T> List<T> hfetchList(KeyGenerator keyGenerator, String field, DataExtractor<List<T>> dataExtractor, Class<T> cls) {
        return hfetchList(keyGenerator, field, dataExtractor, cls, 0);
    }

    @Override
    public <T> List<T> hfetchList(KeyGenerator keyGenerator, String field, DataExtractor<List<T>> dataExtractor, Class<T> cls, int expireTimeInSecond) {
        return hfetchListWithSelector(keyGenerator, field, dataExtractor, cls, expireTimeInSecond, -1);
    }


    @Override
    public <T> Map<String, T> hfetchall(KeyGenerator keyGenerator, Class<T> cls) {
        return hfetchall(keyGenerator, null, cls);
    }

    @Override
    public <T> Map<String, T> hfetchall(KeyGenerator keyGenerator, Class<T> cls, int expireTimeInSecond) {
        return hfetchall(keyGenerator, null, cls, expireTimeInSecond);
    }

    @Override
    public <T> Map<String, T> hfetchall(KeyGenerator keyGenerator, DataExtractor<Map<String, T>> dataExtractor, Class<T> cls) {
        return hfetchall(keyGenerator, dataExtractor, cls, 0);
    }

    @Override
    public <T> T fetchWithSelector(KeyGenerator keyGenerator, Class<T> cls, int selector) {
        return fetchWithSelector(keyGenerator, null, cls, selector);
    }

    @Override
    public <T> T fetchWithSelector(KeyGenerator keyGenerator, DataExtractor<T> dataExtractor, Class<T> cls, int selector) {
        return fetchWithSelector(keyGenerator, dataExtractor, cls, 0, selector);
    }

    @Override
    public <T> T fetchWithSelector(KeyGenerator keyGenerator, Class<T> cls, int expireTimeInSecond, int selector) {
        return fetchWithSelector(keyGenerator, null, cls, expireTimeInSecond, selector);
    }

    @Override
    public <T> T fetchWithSelector(KeyGenerator keyGenerator, DataExtractor<T> dataExtractor, Class<T> cls, int expireTimeInSecond, int selector) {
        if (null == keyGenerator) {
            throw new NullPointerException("getKeyGenerator");
        }

        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (selector >= 0) {
                jedis.select(selector);
            }
            String value = jedis.get(key);

            if (null != value) {
                return serialization.deserialize(value, cls);
            }

            T fromStore = null;
            if (null != dataExtractor) {
                fromStore = dataExtractor.extract();
                expireTimeInSecond = dataExtractor.updateExpireTimeInSecond(fromStore, expireTimeInSecond);
            }

            if (null != fromStore) {
                jedis.set(key, serialization.serialize(fromStore));
                if (expireTimeInSecond > 0) {
                    jedis.expire(key, expireTimeInSecond);
                }
            }

            return fromStore;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> List<T> fetchListWithSelector(KeyGenerator keyGenerator, Class<T> cls, int selector) {
        return fetchListWithSelector(keyGenerator, null, cls, selector);
    }

    @Override
    public <T> List<T> fetchListWithSelector(KeyGenerator keyGenerator, DataExtractor<List<T>> dataExtractor, Class<T> cls, int selector) {
        return fetchListWithSelector(keyGenerator, dataExtractor, cls, 0, selector);
    }

    @Override
    public <T> List<T> fetchListWithSelector(KeyGenerator keyGenerator, Class<T> cls, int expireTimeInSecond, int selector) {
        return fetchListWithSelector(keyGenerator, null, cls, expireTimeInSecond, selector);
    }

    @Override
    public <T> List<T> fetchListWithSelector(KeyGenerator keyGenerator, DataExtractor<List<T>> dataExtractor, Class<T> cls, int expireTimeInSecond, int selector) {
        if (null == keyGenerator) {
            throw new NullPointerException("getKeyGenerator");
        }

        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (selector >= 0) {
                jedis.select(selector);
            }
            String value = jedis.get(key);

            if (null != value) {
                return serialization.deserializeList(value, cls);
            }

            List<T> fromStore = null;
            if (null != dataExtractor) {
                fromStore = dataExtractor.extract();
                expireTimeInSecond = dataExtractor.updateExpireTimeInSecond(fromStore, expireTimeInSecond);
            }

            if (null != fromStore) {
                jedis.set(key, serialization.serialize(fromStore));
                if (expireTimeInSecond > 0) {
                    jedis.expire(key, expireTimeInSecond);
                }
            }

            return fromStore;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> T hfetchWithSelector(KeyGenerator keyGenerator, String field, Class<T> cls, int selector) {
        return hfetchWithSelector(keyGenerator, field, null, cls, selector);
    }

    @Override
    public <T> T hfetchWithSelector(KeyGenerator keyGenerator, String field, DataExtractor<T> dataExtractor, Class<T> cls, int selector) {
        return hfetchWithSelector(keyGenerator, field, dataExtractor, cls, 0, selector);
    }

    @Override
    public <T> T hfetchWithSelector(KeyGenerator keyGenerator, String field, Class<T> cls, int expireTimeInSecond, int selector) {
        return hfetchWithSelector(keyGenerator, field, null, cls, expireTimeInSecond, selector);
    }

    @Override
    public <T> T hfetchWithSelector(KeyGenerator keyGenerator, String field, DataExtractor<T> dataExtractor, Class<T> cls, int expireTimeInSecond, int selector) {
        if (null == keyGenerator) {
            throw new NullPointerException("getKeyGenerator");
        }

        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }
        if (StringUtils.isBlank(field)) {
            throw new NullPointerException("field");
        }

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (selector >= 0) {
                jedis.select(selector);
            }
            String value = jedis.hget(key, field);

            if (null != value) {
                return serialization.deserialize(value, cls);
            }

            T fromStore = null;
            if (null != dataExtractor) {
                fromStore = dataExtractor.extract();
                expireTimeInSecond = dataExtractor.updateExpireTimeInSecond(fromStore, expireTimeInSecond);
            }

            if (null != fromStore) {
                jedis.hset(key, field, serialization.serialize(fromStore));
                if (expireTimeInSecond > 0) {
                    jedis.expire(key, expireTimeInSecond);
                }
            }

            return fromStore;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> List<T> hfetchListWithSelector(KeyGenerator keyGenerator, String field, Class<T> cls, int selector) {
        return hfetchListWithSelector(keyGenerator, field, cls, 0, selector);
    }

    @Override
    public <T> List<T> hfetchListWithSelector(KeyGenerator keyGenerator, String field, DataExtractor<List<T>> dataExtractor, Class<T> cls, int selector) {
        return hfetchListWithSelector(keyGenerator, field, dataExtractor, cls, 0, selector);
    }

    @Override
    public <T> List<T> hfetchListWithSelector(KeyGenerator keyGenerator, String field, Class<T> cls, int expireTimeInSecond, int selector) {
        return hfetchListWithSelector(keyGenerator, field, null, cls, expireTimeInSecond, selector);
    }

    @Override
    public <T> List<T> hfetchListWithSelector(KeyGenerator keyGenerator, String field, DataExtractor<List<T>> dataExtractor, Class<T> cls, int expireTimeInSecond, int selector) {
        if (null == keyGenerator) {
            throw new NullPointerException("getKeyGenerator");
        }

        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }
        if (StringUtils.isBlank(field)) {
            throw new NullPointerException("field");
        }

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (selector >= 0) {
                jedis.select(selector);
            }
            String value = jedis.hget(key, field);

            if (null != value) {
                return serialization.deserializeList(value, cls);
            }

            List<T> fromStore = null;
            if (null != dataExtractor) {
                fromStore = dataExtractor.extract();
                expireTimeInSecond = dataExtractor.updateExpireTimeInSecond(fromStore, expireTimeInSecond);
            }

            if (null != fromStore) {
                jedis.hset(key, field, serialization.serialize(fromStore));
                if (expireTimeInSecond > 0) {
                    jedis.expire(key, expireTimeInSecond);
                }
            }

            return fromStore;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> Map<String, T> hfetchallWithSelector(KeyGenerator keyGenerator, Class<T> cls, int selector) {
        return hfetchallWithSelector(keyGenerator, null, cls, selector);
    }

    @Override
    public <T> Map<String, T> hfetchallWithSelector(KeyGenerator keyGenerator, DataExtractor<Map<String, T>> dataExtractor, Class<T> cls, int selector) {
        return hfetchallWithSelector(keyGenerator, dataExtractor, cls, 0, selector);
    }

    @Override
    public <T> Map<String, T> hfetchallWithSelector(KeyGenerator keyGenerator, Class<T> cls, int expireTimeInSecond, int selector) {
        return hfetchallWithSelector(keyGenerator, null, cls, expireTimeInSecond, selector);
    }

    @Override
    public <T> Map<String, T> hfetchallWithSelector(KeyGenerator keyGenerator, DataExtractor<Map<String, T>> dataExtractor, Class<T> cls, int expireTimeInSecond, int selector) {
        if (null == keyGenerator) {
            throw new NullPointerException("getKeyGenerator");
        }

        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (selector >= 0) {
                jedis.select(selector);
            }
            Map<String, T> rst = Maps.newHashMap();
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
                        rst.put(k, serialization.deserialize(v, cls));
                    }
                }
            } while (! cursor.equals(startCursor));

            if (MapUtils.isNotEmpty(rst)) {
                return rst;
            }

            Map<String, T> fromStore = null;
            if (null != dataExtractor) {
                fromStore = dataExtractor.extract();
                expireTimeInSecond = dataExtractor.updateExpireTimeInSecond(fromStore, expireTimeInSecond);
            }

            if (null != fromStore) {
                Set<Map.Entry<String, T>> entries = fromStore.entrySet();
                for (Map.Entry<String, T> entry : entries) {
                    jedis.hset(key, entry.getKey(), serialization.serialize(entry.getValue()));
                }
                if (expireTimeInSecond > 0) {
                    jedis.expire(key, expireTimeInSecond);
                }
            }

            return fromStore;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public Long pfCount(KeyGenerator keyGenerator) {
        return pfCount(keyGenerator,-1);
    }

    @Override
    public Long pfCount(KeyGenerator keyGenerator, int expireTimeInSecond) {
        return pfCountWithSelector(keyGenerator,expireTimeInSecond,-1);
    }

    @Override
    public Long pfCountWithSelector(KeyGenerator keyGenerator, int selector) {
        return pfCountWithSelector(keyGenerator,-1,selector);
    }

    @Override
    public Long pfCountWithSelector(KeyGenerator keyGenerator, int expireTimeInSecond, int selector) {
        if (null == keyGenerator) {
            throw new NullPointerException("getKeyGenerator");
        }

        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (selector >= 0) {
                jedis.select(selector);
            }
            Long count = jedis.pfcount(key);
            if( null != count && count > 0 ) {
                if (expireTimeInSecond > 0) {
                    jedis.expire(key, expireTimeInSecond);
                }
            }
            return count;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> List<T> zfetchList(KeyGenerator keyGenerator, Class<T> cls) {
        return zfetchList(keyGenerator,cls,-1);
    }

    @Override
    public <T> List<T> zfetchList(KeyGenerator keyGenerator,  Class<T> cls, int expireTimeInSecond) {
        return zfetchListWithSelector(keyGenerator,cls,expireTimeInSecond,-1);
    }

    @Override
    public <T> List<T> zfetchListWithSelector(KeyGenerator keyGenerator, Class<T> cls, int selector) {
        return zfetchListWithSelector(keyGenerator,cls,-1,selector);
    }

    @Override
    public <T> List<T> zfetchListWithSelector(KeyGenerator keyGenerator, Class<T> cls, int expireTimeInSecond, int selector) {
        if (null == keyGenerator) {
            throw new NullPointerException("getKeyGenerator");
        }

        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (selector >= 0) {
                jedis.select(selector);
            }
            List rs = new ArrayList();
            String cursor = "0";
            ScanParams scanParams = new ScanParams().count(10000);
            do {
                ScanResult<Tuple> zscan = jedis.zscan(key, cursor,scanParams);
                cursor = zscan.getCursor();
                List<Tuple> result = zscan.getResult();
                if (CollectionUtils.isNotEmpty(result)) {
                    for (Tuple t : result) {
                        rs.add(serialization.deserialize(t.getElement(), cls));
                    }
                }
            }while (!"0".equals(cursor));

            if(rs.size()>0){
                if (expireTimeInSecond > 0) {
                    jedis.expire(key, expireTimeInSecond);
                }
            }

            return rs;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> List<DefaultKeyValue> zfetchAll(KeyGenerator keyGenerator, Class<T> cls) {
        return zfetchAll(keyGenerator,cls,-1);
    }

    @Override
    public <T> List<DefaultKeyValue> zfetchAll(KeyGenerator keyGenerator, Class<T> cls, int expireTimeInSecond) {
        return zfetchAllWithSelector(keyGenerator,cls,expireTimeInSecond,-1);
    }

    @Override
    public <T> List<DefaultKeyValue> zfetchAllWithSelector(KeyGenerator keyGenerator, Class<T> cls, int selector) {
        return zfetchAllWithSelector(keyGenerator,cls,-1,selector);
    }

    @Override
    public <T> List<DefaultKeyValue> zfetchAllWithSelector(KeyGenerator keyGenerator, Class<T> cls, int expireTimeInSecond, int selector) {
        if (null == keyGenerator) {
            throw new NullPointerException("getKeyGenerator");
        }

        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (selector >= 0) {
                jedis.select(selector);
            }
            List<DefaultKeyValue> rs = new ArrayList();
            String cursor = "0";
            ScanParams scanParams = new ScanParams().count(10000);
            do {
                ScanResult<Tuple> zscan = jedis.zscan(key, cursor,scanParams);
                cursor = zscan.getCursor();
                List<Tuple> result = zscan.getResult();
                if (CollectionUtils.isNotEmpty(result)) {
                    for (Tuple t : result) {
                        DefaultKeyValue kv = new DefaultKeyValue();
                        kv.setKey(serialization.deserialize(t.getElement(), cls));
                        kv.setValue(t.getScore());
                        rs.add(kv);
                    }
                }
            }while (!"0".equals(cursor));

            if(rs.size()>0){
                if (expireTimeInSecond > 0) {
                    jedis.expire(key, expireTimeInSecond);
                }
            }

            return rs;
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    /**
     * <p>
     *  Jedis 的hgetall实现，为了解决hgetall命令的性能问题。内部使用增量式迭代命令<code>hscan</code>来实现。
     * </p>
     * <b>增量式迭代命令只能对被返回的元素提供有限的保证 （offer limited guarantees about the returned elements）</b>
     * <p>
     *     举个例子， 使用 SMEMBERS 命令可以返回集合键当前包含的所有元素， 但是对于 SCAN 这类增量式迭代命令来说， 因为在对键进行增量式迭代的过程中， 键可能会被修改。
     * </p>
     * <p>
     *     因为增量式命令仅仅使用游标来记录迭代状态， 所以这些命令带有以下缺点：
     * </p>
     * <ul>
     * <li>
     *     同一个元素可能会被返回多次。 处理重复元素的工作交由应用程序负责， 比如说， 可以考虑将迭代返回的元素仅仅用于可以安全地重复执行多次的操作上。
     * </li>
     * <li>
     *     如果一个元素是在迭代过程中被添加到数据集的， 又或者是在迭代过程中从数据集中被删除的， 那么这个元素可能会被返回， 也可能不会， 这是未定义的（undefined）。
     * </li>
     * </ul>
     * <p>
     *     更多说明请参考：<a href="http://doc.redisfans.com/key/scan.html">http://doc.redisfans.com/key/scan.html</a>
     * </p>
     *
     * @param keyGenerator
     * @param dataExtractor
     * @param cls
     * @param expireTimeInSecond
     * @param <T>
     * @return
     */
    @Override
    public <T> Map<String, T> hfetchall(KeyGenerator keyGenerator, DataExtractor<Map<String, T>> dataExtractor, Class<T> cls, int expireTimeInSecond) {
        return hfetchallWithSelector(keyGenerator, dataExtractor, cls, expireTimeInSecond, -1);
    }

    @Override
    public boolean exists(KeyGenerator keyGenerator) {
        return existsWithSelector(keyGenerator, -1);
    }

    @Override
    public boolean existsWithSelector(KeyGenerator keyGenerator, int selector) {
        if (null == keyGenerator) {
            throw new NullPointerException("getKeyGenerator");
        }

        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (selector >= 0) {
                jedis.select(selector);
            }
            return jedis.exists(key);
        }
        finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> Set<T> sfetchAll(KeyGenerator keyGenerator, Class<T> cls) {
        return sfetchAll(keyGenerator, cls, -1);
    }

    @Override
    public <T> Set<T> sfetchAll(KeyGenerator keyGenerator, Class<T> cls, int selector) {
        if (null == keyGenerator) {
            throw new NullPointerException("keyGenerator");
        }
        String key = keyGenerator.generateKey();
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException("key");
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (selector >= 0) {
                jedis.select(selector);
            }
            Set<T> rs = Sets.newHashSet();
            String cursor = "0";
            ScanParams scanParams = new ScanParams().count(10000);
            do {
                ScanResult<String> scanResult = jedis.sscan(key, cursor, scanParams);
                cursor = scanResult.getCursor();
                List<String> result = scanResult.getResult();
                if (CollectionUtils.isNotEmpty(result)) {
                    for (String s : result) {
                        rs.add(serialization.deserialize(s, cls));
                    }
                }
            } while (!"0".equals(cursor));
            return rs;
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
}
