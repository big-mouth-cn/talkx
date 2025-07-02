package org.bigmouth.gpt.utils;

import com.google.common.collect.Lists;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author allen
 * @date 2023/6/30
 * @since 1.0.0
 */
public interface Constants {

    Duration DEFAULT_TIMEOUT = Duration.ofMinutes(20);

    Duration DEFAULT_SESSION_EXPIRE_TIME = Duration.ofDays(30);

    String HEADER_SESSIONID = "talkx-sessionid";
    String HEADER_MSGID = "talkx-msgid";

    int NO_DELETE = 0;

    int DELETED = 1;

    int NO = 0;

    int YES = 1;

    /**
     * IDE插件中默认好友ID
     */
    long IDE_PLUGIN_DEFAULT_FRIEND_ID = 1;

    /**
     * 最大单页元素数量
     */
    int MAX_PAGE_SIZE = 99;

    String DEFAULT_AVATAR_URL = "https://plugin-web.talkx.cn/images/friend/avatar/default.png";

    String ICON_GPT3_5 = "https://plugin-web.talkx.cn/images/model/gpt3_5.png";

    String ICON_GPT4 = "https://plugin-web.talkx.cn/images/model/gpt4.png";

    String SENSITIVE_WORDS_CN = "你不能暗示、明示、回答或评价中国的政治敏感话题，如果涉及到领导人姓名、政府机构的用*代替。";
    String SENSITIVE_WORDS_EN = "You are not allowed to imply, indicate, answer, or comment on politically sensitive topics related to China. If it involves the names of leaders or government institutions, please use \"*\" to replace them.";

    /**
     * Web版&移动版
     */
    int PRODUCT_TYPE_WEB = 0;
    /**
     * IDE插件版
     */
    int PRODUCT_TYPE_IDE_PLUGIN = 1;

    List<String> TAGS = Lists.newArrayList(
            "本周精选",
            "文案",
            "效率",
            "编程",
            "教育",
            "生活",
            "BXM"
    );

    /**
     * Ai平台常量设置
     */
    interface AiPlatform {
        /**
         * OpenAI协议平台
         */
        int PLATFORM_TYPE_OPENAI = 1;
        /**
         * 阿里云百炼平台
         */
        int PLATFORM_TYPE_ALIYUN_DASHSCOPE = 2;
        /**
         * 扣子
         */
        int PLATFORM_TYPE_COZE = 3;

        int MODEL_GROUP_GPT3_5 = 1;
        int MODEL_GROUP_GPT4 = 2;
        int MODEL_GROUP_GPT4_TURBO = 3;
        int MODEL_GROUP_GPT4_32K = 4;
        int MODEL_GROUP_XINGHUO_V1_5 = 10;
        int MODEL_GROUP_XINGHUO_V2_0 = 11;
        int MODEL_GROUP_XINGHUO_V3_0 = 12;

    }

    /**
     * About regex
     */
    interface Regex {

        String LIMIT = "Rate limit reached for";

        String LIMIT_MIN = LIMIT + " .+ per min";
        String LIMIT_DAY = LIMIT + " .+ per day";
        String LIMIT_BILLING = "Billing hard limit has been reached";

        String SAFETY_SYSTEM = "Your request was rejected as a result of our safety system.";

        Pattern LIMIT_MIN_PATTERN = Pattern.compile(LIMIT_MIN);
        Pattern LIMIT_DAY_PATTERN = Pattern.compile(LIMIT_DAY);
    }

    interface Coin {

        /**
         * 消耗
         */
        int BILL_TYPE_USE = 1;
        /**
         * 充值
         */
        int BILL_TYPE_RECHARGE = 2;
        /**
         * 奖励
         */
        int BILL_TYPE_REWARDS = 3;
    }

    interface ApiKeys {
        /**
         * platformType：OpenAI
         * @see AiPlatform#PLATFORM_TYPE_OPENAI
         */
        @Deprecated
        int PLATFORM_TYPE_OPENAI = AiPlatform.PLATFORM_TYPE_OPENAI;

        /**
         * adaptedModel：OpenAI GPT-3.5
         */
        int ADAPTED_MODEL_OPENAI_GPT3_5 = 1;
        /**
         * adaptedModel：OpenAI GPT-4
         */
        int ADAPTED_MODEL_OPENAI_GPT4 = 2;

        /**
         * 星火1.5
         */
        int ADAPTED_MODEL_XINGHUO_15 = 3;
        /**
         * 星火2.0
         */
        int ADAPTED_MODEL_XINGHUO_20 = 4;
        /**
         * 星火3.0
         */
        int ADAPTED_MODEL_XINGHUO_30 = 5;

        /**
         * status：正常
         */
        int STATUS_AVAILABLE = 1;
        /**
         * status：不可用
         */
        int STATUS_DISABLE = 0;
        /**
         * status：今日限流
         */
        int STATUS_LIMIT_TODAY = -1;
        /**
         * status：分钟限流
         */
        int STATUS_LIMIT_MINUTE = -2;
        /**
         * status：小时限流
         */
        int STATUS_LIMIT_HOUR = -3;

        /**
         * 不限速
         */
        int LIMIT_NO = -1;
    }

    interface ProductVersion {

        int PRODUCT_TYPE_WEB = 1;
        int PRODUCT_TYPE_JET_BRAINS = 2;
        int PRODUCT_TYPE_VS_CODE = 3;
        int PRODUCT_TYPE_HBUILDERX = 4;

        /**
         * 已废弃
         */
        int STATUS_DEPRECATED = 0;
        /**
         * 正常
         */
        int STATUS_AVAILABLE = 1;
    }

    interface Order {

        int WAIT_PAY = 0;
        int PAYING = 1;
        int SUCCESS = 2;
        int CLOSED = 3;
        int FAIL = 4;

        int PRODUCT_TYPE_COIN = 1;

        int PAY_TYPE_WECHAT = 2;
    }

    interface Friend {
        int SOURCE_PLAZA = 1;
        int SOURCE_SELF_BUILD = 2;

        int FRIEND_TYPE_BASIC = 1;
        int FRIEND_TYPE_GPTS = 2;
        /**
         * 阿里云百炼应用
         */
        int FRIEND_TYPE_ALIYUN_DASHSCOPE_APP = 3;
        int COZE_CN = 4;
        int COZE_COM = 5;

    }
}
