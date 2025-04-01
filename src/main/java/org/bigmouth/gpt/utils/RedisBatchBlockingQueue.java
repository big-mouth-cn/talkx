package org.bigmouth.gpt.utils;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author gpt-4o
 * @author Allen Hu
 * @date 2025/3/31
 */
@Slf4j
public class RedisBatchBlockingQueue<T> implements BatchQueue<T> {

    private final JedisPool jedisPool;
    private final String queueKey;
    private final int batchSize;
    private final Consumer<List<T>> consumer;
    private final int intervalTimeInMillis;
    private final ThreadPoolExecutor purgeThread;
    private final BlockingQueue<T> asyncQueue;
    private final ThreadPoolExecutor asyncQueueConsumer;
    private final Function<T, String> serializer;
    private final Function<String, T> deserializer;
    private final AtomicBoolean stop = new AtomicBoolean(false);

    public RedisBatchBlockingQueue(JedisPool jedisPool, String queueKey, int batchSize, Consumer<List<T>> consumer, int intervalTimeInMillis, Function<T, String> serializer, Function<String, T> deserializer) {
        this.jedisPool = jedisPool;
        this.queueKey = queueKey;
        this.batchSize = batchSize;
        this.consumer = consumer;
        this.intervalTimeInMillis = intervalTimeInMillis;
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.purgeThread = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.asyncQueue = new LinkedBlockingQueue<>();
        this.asyncQueueConsumer = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        startDrain();
        this.startAsyncQueueConsumerWork();
    }

    private void startDrain() {
        purgeThread.submit(() -> {
            while (true) {
                if (stop.get()) {
                    this.clear();
                    break;
                }
                try (Jedis jedis = jedisPool.getResource()) {
                    List<String> elements = jedis.lrange(queueKey, 0, batchSize - 1);
                    if (!elements.isEmpty()) {
                        jedis.ltrim(queueKey, elements.size(), -1);
                        List<T> apply = elements.stream().map(deserializer).collect(Collectors.toList());
                        consumer.accept(apply);
                    } else {
                        try {
                            Thread.sleep(intervalTimeInMillis);
                        } catch (InterruptedException ignored) {
                        }
                    }
                } catch (Exception e) {
                    log.error("Error during batch processing", e);
                }
            }
        });
    }

    private void startAsyncQueueConsumerWork() {
        asyncQueueConsumer.submit(() -> {
            while (true) {
                try {
                    T take = asyncQueue.take();
                    this.add(take);
                } catch (InterruptedException ignored) {
                }
            }
        });
    }

    @Override
    public Consumer<List<T>> getConsumer() {
        return this.consumer;
    }

    @Override
    public void destroy() {
        this.stop.set(true);
        this.clear();
        this.asyncQueue.clear();
        this.purgeThread.shutdownNow();
        this.asyncQueueConsumer.shutdownNow();
    }

    @Override
    public boolean add(T t) {
        try (Jedis jedis = jedisPool.getResource()) {
            String string = serializer.apply(t);
            jedis.rpush(queueKey, string);
            jedis.expire(queueKey, 3600L);
            return true;
        } catch (Exception e) {
            log.error("Error adding element to Redis queue", e);
            return false;
        }
    }

    @Override
    public void asyncAdd(T t) {
        if (!asyncQueue.offer(t)) {
            log.warn("Failed to add element to async queue");
        }
    }

    @Override
    public void drainAll() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> elements;
            while (!(elements = jedis.lrange(queueKey, 0, batchSize - 1)).isEmpty()) {
                jedis.ltrim(queueKey, elements.size(), -1);
                List<T> apply = elements.stream().map(deserializer).collect(Collectors.toList());
                consumer.accept(apply);
            }
        } catch (Exception e) {
            log.error("Error draining all elements from Redis queue", e);
        }
    }

    @Override
    public int size() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.llen(queueKey).intValue();
        } catch (Exception e) {
            log.error("Error getting size of Redis queue", e);
            return 0;
        }
    }

    @Override
    public void clear() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(queueKey);
        } catch (Exception e) {
            log.error("Error clearing Redis queue", e);
        }
    }
}
