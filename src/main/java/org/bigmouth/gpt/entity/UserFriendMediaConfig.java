package org.bigmouth.gpt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bigmouth.gpt.utils.Constants;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 
 * </p>
 *
 * @author allen
 * @since 2025-02-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserFriendMediaConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long friendId;

    private Long userFriendId;

    /**
     * 音频平台类型
     */
    private String audioPlatformType;

    /**
     * 音频模型
     */
    private String audioModel;

    /**
     * 音频角色
     */
    private String audioRole;

    /**
     * 音频示例
     */
    private String audioDemoUrl;

    /**
     * 是否自定义模型
     */
    private Integer customModel;

    /**
     * 选择的模型
     */
    private String llmModel;

    /**
     * 代理地址
     */
    private String proxyBaseUrl;

    /**
     * 用户的api key (代理)
     */
    private String apiKey;

    /**
     * 是否支持工具
     */
    private Integer isSupportTool;

    private LocalDateTime createTime;

    private LocalDateTime modifyTime;


    public static final String ID = "id";

    public static final String USER_ID = "user_id";

    public static final String FRIEND_ID = "friend_id";

    public static final String USER_FRIEND_ID = "user_friend_id";

    public static final String AUDIO_PLATFORM_TYPE = "audio_platform_type";

    public static final String AUDIO_MODEL = "audio_model";

    public static final String AUDIO_ROLE = "audio_role";

    public static final String AUDIO_DEMO_URL = "audio_demo_url";
    public static final String CUSTOM_MODEL = "custom_model";
    public static final String LLM_MODEL = "llm_model";
    public static final String PROXY_BASE_URL = "proxy_base_url";

    public static final String API_KEY = "api_key";
    public static final String IS_SUPPORT_TOOL = "is_support_tool";
    public static final String CREATE_TIME = "create_time";

    public static final String MODIFY_TIME = "modify_time";

    public boolean isUseCustomModel() {
        return Objects.equals(Constants.YES, getCustomModel());
    }
}
