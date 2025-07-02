package org.bigmouth.gpt.xiaozhi.tts;

public interface VoiceReprintService {

    TtsPlatformType of();

    VoiceReprintResult reprint(VoiceReprintRequest request);
}
