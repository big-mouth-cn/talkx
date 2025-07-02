package org.bigmouth.gpt.ai.entity;

import java.io.IOException;

/**
 * @author allen
 * @date 2023/5/8
 * @since 1.0.0
 */
public interface ByteWriter<T> {

    /**
     * 执行
     *
     * @throws IOException 异常
     */
    void write(T bytes) throws IOException;
}
