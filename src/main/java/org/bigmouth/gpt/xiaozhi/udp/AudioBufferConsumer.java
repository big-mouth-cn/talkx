package org.bigmouth.gpt.xiaozhi.udp;

import java.util.List;
import java.util.function.Consumer;

public interface AudioBufferConsumer extends Consumer<List<byte[]>> {

    void setContext(UdpClientContext context);
}
