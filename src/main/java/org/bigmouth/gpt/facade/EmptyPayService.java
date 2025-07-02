package org.bigmouth.gpt.facade;

import org.springframework.stereotype.Service;

/**
 * @author Allen Hu
 * @date 2025/4/24
 */
@Service
public class EmptyPayService implements PayService {
    @Override
    public String pay(PayOrder payOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close(PayOrder payOrder) {
        throw new UnsupportedOperationException();
    }
}
