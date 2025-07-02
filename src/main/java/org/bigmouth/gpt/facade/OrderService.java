package org.bigmouth.gpt.facade;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <h3>类的基本描述</h3>
 *
 * @author hcmony
 * @since V1.0.0, 2019/10/15 10:48
 */
public interface OrderService {


    /**
     * 定时任务去查看支付结果
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryResults")
    public void queryResults();


    /**
     * 根据订单号查询状态
     * payType business orderNum  clientType 必传
     *
     * @return byte
     * @throws Exception
     */
    @RequestMapping("/queryOrder")
    public Byte findByOrder(@RequestBody PayOrder payOrder);

    /**
     * 根据订单号查询状态 ,此方法只支持查征信支付宝h5定单
     * @return byte
     * @throws Exception
     */
    @Deprecated
    @RequestMapping("/queryAll")
    public Byte findByOrderNum(@RequestParam("orderNum") String orderNum);

    @RequestMapping("/queryOrderByOrderId")
    public PayOrder findOrder(@RequestParam("orderId") String orderId);

}
