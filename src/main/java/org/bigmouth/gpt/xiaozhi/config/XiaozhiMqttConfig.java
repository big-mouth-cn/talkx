package org.bigmouth.gpt.xiaozhi.config;

import lombok.Data;
import org.bigmouth.gpt.xiaozhi.mqtt.MqttType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Allen Hu
 * @date 2025/3/17
 */
@Data
@ConfigurationProperties(prefix = "xiaozhi.mqtt")
public class XiaozhiMqttConfig {

    /**
     * 设置MQTT类型
     */
    private MqttType type = MqttType.TALKX;

    private String talkxMqttTopicOfServer = "forward-server";
    private String talkxMqttTopicOfP2p = "forwards";
    private String talkxMqttClientIdPrefixForDevice = "GID_talkxclient@@@";

    /**
     * 您在控制台创建的云消息队列 MQTT 版的实例ID。
     */
    private String instanceId = "";
    /**
     * MQTT 接入点
     */
    private String endpoint = "";
    /**
     * MQTT Cloud 管理端口
     */
    private int cloudPort = 5672;
    /**
     * AccessKey ID，阿里云身份验证，在阿里云RAM控制台创建。
     * 阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
     * 强烈建议不要把AccessKey ID和AccessKey Secret保存到工程代码里，否则可能导致AccessKey泄露，威胁您账号下所有资源的安全。
     * 本示例以将AccessKey 和 AccessKeySecret 保存在环境变量为例说明。
     */
    private String accessKey = "";
    /**
     * AccessKey Secret，阿里云身份验证，在阿里云RAM控制台创建。仅在签名鉴权模式下需要设置。
     */
    private String secretKey = "";
    /**
     * 设备客户端ID前缀，客户端ID由前缀和@@@后缀组成
     */
    private String clientIdPrefixForDevice = "GID_test@@@";
    /**
     * 服务端主题，客户端向服务端上报时使用的
     */
    private String topicOfServer = "device-server";
    /**
     * 客户端主题，服务端向客户端下发时使用的，这个只是parent topic，实际使用的时候需要加上 /p2p/GID_test@@@XXX
     */
    private String topicOfDevice = "devices";

    public String getServerUrl() {
        return "ssl://" + endpoint + ":8883";
    }

    public String createP2pTopic(String clientId) {
        // devices/p2p/GID_test@@@a0_85_e3_e1_55_34
        String topicOfDevice = getTopicOfDevice();
        return String.format("%s/p2p/%s", topicOfDevice, clientId);
    }

    public String createFrontP2pClient(String clientId) {
        // forwards/p2p/GID_talkxclient@@@a0_85_e3_e1_55_34
        String topicOfForward = getTalkxMqttTopicOfP2p();
        return String.format("%s/p2p/%s", topicOfForward, clientId);
    }
}
