package org.bigmouth.gpt.xiaozhi;

import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintRequest;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintResult;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintServiceFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Allen Hu
 * @date 2025/4/18
 */
@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiVoiceReprintApiController {

    private final VoiceReprintServiceFactory voiceReprintServiceFactory;

    public XiaozhiVoiceReprintApiController(VoiceReprintServiceFactory voiceReprintServiceFactory) {
        this.voiceReprintServiceFactory = voiceReprintServiceFactory;
    }

    @PostMapping("/voice/reprint")
    public ResponseEntity<VoiceReprintResult> voiceReprint(@RequestBody VoiceReprintRequest request) {
        return ResponseEntity.ok(voiceReprintServiceFactory.get().reprint(request));
    }
}
