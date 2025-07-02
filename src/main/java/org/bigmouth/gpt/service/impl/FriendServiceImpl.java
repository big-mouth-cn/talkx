package org.bigmouth.gpt.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxm.warcar.cache.Fetcher;
import com.bxm.warcar.cache.Updater;
import org.bigmouth.gpt.entity.Friend;
import org.bigmouth.gpt.entity.FriendTagCount;
import org.bigmouth.gpt.mapper.talkx.FriendMapper;
import org.bigmouth.gpt.service.IFriendService;
import org.bigmouth.gpt.utils.RedisKeys;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author allen
 * @since 2023-12-13
 */
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements IFriendService {

    private final Fetcher fetcher;
    private final Updater updater;

    public FriendServiceImpl(Fetcher fetcher, Updater updater) {
        this.fetcher = fetcher;
        this.updater = updater;
    }

    @Override
    public Friend getByRoleType(String roleType) {
        return fetcher.fetch(RedisKeys.AboutFriend.stringFriend(roleType),
                () -> getOne(Wrappers.query(new Friend().setRoleType(roleType))),
                Friend.class, 5 * 60);
    }

    @Override
    public Friend getCacheById(Long id) {
        return fetcher.fetch(RedisKeys.AboutFriend.stringFriend(id), () -> getById(id), Friend.class, 60);
    }

    @Override
    public void deleteCacheById(Long id) {
        updater.remove(RedisKeys.AboutFriend.stringFriend(id));
    }

    @Override
    public void deleteCacheByRoleType(String roleType) {
        updater.remove(RedisKeys.AboutFriend.stringFriend(roleType));
    }

    @Override
    public Map<String, Integer> countFriendTag() {
        List<FriendTagCount> friendTagCounts = fetcher.fetchList(RedisKeys.AboutFriend.stringFriendCount(),
                () -> baseMapper.countFriendTag(), FriendTagCount.class, 60);
        return friendTagCounts.stream().collect(Collectors.toMap(FriendTagCount::getName, FriendTagCount::getCount));
    }
}
