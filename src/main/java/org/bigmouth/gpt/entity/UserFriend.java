package org.bigmouth.gpt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.utils.Constants;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户的好友
 * </p>
 *
 * @author allen
 * @since 2023-12-13
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_friend")
public class UserFriend implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 好友ID
     */
    private Long friendId;

    /**
     * 是否置顶
     */
    private Integer top;

    /**
     * 排序
     */
    private Integer ordered;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 名称
     */
    private String name;

    /**
     * 是否支持记忆
     */
    private Integer isSupportMemory;

    /**
     * 添加来源。1 好友广场、2 自建
     */
    private Integer source;

    /**
     * 角色类型
     */
    private String roleType;

    /**
     * GPTs ID
     */
    private String gptsId;

    /**
     * 是否支持语音聊天。1 是，其他情况否。
     */
    private Integer voiceChat;

    /**
     * 欢迎语
     */
    private String welcome;

    /**
     * 简介
     */
    private String intro;

    /**
     * 所属产品类型。0 Web版+移动版、1 IDE插件版
     */
    private Integer productType;

    /**
     * 自定义格式的头像，前端自己定义格式
     */
    private String cssAvatar;

    /**
     * 标签
     */
    private String tag;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 用户提示词
     */
    private String contentPrompt;

    /**
     * 上下文数量
     */
    private Integer messageContextSize;

    /**
     * 模型的配置JSON
     */
    private String openaiRequestBody;

    /**
     * 自定义变量定义，一般用于阿里云百炼等平台调用时的传参，默认是一个JSON格式的数据。
     */
    private String variables;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;


    public static final String ID = "id";

    public static final String USER_ID = "user_id";

    public static final String FRIEND_ID = "friend_id";

    public static final String TOP = "top";

    public static final String ORDERED = "ordered";

    public static final String AVATAR = "avatar";

    public static final String NAME = "name";
    public static final String IS_SUPPORT_MEMORY = "is_support_memory";

    public static final String SOURCE = "source";

    public static final String ROLE_TYPE = "role_type";

    public static final String GPTS_ID = "gpts_id";

    public static final String VOICE_CHAT = "voice_chat";

    public static final String WELCOME = "welcome";

    public static final String INTRO = "intro";

    public static final String PRODUCT_TYPE = "product_type";

    public static final String CSS_AVATAR = "css_avatar";

    public static final String TAG = "tag";

    public static final String SYSTEM_PROMPT = "system_prompt";

    public static final String CONTENT_PROMPT = "content_prompt";

    public static final String MESSAGE_CONTEXT_SIZE = "message_context_size";

    public static final String OPENAI_REQUEST_BODY = "openai_request_body";

    public static final String VARIABLES = "variables";

    public static final String CREATE_TIME = "create_time";
    public static final String MODIFY_TIME = "modify_time";

    /**
     * 转换成用户好友。
     * 缺少以下参数：
     * userId / productType / systemPrompt / openAiOpenBody / gptsId
     * @param friend 好友
     * @return 用户好友
     */
    public static UserFriend of(Friend friend) {
        return new UserFriend()
                .setFriendId(friend.getId())
                .setTop(Constants.NO)
                .setOrdered(friend.getOrdered())
                .setAvatar(friend.getAvatar())
                .setName(friend.getName())
                .setRoleType(friend.getRoleType())
                .setVoiceChat(friend.getVoiceChat())
                .setWelcome(friend.getWelcome())
                .setIntro(friend.getIntro())
                .setCssAvatar(friend.getCssAvatar())
                .setTag(friend.getTag())
                .setVariables(friend.getVariables());
    }
}
