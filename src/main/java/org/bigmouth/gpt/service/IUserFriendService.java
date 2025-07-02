package org.bigmouth.gpt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.bigmouth.gpt.entity.FriendVo;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.entity.UserFriend;
import org.bigmouth.gpt.entity.request.FriendCreateRequest;
import org.bigmouth.gpt.entity.request.FriendUpdateMemoryConfigRequest;
import org.bigmouth.gpt.entity.request.FriendUpdateRequest;

import java.util.List;

/**
 * <p>
 * 用户的好友 服务类
 * </p>
 *
 * @author allen
 * @since 2023-12-13
 */
public interface IUserFriendService extends IService<UserFriend> {

    UserFriend queryWithCache(Long userId, Integer productType, String roleType);

    void deleteFriendCache(Long userId, Integer productType, String roleType);

    void setTop(User user, Long friendId, boolean isTop);

    FriendVo follow(User user, Long friendId);

    void delete(User user, Long friendId);

    void deleteFriendListCache(Integer productType, User user);

    List<FriendVo> getFriendsForGuest();

    List<FriendVo> getMyFriends(Integer productType, User user);

    FriendVo createFriend(FriendCreateRequest request, User user);

    FriendVo updateFriend(FriendUpdateRequest request, User user);

    FriendVo updateMemoryConfig(FriendUpdateMemoryConfigRequest request, User user);
}
