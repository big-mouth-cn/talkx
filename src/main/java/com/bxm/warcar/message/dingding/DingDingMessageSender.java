package com.bxm.warcar.message.dingding;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bxm.warcar.message.AbstractMessageSender;
import com.bxm.warcar.message.Message;
import com.bxm.warcar.message.MessageException;
import com.bxm.warcar.message.MessageSender;
import com.bxm.warcar.utils.JsonHelper;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author allen
 * @since 1.0.0
 */
public class DingDingMessageSender extends AbstractMessageSender implements MessageSender {

    private final String url;

    public DingDingMessageSender(String url) {
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

        Map<String, Object> text = new HashMap<>();
        text.put("content", message.getContent());

        Map<String, Object> at = new HashMap<>();
        at.put("atMobiles", message.getTos());
        at.put("isAtAll", false);

        jsonParam.put("msgtype", "text");
        jsonParam.put("text", text);
        jsonParam.put("at", at);
        ResponseEntity<String> responseEntity = postForEntity(url,jsonParam);

        String response = responseEntity.getBody();
        if (null == response) {
            throw new MessageException("Return body has be empty!");
        }
        SendResult body = null;
        try {
            body = JsonHelper.convert(response, SendResult.class);
        } catch (JSONException e) {
            throw new MessageException("Json syntax error!");
        }
        if (null == body) {
            throw new MessageException("Illegal body object!");
        }

        if (!body.isSucceed()) {
            throw new MessageException(body.getErrmsg(), String.valueOf(body.getErrcode()));
        }
    }

    public static class SendResult implements Serializable {

        private static final long serialVersionUID = -9083143317071986607L;
        private int errcode = -1;
        private String errmsg;

        public boolean isSucceed() {
            return errcode == 0;
        }

        public int getErrcode() {
            return errcode;
        }

        public void setErrcode(int errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }
    }
}
