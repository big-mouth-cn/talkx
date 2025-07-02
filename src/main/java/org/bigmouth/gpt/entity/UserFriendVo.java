package org.bigmouth.gpt.entity;

import com.bxm.warcar.utils.JsonHelper;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bigmouth.gpt.entity.request.FriendCreateModelConfig;
import org.bigmouth.gpt.utils.Constants;

import java.util.List;

/**
 * @author huxiao
 * @date 2023/12/14
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class UserFriendVo {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 好友ID
     */
    private Long friendId;

    private Long userFriendId;

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

    private Integer isSupportMemory;

    /**
     * 角色类型
     */
    private String roleType;

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

    private String contentPrompt;

    /**
     * 上下文数量
     */
    private Integer messageContextSize;

    /**
     * 模型设置
     */
    private FriendCreateModelConfig openaiRequestBody;

    /**
     * 快速开始
     */
    private List<String> conversationStart;

    /**
     * AI 类型。1 普通、2 GPTs
     */
    private int friendType = Constants.Friend.FRIEND_TYPE_BASIC;

    /**
     * 添加来源。1 好友广场、2 自建
     */
    private int friendSource;

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
     * 变量
     */
    private String variables;

    public static UserFriendVo of(Friend friend, UserFriend o) {
        return new UserFriendVo()
                .setFriendId(o.getFriendId())
                .setUserId(o.getUserId())
                .setUserFriendId(o.getId())
                .setTop(o.getTop())
                .setOrdered(o.getOrdered())
                .setAvatar(o.getAvatar())
                .setName(o.getName())
                .setIsSupportMemory(o.getIsSupportMemory())
                .setRoleType(o.getRoleType())
                .setVoiceChat(o.getVoiceChat())
                .setWelcome(o.getWelcome())
                .setIntro(o.getIntro())
                .setProductType(o.getProductType())
                .setCssAvatar(o.getCssAvatar())
                .setTag(o.getTag())
                .setSystemPrompt(o.getSystemPrompt())
                .setContentPrompt(o.getContentPrompt())
                .setMessageContextSize(o.getMessageContextSize())
                .setOpenaiRequestBody(JsonHelper.convert(o.getOpenaiRequestBody(), FriendCreateModelConfig.class))
                .setConversationStart(friend.getConversactionStartSet())
                .setFriendSource(o.getSource())
                .setFriendType(friend.getFriendType())
                .setAliyunDashscopeWorkspaceId(friend.getAliyunDashscopeWorkspaceId())
                .setAliyunDashscopeAppId(friend.getAliyunDashscopeAppId())
                .setAliyunDashscopeApiKey(friend.getAliyunDashscopeApiKey())
                .setCozeBotId(friend.getCozeBotId())
                .setCozeAccessToken(friend.getCozeAccessToken())
                .setVariables(o.getVariables())
                ;
    }
}
