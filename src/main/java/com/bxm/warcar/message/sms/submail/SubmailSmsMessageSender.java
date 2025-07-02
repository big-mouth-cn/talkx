package com.bxm.warcar.message.sms.submail;

import com.alibaba.fastjson.JSONObject;
import com.bxm.warcar.message.AbstractMessageSender;
import com.bxm.warcar.message.Message;
import com.bxm.warcar.message.MessageException;
import com.bxm.warcar.message.MessageSender;
import com.bxm.warcar.utils.JsonHelper;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * <h3>发送短信验证</h3>
 * <p></p>
 *
 * @author hcmony
 * @since V1.0.0, 2018/07/30 11:58
 */
public class SubmailSmsMessageSender extends AbstractMessageSender implements MessageSender {

    private static final String SUCCESS = "success";
    private final String appid;
    private final String appkey;
    private final String url;

    public SubmailSmsMessageSender(String appid, String appkey, String url) {
        this.appid = appid;
        this.appkey = appkey;
        this.url = url;
    }

    @Override
    public boolean send(Message message) {
        try {
            send2(message);
            return true;
        } catch (MessageException e) {
            return false;
        }
    }

    @Override
    public void send2(Message message) throws MessageException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("appid", appid);
        jsonParam.put("to", message.getTos().get(0));
        jsonParam.put("content", message.getContent());
        jsonParam.put("signature", appkey);

        ResponseEntity<String> responseEntity = postForEntity(url, jsonParam);
        SendResult body = JsonHelper.convert(responseEntity.getBody(), SendResult.class);
        if (null == body) {
            throw new MessageException("result is null!");
        }

        if (!SUCCESS.equals(body.getStatus())) {
            throw new MessageException(JSONObject.toJSONString(body), body.getStatus());
        }
    }

    public static class SendResult implements Serializable {

        private static final long serialVersionUID = -9083143317071986607L;
        private String status;
        private String send_id;
        private int fee;
        private String sms_credits;
        private String transactional_sms_credits;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSend_id() {
            return send_id;
        }

        public void setSend_id(String send_id) {
            this.send_id = send_id;
        }

        public int getFee() {
            return fee;
        }

        public void setFee(int fee) {
            this.fee = fee;
        }

        public String getSms_credits() {
            return sms_credits;
        }

        public void setSms_credits(String sms_credits) {
            this.sms_credits = sms_credits;
        }

        public String getTransactional_sms_credits() {
            return transactional_sms_credits;
        }

        public void setTransactional_sms_credits(String transactional_sms_credits) {
            this.transactional_sms_credits = transactional_sms_credits;
        }
    }
}
