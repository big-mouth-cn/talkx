package org.bigmouth.gpt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import org.bigmouth.gpt.ai.prompt.SystemPromptParser;
import org.bigmouth.gpt.ai.prompt.UserPromptParser;
import org.bigmouth.gpt.service.FileUploadType;
import org.bigmouth.gpt.entity.response.ModelResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author allen
 * @date 2023-04-24
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "config")
public class ApplicationConfig {

    /**
     * TalkX Web端的Host地址
     */
    private String talkxHost = "http://127.0.0.1:5174";

    /**
     * TalkX API端的Host地址
     */
    private String talkxApiHost = "http://127.0.0.1:8080";

    /**
     * LLM api host，如：https://api.aigateway.work
     */
    private String llmApiHost = "https://api.aigateway.work";

    /**
     * LLM api key
     */
    private String llmApiKey = "";

    /**
     * LLM 默认模型
     */
    private String llmModelName = "qwen-turbo";

    /**
     * 系统介绍
     */
    private List<String> systemIntro = Lists.newArrayList("你好！我是「TalkX」，你可以跟我聊任何你感兴趣的话题，无论是关于生活、工作、娱乐、教育、科技等等");

    /**
     * 每日游客最大聊天次数
     */
    private int maximumChatByDaily = 3;

    /**
     * 邀请注册奖励蒜粒数量
     */
    private BigDecimal inviteRegisterRewardCoins = BigDecimal.valueOf(0.1);

    /**
     * 注册奖励蒜粒数量
     */
    private BigDecimal registerRewardCoins = BigDecimal.valueOf(0.1);

    /**
     * 支付回调地址
     */
    private String payNotifyUrl = "http://127.0.0.1:8080/pay/notify";

    /**
     * ffmpeg 命令路径
     */
    private String ffmpegCommand = "ffmpeg";

    /**
     * 是否启用清除n天前的历史会话
     */
    private boolean enableClearHistorySession = true;

    /**
     * 清除n天前的历史会话
     */
    private int clearHistorySessionBeforeDays = 30;

    /**
     * 注册验证码，写死的。如果填写空值，则随机生成。
     */
    private String registerVerifyCode = "123456";

    /**
     * 如果请求LLM服务失败，重试次数
     */
    private int retryTimes = 0;

    /**
     * 是否开启Redis缓存
     */
    private boolean enableRedisCache = false;

    /**
     * 系统提示词解析器
     * key = roleType
     * value = SystemPromptParser
     */
    private Map<String, Class<? extends SystemPromptParser>> systemPromptParserMap = Maps.newHashMap();

    /**
     * 用户提示词解析器
     * key = roleType
     * value = UserPromptParser
     */
    private Map<String, Class<? extends UserPromptParser>> userPromptParserMap = Maps.newHashMap();

    /**
     * 指定文件上传实现的类型，默认：TalkX
     */
    private FileUploadType defaultFileUploadType = FileUploadType.TALKX;

    /**
     * 阿里云百炼模型、应用调用时使用的密钥
     */
    private String aliyunDashscopeApiKey = "";

    /**
     * 扣子CN AccessToken
     */
    private String cozeCnAccessToken = "";

    /**
     * 扣子COM AccessToken
     */
    private String cozeComAccessToken = "";

    @Data
    public static class CustomModel {
        private Set<Long> userIds = Sets.newHashSet();
        private List<ModelResponse> modelResponses;
    }
}
