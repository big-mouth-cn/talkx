package org.bigmouth.gpt.xiaozhi.tts;

public interface VoiceReprintService {

    TtsPlatformType of();

    String reprint(String voiceSrcUrl, String modelNamePrefix);

    VoiceReprintResult reprint(VoiceReprintRequest request);
}
