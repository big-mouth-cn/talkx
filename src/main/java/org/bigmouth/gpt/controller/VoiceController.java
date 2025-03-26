package org.bigmouth.gpt.controller;

import com.bxm.warcar.utils.UUIDHelper;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.ai.Speech2TextService;
import org.bigmouth.gpt.ai.Text2SpeechService;
import org.bigmouth.gpt.ai.entity.ApiKey;
import org.bigmouth.gpt.ai.entity.voice.Speech2TextArgument;
import org.bigmouth.gpt.ai.entity.voice.Text2SpeechArgument;
import org.bigmouth.gpt.ai.entity.voice.Tts1Request;
import org.bigmouth.gpt.entity.GetAccessKeyParam;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.exceptions.AiStatusException;
import org.bigmouth.gpt.interceptor.ContextFactory;
import org.bigmouth.gpt.service.AccessKeyService;
import org.bigmouth.gpt.service.IAiModelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * @author huxiao
 * @date 2023/11/10
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/voice")
public class VoiceController {

    private final Text2SpeechService text2SpeechService;
    private final Speech2TextService speech2TextService;
    private final AccessKeyService accessKeyService;
    private final IAiModelService aiModelService;

    public VoiceController(Text2SpeechService text2SpeechService, Speech2TextService speech2TextService, AccessKeyService accessKeyService, IAiModelService aiModelService) {
        this.text2SpeechService = text2SpeechService;
        this.speech2TextService = speech2TextService;
        this.accessKeyService = accessKeyService;
        this.aiModelService = aiModelService;
    }

    @GetMapping(value = "/text_to_speech_stream")
    public ResponseEntity<StreamingResponseBody> text2speechStream(@Validated Tts1Req req,
                                                                   HttpServletRequest httpServletRequest,
                                                                   HttpServletResponse httpServletResponse) {
        MediaType mediaType = MediaType.asMediaType(req.getMimeType());

        String range = httpServletRequest.getHeader("Range");
        if (StringUtils.equals("bytes=0-1", range)) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("accept-ranges", "bytes");
            headers.add("Content-Range", "bytes 0-1/*");
            return ResponseEntity.status(HttpStatus.OK).contentType(mediaType).headers(headers).build();
        }

        StreamingResponseBody body = outputStream -> {
            User user = ContextFactory.getLoginUser();

            Set<ApiKey> excludes = Sets.newHashSet();
            int retry = 0;
            while (true) {
                ApiKey available = null;
                try {
                    available = getApiKeyThrowable(user, excludes);
                    text2SpeechService.speech(Text2SpeechArgument.builder()
                            .id(UUIDHelper.generate())
                            .user(user)
                            .tts1(req.getTts1())
                            .apiKey(available)
                            .outputStream(outputStream)
                            .completeRunnable(() -> IOUtils.closeQuietly(outputStream))
                            .clientAbortExceptionStringBiConsumer((e, s) -> IOUtils.closeQuietly(outputStream))
                            .build());
                    break;
                } catch (Exception e) {
                    if (retry >= 1) {
                        throw e;
                    }
                    if ((e instanceof AiStatusException)) {
                        throw e;
                    }
                    if (Objects.nonNull(available)) {
                        if (!available.isUserPrivate()) {
                            excludes.add(available);
                        }
                    }
                    retry++;
                }
            }
        };
        HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.ok().headers(headers).contentType(mediaType).body(body);
    }

    @PostMapping(value = "/text_to_speech")
    public ResponseEntity<Object> text2speech(@RequestBody @Validated Tts1Req req) {
        User user = ContextFactory.getLoginUser();

        Set<ApiKey> excludes = Sets.newHashSet();
        int retry = 0;
        while (true) {
            ApiKey available = null;
            try {
                available = getApiKeyThrowable(user, excludes);
                byte[] speech = text2SpeechService.speech(Text2SpeechArgument.builder()
                        .id(UUIDHelper.generate())
                        .user(user)
                        .tts1(req.getTts1())
                        .apiKey(available)
                        .build());
                return ResponseEntity.ok().contentType(MediaType.asMediaType(req.getMimeType())).body(speech);
            } catch (Exception e) {
                if (retry >= 1) {
                    throw e;
                }
                if ((e instanceof AiStatusException)) {
                    throw e;
                }
                if (Objects.nonNull(available)) {
                    if (!available.isUserPrivate()) {
                        excludes.add(available);
                    }
                }
                retry++;
            }
        }
    }

    @PostMapping(value = "/speech_to_text")
    public ResponseEntity<String> speech2Text(@RequestParam("file") MultipartFile file) throws IOException {
        User user = ContextFactory.getLoginUser();

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        byte[] bytes = file.getBytes();

        Set<ApiKey> excludes = Sets.newHashSet();
        int retry = 0;
        while (true) {
            ApiKey available = null;
            try {
                available = getApiKeyThrowable(user, excludes);
                String transcriptions = speech2TextService.transcriptions(Speech2TextArgument.builder()
                        .id(UUIDHelper.generate())
                        .user(user)
                        .originalFilename(originalFilename)
                        .contentType(contentType)
                        .bytes(bytes)
                        .apiKey(available)
                        .build());
                if (Objects.isNull(transcriptions)) {
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(transcriptions);
            } catch (Exception e) {
                if (retry >= 1) {
                    throw e;
                }
                if ((e instanceof AiStatusException)) {
                    throw e;
                }
                if (Objects.nonNull(available)) {
                    if (!available.isUserPrivate()) {
                        excludes.add(available);
                    }
                }
                retry++;
            }
        }
    }

    private ApiKey getApiKeyThrowable(User user, Set<ApiKey> excludes) {
        GetAccessKeyParam param = new GetAccessKeyParam().setUser(user);
        ApiKey available = accessKeyService.getAvailable(param);
        String accessKey = available.getApiKey();
        if (StringUtils.isBlank(accessKey)) {
            if (!CollectionUtils.isEmpty(excludes)) {
                return excludes.iterator().next();
            }
            throw new AiStatusException("当前模型的密钥不存在或已经失效了。请5分钟后再试。");
        }
        return available;
    }

    @Data
    public static class Tts1Req {
        @NotNull private Tts1Request tts1;

        public MimeType getMimeType() {
            MimeType defaultMpeg = MimeType.valueOf("audio/mpeg");
            Tts1Request tts1Request = getTts1();
            if (Objects.isNull(tts1Request)) {
                return defaultMpeg;
            }
            String responseFormat = tts1Request.getResponseFormat();
            if (StringUtils.equals(responseFormat, "opus")) {
                return MimeType.valueOf("audio/opus");
            } else if (StringUtils.equals(responseFormat, "mp3")) {
                return defaultMpeg;
            } else if (StringUtils.equals(responseFormat, "aac")) {
                return MimeType.valueOf("audio/aac");
            } else if (StringUtils.equals(responseFormat, "flac")) {
                return MimeType.valueOf("audio/flac");
            }
            return defaultMpeg;
        }
    }
}
