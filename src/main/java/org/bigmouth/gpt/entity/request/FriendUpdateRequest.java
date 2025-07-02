package org.bigmouth.gpt.entity.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author huxiao
 * @date 2023/12/15
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FriendUpdateRequest extends FriendCreateRequest {

    /**
     * user friend id
     */
    @NotNull(message = "ID不能为空") private Long id;
}
