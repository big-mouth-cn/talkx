package org.bigmouth.gpt.xiaozhi.tts.volcengine;

import lombok.Getter;

/**
 * @author Allen Hu
 * @date 2025/3/24
 */
@Getter
public class TtsException extends RuntimeException {
    private final int code;
    private final String message;

    public TtsException(int code, String message) {
        super("code=" + code + ", message=" + message);
        this.code = code;
        this.message = message;
    }
}