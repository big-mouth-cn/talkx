package org.bigmouth.gpt.xiaozhi.tts.talkx;

import org.bigmouth.gpt.xiaozhi.forest.TalkXApi;
import org.bigmouth.gpt.xiaozhi.tts.TtsPlatformType;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintRequest;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintResult;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintService;
import org.springframework.context.annotation.Configuration;

/**
 * @author Allen Hu
 * @date 2025/4/18
 */
@Configuration
public class TalkXVoiceReprintService implements VoiceReprintService {

    private final TalkXApi talkXApi;

    public TalkXVoiceReprintService(TalkXApi talkXApi) {
        this.talkXApi = talkXApi;
    }

    @Override
    public TtsPlatformType of() {
        return TtsPlatformType.TalkX;
    }

    @Override
    public VoiceReprintResult reprint(VoiceReprintRequest request) {
        return talkXApi.reprint(request);
    }
}
