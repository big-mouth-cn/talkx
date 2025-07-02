package org.bigmouth.gpt.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.utils.Constants;

import java.util.List;
import java.util.Optional;

/**
 * @author huxiao
 * @date 2023/12/13
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class FriendVo {

    /**
     * 好友ID
     */
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
     * UserFirned.id
     */
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
     * 自定义格式的头像，前端自己定义格式
     */
    private String cssAvatar;

    /**
     * 标签，支持多个，用英文逗号分隔
     */
    private String tag;

    /**
     * 固定模型
     */
    private String fixedModel;

    // --------- other property ------- .//

    /**
     * AI 类型。1 普通、2 GPTs
     */
    private int friendType = Constants.Friend.FRIEND_TYPE_BASIC;
    /**
     * 提示标签
     */
    private List<Tag> commentTags;
    /**
     * 是否显示模型选项
     */
    private boolean showModelSelect = true;
    /**
     * 快速开始
     */
    private List<String> conversationStart;

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

    public static FriendVo of(Friend friend) {
        FriendVo vo = new FriendVo()
                .setId(friend.getId())
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
                .setTag(friend.getTag());
        fillOther(friend, vo);
        return vo;
    }

    public static FriendVo of(Friend friend, UserFriend uf) {
        FriendVo vo = new FriendVo()
                .setId(uf.getFriendId())
                .setFriendId(uf.getFriendId())
                .setUserId(uf.getUserId())
                .setUserFriendId(uf.getId())
                .setTop(uf.getTop())
                .setOrdered(uf.getOrdered())
                .setAvatar(uf.getAvatar())
                .setName(uf.getName())
                .setRoleType(uf.getRoleType())
                .setVoiceChat(uf.getVoiceChat())
                .setWelcome(uf.getWelcome())
                .setIntro(uf.getIntro())
                .setCssAvatar(uf.getCssAvatar())
                .setTag(uf.getTag());
        fillOther(friend, vo);
        return vo;
    }

    private static void fillOther(Friend friend, FriendVo vo) {
        vo.setFriendType(friend.getFriendType());

        List<Tag> tags = Optional.ofNullable(friend.getCommentTags())
                .map(s -> JSONObject.parseArray(s, Tag.class)).orElse(null);
        vo.setCommentTags(tags);

        String fixedModel = friend.getFixedModel();
        vo.setFixedModel(fixedModel);
        vo.setShowModelSelect(StringUtils.isBlank(fixedModel));

        vo.setConversationStart(friend.getConversactionStartSet());

        vo.setAliyunDashscopeWorkspaceId(friend.getAliyunDashscopeWorkspaceId())
                .setAliyunDashscopeAppId(friend.getAliyunDashscopeAppId())
                .setAliyunDashscopeApiKey(friend.getAliyunDashscopeApiKey());

        vo.setCozeAccessToken(friend.getCozeAccessToken())
                .setCozeBotId(friend.getCozeBotId());

        vo.setVariables(friend.getVariables());
    }

    @Data
    public static class Tag {
        private String name;
        private String bgColor;
        private String color;
    }
}
