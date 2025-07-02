package org.bigmouth.gpt.xiaozhi.entity.asr;

import lombok.Data;
import org.apache.commons.codec.binary.Base64;

/**
 * @author Allen Hu
 * @date 2025/3/17
 */
@Data
public class TalkXAsrRequest {

    private String key;
    private String audioBase64;

    public TalkXAsrRequest() {
    }

    public TalkXAsrRequest(String key, byte[] bytes) {
        this.key = key;
        this.audioBase64 = Base64.encodeBase64String(bytes);
    }
}
