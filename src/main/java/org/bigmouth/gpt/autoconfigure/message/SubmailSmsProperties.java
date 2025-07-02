package org.bigmouth.gpt.autoconfigure.message;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Allen Hu
 * @date 2025/3/27
 */
@ConfigurationProperties("config.sms.submail")
public class SubmailSmsProperties {

    private String appid;
    private String appkey;
    private String url = "https://api.mysubmail.com/message/send.json";

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAppid() {
        return appid;
    }

    public String getAppkey() {
        return appkey;
    }

    public String getUrl() {
        return url;
    }
}
