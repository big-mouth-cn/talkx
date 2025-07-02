package org.bigmouth.gpt.entity;

import lombok.Data;
import org.bigmouth.gpt.ai.entity.Message;
import org.bigmouth.gpt.utils.Constants;

import java.util.List;

import static org.bigmouth.gpt.ai.entity.DalleApiRequest.QUALITY_STANDARD;
import static org.bigmouth.gpt.ai.entity.DalleApiRequest.STYLE_VIVID;

/**
 * @author allen
 * @date 2023-04-20
 * @since 1.0
 */
@Data
public class ChatRequest {

    /**
     * 角色类型
     */
    private String roleType = RoleType.GENERAL;
    /**
     * 产品类型
     */
    private int productId = Constants.PRODUCT_TYPE_WEB;
    /**
     * SessionID
     */
    private String sessionId;
    /**
     * 消息列表，也叫：补全，Completion
     */
    private List<Message> messages;

    /**
     * DALL-E 参数
     */
    private DallE dalle;

    /**
     * 是否流式
     */
    private boolean stream = true;

    /**
     * 选择的 Table_Schema ID
     */
    private Long tableSchemaId;

    @Data
    public static class DallE {
        /**
         * The quality of the image that will be generated. hd creates images with finer details
         * and greater consistency across the image. This param is only supported for dall-e-3.
         */
        private String quality = QUALITY_STANDARD;

        /**
         * The size of the generated images. Must be one of 256x256, 512x512, or 1024x1024 for dall-e-2.
         * Must be one of 1024x1024, 1792x1024, or 1024x1792 for dall-e-3 models.
         */
        private String size = "1024x1024";

        /**
         * The style of the generated images. Must be one of vivid or natural.
         * Vivid causes the model to lean towards generating hyper-real and dramatic images.
         * Natural causes the model to produce more natural, less hyper-real looking images.
         * This param is only supported for dall-e-3.
         */
        private String style = STYLE_VIVID;
    }

    /**
     * 返回 productType 类型，根据 productId 来判断的
     * @return productType
     */
    public int toProductTypeByProductId() {
        // https://bxmrds.yuque.com/wq5oui/project_doc/zh0caw5kw3whz84n
        switch (productId) {
            case 0:
            case 1:
            case 2:
                return Constants.PRODUCT_TYPE_WEB;
            default:
                return Constants.PRODUCT_TYPE_IDE_PLUGIN;
        }
    }
}
