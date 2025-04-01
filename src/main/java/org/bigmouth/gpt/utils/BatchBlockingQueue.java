package org.bigmouth.gpt.utils;

import com.bxm.warcar.utils.NamedThreadFactory;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * <p>基于 {@link BlockingQueue} 实现的批处理队列</p>
 * @author allen
 * @since 1.0.0
 */
public class BatchBlockingQueue<T> implements BatchQueue<T> {

    private static final int DEFAULT_INTERVAL_TIME = 1000;

    private final int batchSize;
    private final Consumer<List<T>> consumer;
    private final int intervalTimeInMillis;

    private final AtomicBoolean process = new AtomicBoolean(false);
    private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
    private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

    private final ThreadPoolExecutor purgeThread = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new NamedThreadFactory("purge"));

    public BatchBlockingQueue(int batchSize, Consumer<List<T>> consumer) {
        this(batchSize, consumer, DEFAULT_INTERVAL_TIME);
    }

    public BatchBlockingQueue(int batchSize, Consumer<List<T>> consumer, int intervalTimeInMillis) {
        this.batchSize = batchSize;
        this.consumer = consumer;
        this.intervalTimeInMillis = intervalTimeInMillis;
    }

    private boolean isProcessing() {
        return process.get();
    }

    @Override
    public Consumer<List<T>> getConsumer() {
        return this.consumer;
    }

    @Override
    public boolean add(T t) {
        boolean b = queue.add(t);
        if (!isProcessing() && b) {
            process.set(true);
            startDrain();
        }
        return b;
    }

    @Override
    public void asyncAdd(T t) {
        this.add(t);
    }

    private void startDrain() {
        boolean shutdown = purgeThread.isShutdown();
        boolean terminated = purgeThread.isTerminated();
        if (shutdown || terminated) {
            return;
        }
        purgeThread.submit(() -> {
            startTime.set(System.currentTimeMillis());

            for ( ;; ) {
                long last = System.currentTimeMillis() - startTime.get();
                if (queue.size() >= batchSize || (!queue.isEmpty() && last > intervalTimeInMillis)) {
                    doDrainWithBatchSize();
                } else if (queue.isEmpty()) {
                    process.set(false);
                    break;
                }
            }
        });
    }

    @Override
    public void drainAll() {
        while (!queue.isEmpty()) {
            doDrainWithBatchSize();
        }
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public void clear() {
        queue.clear();
    }

    private void doDrainWithBatchSize() {
        List<T> drained = Lists.newArrayList();
        if (queue.drainTo(drained, batchSize) > 0) {
            consumer.accept(drained);
            startTime.set(System.currentTimeMillis());
        }
    }

    @Override
    public void destroy() {
        this.clear();
        this.purgeThread.shutdownNow();
    }
}
