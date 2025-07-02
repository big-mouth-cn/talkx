package org.bigmouth.gpt.entity.request;

import lombok.Data;
import org.bigmouth.gpt.utils.Constants;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author huxiao
 * @date 2023/12/13
 * @since 1.0.0
 */
@Data
public class FriendCreateRequest {
    /**
     * TalkX产品类型
     * 0 Web版
     * 1 移动版
     * 2 IDE插件版
     */
    private int productType = Constants.PRODUCT_TYPE_WEB;

    /**
     * AI 类型。1 普通、2 GPTs、3 阿里云百炼应用
     */
    private Integer friendType = Constants.Friend.FRIEND_TYPE_BASIC;

    /**
     * 头像地址
     */
    private String avatar = Constants.DEFAULT_AVATAR_URL;

    /**
     * 自定义格式的头像，前端自己定义格式
     */
    private String cssAvatar;

    /**
     * 好友名称
     */
    @NotBlank(message = "昵称不能为空") private String name;

    /**
     * 简介
     */
    private String intro;

    /**
     * 招呼语
     */
    private String welcome;

    /**
     * 是否支持语音聊天。1 是，其他情况否。
     */
    private Integer voiceChat = 0;

    /**
     * 标签
     */
    private String tag;

    /**
     * 指令
     */
    private String systemPrompt;

    private String contentPrompt;

    /**
     * 上下文数量
     */
    private int messageContextSize = 32;

    /**
     * 快速开始
     */
    private List<String> conversationStart;

    /**
     * 模型设置
     */
    private FriendCreateModelConfig openaiRequestBody = new FriendCreateModelConfig();

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
}
