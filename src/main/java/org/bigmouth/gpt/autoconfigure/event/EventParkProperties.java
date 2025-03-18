package org.bigmouth.gpt.autoconfigure.event;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author allen
 * @date 2019/4/26
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "warcar.integration.eventbus")
public class EventParkProperties {

    private static final int CORE_SIZE = Runtime.getRuntime().availableProcessors() * 2 + 1;

    /**
     * 异步事件总线核心线程数大小
     */
    private int asyncEventBusPoolSize = CORE_SIZE;
    /**
     * 异步事件总线队列阻塞报警线
     */
    private int asyncEventBusAlarmBlockQueueSize = 100;

    public int getAsyncEventBusPoolSize() {
        return asyncEventBusPoolSize;
    }

    public void setAsyncEventBusPoolSize(int asyncEventBusPoolSize) {
        this.asyncEventBusPoolSize = asyncEventBusPoolSize;
    }

    public int getAsyncEventBusAlarmBlockQueueSize() {
        return asyncEventBusAlarmBlockQueueSize;
    }

    public void setAsyncEventBusAlarmBlockQueueSize(int asyncEventBusAlarmBlockQueueSize) {
        this.asyncEventBusAlarmBlockQueueSize = asyncEventBusAlarmBlockQueueSize;
    }
}
