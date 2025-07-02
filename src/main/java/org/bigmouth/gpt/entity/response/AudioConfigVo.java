package org.bigmouth.gpt.entity.response;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bigmouth.gpt.xiaozhi.tts.TtsPlatformType;

/**
 * 音频配置信息
 *
 * @author allen
 * @since 2025-02-28
 */
@Data
@Accessors(chain = true)
public class AudioConfigVo {

    /**
     * 音频平台类型
     */
    private TtsPlatformType platformType;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 音频模型
     */
    private String model;

    /**
     * 音频角色
     */
    private String role;

    /**
     * 音频示例
     */
    private String demoUrl;

    /**
     * 语言
     */
    private String[] languages;
}