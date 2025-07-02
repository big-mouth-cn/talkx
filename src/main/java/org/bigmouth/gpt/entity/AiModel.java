package org.bigmouth.gpt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.bigmouth.gpt.utils.Constants;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 模型列表
 * </p>
 *
 * @author allen
 * @since 2023-10-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ai_model")
public class AiModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 平台类型。1 OpenAI、2 阿里云百炼
     */
    private Integer platformType;

    /**
     * 模型名称，如：gpt-4，不能作为别名使用
     */
    private String modelName;

    /**
     * 模型分组，比如gpt-3.5-turbo、gpt-3.5-turbo-16k 都属于gpt-3.5。1 gpt-3.5、2 gpt-4、3 星火1.5、4 星火2.0、5 星火3.0
     * @see org.bigmouth.gpt.utils.Constants.AiPlatform
     */
    private Integer modelGroup;

    /**
     * 模型请求地址
     */
    private String requestUrl;

    /**
     * 模型最大的token数限制
     */
    private Integer maxToken;

    /**
     * input price per token
     */
    private BigDecimal inputPrice;

    /**
     * cached price per token
     */
    private BigDecimal cachedPrice;

    /**
     * output price per token
     */
    private BigDecimal outPrice;

    /**
     * 结算币种。1 人民币、2 美元
     */
    private Integer settleCurrency;

    /**
     * 每次对话所需要的蒜粒数量
     */
    private BigDecimal coinCostPer;

    /**
     * 输入1K的需要的蒜粒
     */
    private BigDecimal inputCoins;

    /**
     * 输出1K的需要的蒜粒
     */
    private BigDecimal outputCoins;

    /**
     * 展示图标
     */
    private String icon;

    /**
     * 展示排序
     */
    private Integer ordered;

    /**
     * 是否隐藏显示。1 隐藏，0 不隐藏。
     */
    private Integer isHidden;

    /**
     * 是否允许选择。0 不允许、1 允许
     */
    private Integer canSelection;

    /**
     * 是否允许上传文件。1 允许
     */
    private Integer isAllowUpload;

    /**
     * 是否支持工具
     */
    private Integer isSupportTool;

    /**
     * 提示标签
     */
    private String commentTags;

    /**
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime modifyTime;


    public static final String ID = "id";

    public static final String PLATFORM_TYPE = "platform_type";

    public static final String MODEL_NAME = "model_name";

    public static final String MODEL_GROUP = "model_group";

    public static final String REQUEST_URL = "request_url";

    public static final String MAX_TOKEN = "max_token";

    public static final String INPUT_PRICE = "input_price";

    public static final String CACHED_PRICE = "cached_price";

    public static final String OUT_PRICE = "out_price";

    public static final String SETTLE_CURRENCY = "settle_currency";

    public static final String COIN_COST_PER = "coin_cost_per";

    public static final String INPUT_COINS = "input_coins";

    public static final String OUTPUT_COINS = "output_coins";

    public static final String ICON = "icon";

    public static final String ORDERED = "ordered";

    public static final String IS_HIDDEN = "is_hidden";

    public static final String CAN_SELECTION = "can_selection";

    public static final String IS_ALLOW_UPLOAD = "is_allow_upload";

    public static final String IS_SUPPORT_TOOL = "is_support_tool";

    public static final String COMMENT_TAGS = "comment_tags";

    public static final String BEGIN_TIME = "begin_time";

    public static final String EXPIRE_TIME = "expire_time";

    public static final String CREATE_TIME = "create_time";

    public static final String MODIFY_TIME = "modify_time";

    public static AiModel ofBasic(String modelName,
                                  String requestUrl,
                                  Integer isSupportTool) {
        return ofBasic(Constants.AiPlatform.PLATFORM_TYPE_OPENAI, modelName, requestUrl, isSupportTool);
    }

    public static AiModel ofBasic(int platformType,
                                  String modelName,
                                  String requestUrl,
                                  Integer isSupportTool) {
        return new AiModel()
                .setPlatformType(platformType)
                .setModelName(modelName)
                .setModelGroup(1)
                .setRequestUrl(requestUrl)
                .setMaxToken(Integer.MAX_VALUE)
                .setInputPrice(BigDecimal.ZERO).setCachedPrice(BigDecimal.ZERO).setOutPrice(BigDecimal.ZERO)
                .setSettleCurrency(2)
                .setCoinCostPer(BigDecimal.ZERO).setInputCoins(BigDecimal.ZERO).setOutputCoins(BigDecimal.ZERO)
                .setIsSupportTool(isSupportTool);
    }
}
