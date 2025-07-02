package org.bigmouth.gpt.facade;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <h3>类的基本描述</h3>
 *
 * @author hcmony
 * @since V1.0.0, 2019/10/15 10:49
 */
public interface PayService {

    /**
     * 支付渠道 payType 1：微信，2:支付宝
     * 支付环境 clientType，0：h5，1：app，2：微信，3：QQ, 4:PC , 5:支付宝
     * 业务 business 1：征信
     * 支付完成通知地址： notifyUrl
     * @param payOrder
     * @return
     */
    @RequestMapping("/pay")
    String pay(@RequestBody PayOrder payOrder);


    /**
     * 关闭
     * @param payOrder
     * @return
     */
    @RequestMapping("/close")
    void close(@RequestBody PayOrder payOrder);
}
