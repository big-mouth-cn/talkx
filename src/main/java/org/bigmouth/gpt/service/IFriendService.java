package org.bigmouth.gpt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.bigmouth.gpt.entity.Friend;
import org.bigmouth.gpt.entity.FriendTagCount;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author allen
 * @since 2023-12-13
 */
public interface IFriendService extends IService<Friend> {

    Friend getByRoleType(String roleType);

    Friend getCacheById(Long id);

    void deleteCacheById(Long id);

    void deleteCacheByRoleType(String roleType);

    Map<String, Integer> countFriendTag();
}
