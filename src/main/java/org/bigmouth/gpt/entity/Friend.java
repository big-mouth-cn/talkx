package org.bigmouth.gpt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.utils.Constants;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * <p>
 *
 * </p>
 *
 * @author allen
 * @since 2024-01-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Friend implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer ordered;

    private String avatar;

    private String name;

    @TableField("roleType")
    private String roleType;

    /**
     * AI 类型。1 普通、2 GPTs、3 阿里云百炼应用、4 COZE中国、5 COZE全球
     */
    private Integer friendType;

    /**
     * 固定模型
     */
    private String fixedModel;

    /**
     * 提示标签
     */
    private String commentTags;

    /**
     * 快速开始
     */
    private String conversactionStart;

    /**
     * 是否默认启用语音聊天。1 是，其他情况不是
     */
    private Integer voiceChat;

    private String welcome;

    private String intro;

    /**
     * 是否默认添加到用户的好友列表
     */
    private Integer isDefaultFriend;

    /**
     * 是否公开。1 公开，0 不公开
     */
    private Integer isPublic;

    /**
     * 是否需要授权才能添加和使用，0 不需要、1 需要
     */
    private Integer isPermission;

    /**
     * 自定义格式的头像，前端自己定义格式
     */
    private String cssAvatar;

    /**
     * 标签
     */
    private String tag;

    /**
     * 独立设置的请求链接
     */
    private String requestUrl;

    /**
     * 独立请求链接的密钥
     */
    private String apiKey;

    /**
     * 阿里云百炼工作空间ID
     */
    private String aliyunDashscopeWorkspaceId;

    /**
     * 阿里云百炼应用ID
     */
    private String aliyunDashscopeAppId;

    /**
     * 阿里云百炼应用密钥
     */
    private String aliyunDashscopeApiKey;

    /**
     * Coze 智能体ID
     */
    private String cozeBotId;

    /**
     * Coze 智能体访问令牌
     */
    private String cozeAccessToken;

    /**
     * 自定义变量定义，一般用于阿里云百炼等平台调用时的传参，默认是一个JSON格式的数据。
     */
    private String variables;

    private LocalDateTime createTime;

    private LocalDateTime modifyTime;

    private Integer deleted;


    public static final String ID = "id";

    public static final String ORDERED = "ordered";

    public static final String AVATAR = "avatar";

    public static final String NAME = "name";

    public static final String ROLETYPE = "roleType";

    public static final String FRIEND_TYPE = "friend_type";

    public static final String FIXED_MODEL = "fixed_model";

    public static final String COMMENT_TAGS = "comment_tags";

    public static final String CONVERSACTION_START = "conversaction_start";

    public static final String VOICE_CHAT = "voice_chat";

    public static final String WELCOME = "welcome";

    public static final String INTRO = "intro";

    public static final String IS_DEFAULT_FRIEND = "is_default_friend";

    public static final String IS_PUBLIC = "is_public";

    public static final String CSS_AVATAR = "css_avatar";

    public static final String TAG = "tag";

    public static final String REQUEST_URL = "request_url";

    public static final String API_KEY = "api_key";

    public static final String ALIYUN_DASHSCOPE_WORKSPACE_ID = "aliyun_dashscope_workspace_id";

    public static final String ALIYUN_DASHSCOPE_APP_ID = "aliyun_dashscope_app_id";

    public static final String ALIYUN_DASHSCOPE_API_KEY = "aliyun_dashscope_api_key";

    public static final String COZE_BOT_ID = "coze_bot_id";

    public static final String COZE_ACCESS_TOKEN = "coze_access_token";

    public static final String VARIABLES = "variables";

    public static final String CREATE_TIME = "create_time";

    public static final String MODIFY_TIME = "modify_time";

    public static final String DELETED = "deleted";

    public static final Set<Integer> AGENT_FRIEND_TYPE_SET = Sets.newHashSet(
            Constants.Friend.FRIEND_TYPE_ALIYUN_DASHSCOPE_APP,
            Constants.Friend.COZE_CN,
            Constants.Friend.COZE_COM
    );

    public List<String> getConversactionStartSet() {
        return Optional.ofNullable(getConversactionStart())
                .map((Function<String, List<String>>) s -> Lists.newArrayList(StringUtils.split(s, ",")))
                .orElse(null);
    }

    public boolean isAgentFriend() {
        return AGENT_FRIEND_TYPE_SET.contains(getFriendType());
    }

    public String getAgentSpecialModelName() {
        if (Objects.equals(getFriendType(), Constants.Friend.COZE_CN) || Objects.equals(getFriendType(), Constants.Friend.COZE_COM)) {
            return getCozeBotId();
        } else if (Objects.equals(getFriendType(), Constants.Friend.FRIEND_TYPE_ALIYUN_DASHSCOPE_APP)) {
            return Optional.ofNullable(getAliyunDashscopeWorkspaceId()).orElse(StringUtils.EMPTY) + "$" + getAliyunDashscopeAppId();
        }
        return getFriendType() + "" + getId();
    }

    public Integer getAgentImplementAiPlatformType() {
        if (Objects.equals(getFriendType(), Constants.Friend.COZE_CN) || Objects.equals(getFriendType(), Constants.Friend.COZE_COM)) {
            return Constants.AiPlatform.PLATFORM_TYPE_COZE;
        } else if (Objects.equals(getFriendType(), Constants.Friend.FRIEND_TYPE_ALIYUN_DASHSCOPE_APP)) {
            return Constants.AiPlatform.PLATFORM_TYPE_ALIYUN_DASHSCOPE;
        }
        return Constants.AiPlatform.PLATFORM_TYPE_OPENAI;
    }
}
