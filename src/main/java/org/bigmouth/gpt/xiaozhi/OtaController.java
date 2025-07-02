package org.bigmouth.gpt.xiaozhi;

import com.bxm.warcar.utils.Constants;
import com.bxm.warcar.utils.JsonHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.entity.Device;
import org.bigmouth.gpt.service.IDeviceService;
import org.bigmouth.gpt.service.impl.DeviceServiceImpl;
import org.bigmouth.gpt.xiaozhi.aliyun.Tools;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiMqttConfig;
import org.bigmouth.gpt.xiaozhi.mqtt.FrontEndHolder;
import org.bigmouth.gpt.xiaozhi.mqtt.MqttService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Allen Hu
 * @date 2025/2/20
 */
@Slf4j
@RestController
@RequestMapping("/xiaozhi")
public class OtaController {

    private final XiaozhiMqttConfig xiaozhiMqttConfig;
    private final IDeviceService deviceService;
    private final MqttService mqttService;
    private final FrontEndHolder frontEndHolder;

    public OtaController(XiaozhiMqttConfig xiaozhiMqttConfig, IDeviceService deviceService, MqttService mqttService, FrontEndHolder frontEndHolder) {
        this.xiaozhiMqttConfig = xiaozhiMqttConfig;
        this.deviceService = deviceService;
        this.mqttService = mqttService;
        this.frontEndHolder = frontEndHolder;
    }

    @PostMapping("/ota")
    public ResponseEntity<String> ota(@RequestBody @Validated OtaRequest request,
                                      @RequestHeader("Device-Id") String deviceId,
                                      @RequestHeader(value = "Client-Id", required = false) String clientId) {
        String macAddress = request.getMacAddress();
        OtaResponse response = new OtaResponse();

        // 设置MQTT配置
        OtaResponse.Mqtt mqtt = mqttService.create(macAddress);
        if (Objects.isNull(mqtt)) {
            throw new IllegalStateException("MQTT Server create fail!");
        }
        response.setMqtt(mqtt);

        // 设置服务器时间
        OtaResponse.ServerTime serverTime = new OtaResponse.ServerTime();
        serverTime.setTimestamp(Instant.now().toEpochMilli());
        serverTime.setTimezone("Asia/Shanghai");
        serverTime.setTimezoneOffset(ZoneId.of("Asia/Shanghai").getRules().getOffset(Instant.now()).getTotalSeconds() / 60);
        response.setServerTime(serverTime);

        // 设置固件信息
        OtaResponse.Firmware firmware = new OtaResponse.Firmware();
        firmware.setVersion(request.getApplication().getVersion());
        firmware.setUrl("");
        response.setFirmware(firmware);

        Device device = deviceService.find(macAddress);
        if (device == null) {
            OtaResponse.Activation activation = new OtaResponse.Activation();
            String bindCode = deviceService.createBindCode(new OtaBindInf().setRequest(request).setResponse(response));
            activation.setMessage(bindCode);
            activation.setCode(bindCode);
            activation.setChallenge(UUID.randomUUID().toString());

            response.setActivation(activation);
        } else {
            // 只有当激活了才检查固件信息
            boolean isOtaUpdate = Objects.equals(Constants.YES, device.getOtaUpdate());
            if (isOtaUpdate) {
                // TODO
            }
        }

        frontEndHolder.remove(mqtt.getClientId());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonHelper.convert(response));
    }

    @PostMapping("/ota/activate")
    public ResponseEntity<String> activate(@RequestBody @Validated String json,
                                           @RequestHeader("Device-Id") String deviceId,
                                           @RequestHeader(value = "Client-Id", required = false) String clientId) {
        Device device = deviceService.find(deviceId);
        if (Objects.isNull(device)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("success");
    }

    @PostMapping("/ota/mqtt")
    public ResponseEntity<OtaResponse.Mqtt> mqttResponseEntity(@RequestBody @Validated OtaMqttRequest request) throws NoSuchAlgorithmException, InvalidKeyException {
        String macAddress = request.getClientMacAddress();
        String frontEndClientId = request.getFrontEndClientId();
        OtaResponse.Mqtt mqtt = getMqttClientConfig(macAddress);
        String clientId = mqtt.getClientId();
        String p2pTopic = xiaozhiMqttConfig.createP2pTopic(clientId);
        frontEndHolder.save(clientId, frontEndClientId);

        log.info("mqttResponseEntity macAddress: {}, frontEndClientId: {}, p2pTopic: {}", macAddress, frontEndClientId, p2pTopic);
        return ResponseEntity.ok(mqtt);
    }

    @PostMapping("/ota/mqtt_forward_connect")
    public ResponseEntity<ForwardConnectResponse> mqttForwardConnect(@RequestBody @Validated ForwardConnectRequest request) throws NoSuchAlgorithmException, InvalidKeyException {
        String clientId = request.getClientId();
        ForwardConnectResponse response = new ForwardConnectResponse();
        response.setEndpoint(xiaozhiMqttConfig.getEndpoint());
        response.setServerUrl(xiaozhiMqttConfig.getServerUrl());
        response.setClientId(clientId);
        response.setUsername("Signature|" + xiaozhiMqttConfig.getAccessKey() + "|" + xiaozhiMqttConfig.getInstanceId());
        response.setPassword(Tools.macSignature(clientId, xiaozhiMqttConfig.getSecretKey()));
        log.info("mqttForwardConnect response={}", response);
        return ResponseEntity.ok(response);
    }

    @NotNull
    private OtaResponse.Mqtt getMqttClientConfig(String macAddress) throws InvalidKeyException, NoSuchAlgorithmException {
        OtaResponse.Mqtt mqtt = new OtaResponse.Mqtt();
        mqtt.setEndpoint(xiaozhiMqttConfig.getEndpoint());
        String clientIdSuffix = DeviceServiceImpl.parse2ClientIdSuffix(macAddress);
        String clientId = deviceService.generateClientId(macAddress);
        mqtt.setClientId(clientId);
        mqtt.setUsername("Signature|" + xiaozhiMqttConfig.getAccessKey() + "|" + xiaozhiMqttConfig.getInstanceId());
        mqtt.setPassword(Tools.macSignature(clientId, xiaozhiMqttConfig.getSecretKey()));
        mqtt.setPublishTopic(xiaozhiMqttConfig.getTopicOfServer());
        mqtt.setSubscribeTopic(xiaozhiMqttConfig.getTopicOfDevice() + "/" + clientIdSuffix);
        return mqtt;
    }

    @Data
    public static class OtaMqttRequest {
        @NotBlank
        private String clientMacAddress;
        @NotBlank
        private String frontEndClientId;
    }

    @Data
    public static class ForwardConnectRequest {
        private String macAddress;
        @NotBlank
        private String clientId;
    }

    @Data
    public static class ForwardConnectResponse {
        private String endpoint;
        private String serverUrl;
        private String clientId;
        private String username;
        private String password;
    }
}
