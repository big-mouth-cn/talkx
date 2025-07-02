package org.bigmouth.gpt.integration;

import org.bigmouth.gpt.facade.PayOrder;
import org.bigmouth.gpt.facade.PayService;
import org.springframework.context.annotation.Configuration;

/**
 * @author huxiao
 * @date 2023/11/3
 * @since 1.0.0
 */
@Configuration
public class PayIntegration {

    private final PayService payService;

    public PayIntegration(PayService payService) {
        this.payService = payService;
    }

    public String pay(PayOrder payOrder) {
        return payService.pay(payOrder);
    }

    public void close(PayOrder payOrder) {
        payService.close(payOrder);
    }
}