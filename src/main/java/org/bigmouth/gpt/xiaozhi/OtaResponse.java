package org.bigmouth.gpt.xiaozhi;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OtaResponse {
    private Mqtt mqtt;
    @JsonProperty("server_time")
    @JSONField(name = "server_time")
    private ServerTime serverTime;
    private Firmware firmware;
    private Activation activation;

    @Data
    public static class Mqtt {
        private String endpoint;
        @JsonProperty("client_id")
        @JSONField(name = "client_id")
        private String clientId;
        private String username;
        private String password;
        @JsonProperty("publish_topic")
        @JSONField(name = "publish_topic")
        private String publishTopic;
        @Deprecated
        @JsonProperty("subscribe_topic")
        @JSONField(name = "subscribe_topic")
        private String subscribeTopic;
    }

    @Data
    public static class ServerTime {
        private Long timestamp;
        private String timezone;
        @JsonProperty("timezone_offset")
        @JSONField(name = "timezone_offset")
        private Integer timezoneOffset;
    }

    @Data
    public static class Firmware {
        private String version;
        private String url;
    }

    @Data
    public static class Activation {
        private String message;
        private String code;
        private String challenge;
    }
}