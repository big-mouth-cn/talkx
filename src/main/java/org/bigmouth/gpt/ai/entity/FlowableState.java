package org.bigmouth.gpt.ai.entity;

/**
 * @author Allen Hu
 * @date 2025/4/22
 */
public class FlowableState {

    /**
     * 接收到第一个数据的时间
     */
    private long firstByteTimeInNanoTime;
    private Throwable throwable;
    private StringBuilder completion = new StringBuilder();

    public void append(String str) {
        completion.append(str);
    }

    public void initFirstByteTimeInNanoTime() {
        if (this.firstByteTimeInNanoTime == 0) {
            this.firstByteTimeInNanoTime = System.nanoTime();
        }
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isOccurError() {
        return throwable != null;
    }

    public StringBuilder getCompletion() {
        return completion;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public long getFirstByteTimeInNanoTime() {
        return firstByteTimeInNanoTime;
    }
}
