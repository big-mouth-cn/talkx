package org.bigmouth.gpt.entity.response;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bigmouth.gpt.entity.FriendVo;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.entity.UserFriendMediaConfig;

/**
 * @author Allen Hu
 * @date 2025/3/5
 */
@Data
@Accessors(chain = true)
public class UserFriendMediaConfigVo {

    private UserFriendMediaConfig userFriendMediaConfig;
    private User user;
    private FriendVo friendVo;
}
