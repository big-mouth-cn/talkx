package org.bigmouth.gpt.mapper.talkx;

import org.apache.ibatis.annotations.Param;
import org.bigmouth.gpt.entity.UserFriend;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 用户的好友 Mapper 接口
 * </p>
 *
 * @author allen
 * @since 2023-12-13
 */
public interface UserFriendMapper extends BaseMapper<UserFriend> {

    int updateSystemPrompt(@Param("friendId") Long friendId, @Param("systemPrompt") String systemPrompt);

    List<Long> getUserIdByFriendId(@Param("friendId") Long friendId);
}
