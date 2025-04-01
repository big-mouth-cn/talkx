package org.bigmouth.gpt.utils;

import java.util.List;
import java.util.function.Consumer;

public interface BatchQueue<T> {

    /**
     * @return 返回队列消费的函数
     */
    Consumer<List<T>> getConsumer();

    /**
     * 添加一个对象到队列中
     * @param t 对象
     * @return 返回true添加成功。否则添加失败，表示队列可能满了
     */
    boolean add(T t);

    /**
     * 异步添加一个对象到队列中
     * @param t 对象
     */
    void asyncAdd(T t);

    /**
     * 主动清理当前队列所有的对象
     */
    void drainAll();

    /**
     * @return 返回当前队列长度
     */
    int size();

    /**
     * 清空队列
     */
    void clear();

    /**
     * 销毁
     */
    void destroy();
}
