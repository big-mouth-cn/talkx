package org.bigmouth.gpt.facade;

import org.springframework.stereotype.Service;

/**
 * @author Allen Hu
 * @date 2025/4/24
 */
@Service
public class EmptyOrderService implements OrderService {
    @Override
    public void queryResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Byte findByOrder(PayOrder payOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Byte findByOrderNum(String orderNum) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PayOrder findOrder(String orderId) {
        throw new UnsupportedOperationException();
    }
}
