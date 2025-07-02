package org.bigmouth.gpt.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.ai.entity.OpenApiRequest;
import org.bigmouth.gpt.ai.entity.xinghuo.XinghuoApiRequest;

import java.math.BigDecimal;

/**
 * @author allen
 * @date 2023/5/15
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class Prompt {

    /**
     * 角色类型
     */
    private String roleType;
    /**
     * GPTs ID
     */
    private String gptsId;
    /**
     * 每次对话所需要的蒜粒数量
     */
    private BigDecimal coinCostPer;
    /**
     * 是否固定密钥
     */
    private boolean fixedKey;
    /**
     * 提示词
     */
    private String systemPrompt;

    /**
     * 内容提示词
     */
    private String contentPrompt;

    /**
     * 内容宏
     */
    private String macro;
    /**
     * 消息上下文数量。
     */
    private int messageContextSize;
    /**
     * 接口参数
     */
    private OpenApiRequest request = new OpenApiRequest();

    /**
     * 自定义变量
     */
    private String variables;

    /**
     * 星火接口参数
     */
    private XinghuoApiRequest xinghuoApiRequest = new XinghuoApiRequest();

    public boolean isEmptyContentPrompt() {
        return (StringUtils.isBlank(contentPrompt) || StringUtils.isBlank(macro));
    }

    public boolean isGpts() {
        return StringUtils.isNotBlank(gptsId);
    }
}
