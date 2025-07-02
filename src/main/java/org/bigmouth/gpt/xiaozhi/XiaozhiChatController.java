package org.bigmouth.gpt.xiaozhi;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bigmouth.gpt.xiaozhi.asr.AsrService;
import org.bigmouth.gpt.xiaozhi.asr.AsrServiceFactory;
import org.bigmouth.gpt.xiaozhi.asr.AsrType;
import org.bigmouth.gpt.xiaozhi.entity.asr.TalkXAsrRequest;
import org.bigmouth.gpt.xiaozhi.tts.TtsPlatformType;
import org.bigmouth.gpt.xiaozhi.tts.TtsService;
import org.bigmouth.gpt.xiaozhi.tts.TtsServiceFactory;
import org.bigmouth.gpt.xiaozhi.udp.UdpClientContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Allen Hu
 * @date 2025/3/21
 */
@Slf4j
@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiChatController implements DisposableBean {

    private final AsrService asrService;
    private final TtsServiceFactory ttsServiceFactory;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public XiaozhiChatController(AsrServiceFactory asrServiceFactory, TtsServiceFactory ttsServiceFactory) {
        this.asrService = asrServiceFactory.get(AsrType.SENSEVOICE_FASTAPI);
        this.ttsServiceFactory = ttsServiceFactory;
    }

    @PostMapping("/asr")
    public ResponseEntity<String> asr(@RequestBody @Validated TalkXAsrRequest request) {
        try {
            String key = request.getKey();
            String audioBase64 = request.getAudioBase64();
            byte[] bytes = Base64.decodeBase64(audioBase64);
            String result = asrService.asr(key, bytes);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("asr error", e);
            throw new IllegalStateException("asr error");
        }
    }

    @PostMapping("/tts")
    public ResponseEntity<byte[]> tts(@RequestBody @Validated TtsRequest request) {
        String sessionId = request.getSessionId();
        TtsPlatformType ttsPlatformType = request.getTtsPlatformType();
        String voiceModel = request.getVoiceModel();
        String voiceRole = request.getVoiceRole();
        TtsService ttsService = ttsServiceFactory.createInstance(sessionId, ttsPlatformType, voiceModel, voiceRole);
        ttsService.init();
        List<byte[]> res = Lists.newArrayList();
        ttsService.stream(request.getText(), res::add);
        ttsService.destroy();
        return ResponseEntity.ok(UdpClientContext.mergeByteArrays(res));
    }

    @PostMapping("/tts_sse")
    public ResponseEntity<StreamingResponseBody> ttsSse(@RequestBody @Validated TtsRequest request) throws ExecutionException {
        String sessionId = request.getSessionId();
        TtsPlatformType ttsPlatformType = request.getTtsPlatformType();
        String voiceModel = request.getVoiceModel();
        String voiceRole = request.getVoiceRole();

        TtsService ttsService = ttsServiceFactory.createInstance(sessionId, ttsPlatformType, voiceModel, voiceRole);
        ttsService.init();

        StreamingResponseBody body = outputStream -> {
            ttsService.stream(request.getText(), bytes -> {
                try {
                    log.info("write data: {} for {}", bytes.length, request.getText());
                    outputStream.write(bytes);
                } catch (Exception e) {
                    log.error("ttsSse error {}", e.getMessage(), e);
                }
            });
        };
        ttsService.destroy();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE).body(body);
    }

    @Override
    public void destroy() throws Exception {
        executor.shutdownNow();
    }

    @Data
    public static class TtsRequest {

        private String sessionId;
        /**
         * 平台
         */
        private TtsPlatformType ttsPlatformType;
        /**
         * 合成大模型
         */
        private String voiceModel;
        /**
         * 音色角色
         */
        private String voiceRole;
        /**
         * 文本
         */
        private String text;
    }
}
