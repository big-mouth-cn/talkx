package org.bigmouth.gpt.xiaozhi.tts;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author Allen Hu
 * @date 2025/3/24
 */
@Data
@Accessors(chain = true)
public class VoiceReprintRequest {
    /**
     * 原始音频地址
     */
    @NotBlank(message = "音频地址不能为空")
    private String voiceSrcUrl;
    /**
     * 自定义模型名称前缀
     */
    @NotBlank(message = "自定义模型名称前缀不能为空")
    private String modelNamePrefix;
}
