package org.bigmouth.gpt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxm.warcar.cache.DataExtractor;
import com.bxm.warcar.cache.Fetcher;
import com.bxm.warcar.cache.Updater;
import com.bxm.warcar.integration.eventbus.EventPark;
import com.bxm.warcar.utils.JsonHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.entity.*;
import org.bigmouth.gpt.entity.request.FriendCreateModelConfig;
import org.bigmouth.gpt.entity.request.FriendCreateRequest;
import org.bigmouth.gpt.entity.request.FriendUpdateMemoryConfigRequest;
import org.bigmouth.gpt.entity.request.FriendUpdateRequest;
import org.bigmouth.gpt.event.DeleteUserFriendEvent;
import org.bigmouth.gpt.mapper.talkx.UserFriendMapper;
import org.bigmouth.gpt.service.*;
import org.bigmouth.gpt.utils.Constants;
import org.bigmouth.gpt.utils.RedisKeys;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户的好友 服务实现类
 * </p>
 *
 * @author allen
 * @since 2023-12-13
 */
@Slf4j
@Service
public class UserFriendServiceImpl extends ServiceImpl<UserFriendMapper, UserFriend> implements IUserFriendService {

    private final IFriendService friendService;
    private final IPromptConfigService promptConfigService;
    private final IFriendPermissionService friendPermissionService;
    private final IDeviceService deviceService;
    private final Fetcher fetcher;
    private final Updater updater;
    private final EventPark eventPark;

    public UserFriendServiceImpl(IFriendService friendService, IPromptConfigService promptConfigService, IFriendPermissionService friendPermissionService, IDeviceService deviceService, Fetcher fetcher, Updater updater, EventPark eventPark) {
        this.friendService = friendService;
        this.promptConfigService = promptConfigService;
        this.friendPermissionService = friendPermissionService;
        this.deviceService = deviceService;
        this.fetcher = fetcher;
        this.updater = updater;
        this.eventPark = eventPark;
    }

    @Override
    public UserFriend queryWithCache(Long userId, Integer productType, String roleType) {
        return fetcher.fetch(RedisKeys.AboutFriend.stringUserFriend(userId, productType, roleType), new DataExtractor<UserFriend>() {
            @Override
            public UserFriend extract() {
                return getOne(Wrappers.query(new UserFriend().setUserId(userId).setProductType(productType).setRoleType(roleType)));
            }
        }, UserFriend.class, 60);
    }

    @Override
    public void deleteFriendCache(Long userId, Integer productType, String roleType) {
        updater.remove(RedisKeys.AboutFriend.stringUserFriend(userId, productType, roleType));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setTop(User user, Long friendId, boolean isTop) {
        UserFriend userFriend = super.getOne(Wrappers.query(new UserFriend().setUserId(user.getId()).setFriendId(friendId)));
        if (null == userFriend) {
            throw new IllegalArgumentException("无效的好友");
        }
        userFriend.setTop(isTop ? Constants.YES : Constants.NO);
        userFriend.setModifyTime(LocalDateTime.now());
        super.updateById(userFriend);
        this.deleteFriendListCache(userFriend.getProductType(), user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FriendVo follow(User user, Long friendId) {
        Long userId = user.getId();
        UserFriend userFriend = super.getOne(Wrappers.query(new UserFriend().setUserId(userId).setFriendId(friendId)));
        if (null != userFriend) {
            throw new IllegalArgumentException("不能重复添加");
        }

        Friend friend = friendService.getOne(Wrappers.query(new Friend().setId(friendId).setDeleted(Constants.NO_DELETE).setIsPublic(Constants.YES)));
        if (null == friend) {
            throw new IllegalArgumentException("无效的好友");
        }

        PromptConfig prompt = promptConfigService.getOne(friend.getRoleType());
        if (null == prompt) {
            throw new IllegalArgumentException("无效的好友");
        }

        if (Objects.equals(Constants.YES, friend.getIsPermission()) && !friendPermissionService.isAuthorized(friendId, userId)) {
            log.warn("User [{}] try follow friend [{} - {}] without permission", userId, friend.getId(), friend.getName());
            throw new IllegalArgumentException("对不起，你没有权限添加这个好友，申请权限请联系管理员，");
        }

        userFriend = UserFriend.of(friend)
                .setUserId(userId)
                .setSource(Constants.Friend.SOURCE_PLAZA)
                .setProductType(Constants.PRODUCT_TYPE_WEB)
                .setSystemPrompt(prompt.getSystemPrompt())
                .setContentPrompt(prompt.getContentPrompt())
                .setOpenaiRequestBody(prompt.getOpenaiRequestBody())
                .setVariables(friend.getVariables())
                .setGptsId(prompt.getGptsId());

        super.save(userFriend);
        this.deleteFriendListCache(Constants.PRODUCT_TYPE_WEB, user);
        return FriendVo.of(friend, userFriend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(User user, Long friendId) {
        if (Objects.equals(friendId, Constants.IDE_PLUGIN_DEFAULT_FRIEND_ID)) {
            throw new IllegalArgumentException("不能删除该好友");
        }
        UserFriend userFriend = getOne(Wrappers.query(new UserFriend().setProductType(Constants.PRODUCT_TYPE_WEB).setUserId(user.getId()).setFriendId(friendId)));
        if (null == userFriend) {
            throw new IllegalArgumentException("不存在该好友");
        }

        long count = deviceService.count(Wrappers.query(new Device().setUserFriendId(userFriend.getId())));
        if (count > 0) {
            throw new IllegalArgumentException("存在 " + count + " 智体设备绑定，请先解绑后再试。");
        }

        super.removeById(userFriend.getId());
        this.deleteFriendListCache(userFriend.getProductType(), user);
        eventPark.post(new DeleteUserFriendEvent(this, user, userFriend));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public FriendVo createFriend(FriendCreateRequest request, User user) {
        long counted = countSelfBuildFriend(request.getProductType(), user);
        if (counted > 5) {
            throw new IllegalStateException("抱歉，目前每个用户最多新建 5 个好友。");
        }

        // 1、创建一个 prompt
        String roleType = promptConfigService.createRoleType();
        String systemPrompt = request.getSystemPrompt();
        String contentPrompt = request.getContentPrompt();
        FriendCreateModelConfig openaiRequestBody = request.getOpenaiRequestBody();
        String requestBody = JsonHelper.convert(openaiRequestBody);
        PromptConfig prompt = new PromptConfig()
                .setRoleType(roleType)
                .setSystemPrompt(systemPrompt)
                .setContentPrompt(contentPrompt)
                .setMessageContextSize(request.getMessageContextSize())
                .setOpenaiRequestBody(requestBody);
        promptConfigService.save(prompt);

        // 2、创建一个 friend
        Friend friend = new Friend()
                .setAvatar(request.getAvatar())
                .setName(request.getName())
                .setRoleType(roleType)
                .setFriendType(request.getFriendType())
                .setVoiceChat(request.getVoiceChat())
                .setWelcome(request.getWelcome())
                .setIntro(request.getIntro())
                .setIsDefaultFriend(Constants.NO)
                .setIsPublic(Constants.NO)
                .setCssAvatar(request.getCssAvatar())
                .setTag(request.getTag())
                .setConversactionStart(Optional.ofNullable(request.getConversationStart()).map(strings -> StringUtils.join(strings, ",")).orElse(null))
                .setAliyunDashscopeWorkspaceId(request.getAliyunDashscopeWorkspaceId())
                .setAliyunDashscopeAppId(request.getAliyunDashscopeAppId())
                .setAliyunDashscopeApiKey(request.getAliyunDashscopeApiKey())
                .setCozeBotId(request.getCozeBotId())
                .setCozeAccessToken(request.getCozeAccessToken())
                .setVariables(request.getVariables())
                ;
        friendService.save(friend);

        // 3、创建一个 user friend
        UserFriend userFriend = UserFriend.of(friend)
                .setUserId(user.getId())
                .setSource(Constants.Friend.SOURCE_SELF_BUILD)
                .setProductType(request.getProductType())
                .setSystemPrompt(systemPrompt)
                .setContentPrompt(contentPrompt)
                .setMessageContextSize(request.getMessageContextSize())
                .setOpenaiRequestBody(requestBody)
                .setVariables(request.getVariables())
                ;
        super.save(userFriend);
        // 4、删除缓存
        this.deleteFriendListCache(request.getProductType(), user);

        // 5、返回这个新的好友视图
        return FriendVo.of(friend, userFriend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public FriendVo updateFriend(FriendUpdateRequest request, User user) {
        Long id = request.getId();
        Long userId = user.getId();
        UserFriend exists = getById(id);
        if (null == exists || !Objects.equals(userId, exists.getUserId())) {
            // 如果无效，或者不是这个用户的就不能修改
            throw new IllegalStateException("无效的好友");
        }

        Long friendId = exists.getFriendId();

        Friend friend = friendService.getById(friendId);
        Integer productType = exists.getProductType();
        String roleType = friend.getRoleType();

        // 允许修改自己创建的快速开始
        boolean own = Objects.equals(exists.getSource(), Constants.Friend.SOURCE_SELF_BUILD);
        if (own) {
            friend.setAliyunDashscopeWorkspaceId(request.getAliyunDashscopeWorkspaceId());
            friend.setAliyunDashscopeAppId(request.getAliyunDashscopeAppId());
            friend.setAliyunDashscopeApiKey(request.getAliyunDashscopeApiKey());
            friend.setCozeBotId(request.getCozeBotId());
            friend.setCozeAccessToken(request.getCozeAccessToken());
            friend.setConversactionStart(Optional.ofNullable(request.getConversationStart()).map(strings -> StringUtils.join(strings, ",")).orElse(null));
            friend.setModifyTime(LocalDateTime.now());
            friendService.updateById(friend);
            friendService.deleteCacheById(friendId);
            friendService.deleteCacheByRoleType(roleType);

            // 如果是Agent应用，那么修改所有用户的提示词
            if (friend.isAgentFriend()) {
                String systemPrompt = request.getSystemPrompt();
                if (StringUtils.isNotBlank(systemPrompt)) {
                    this.updateSystemPrompt(friendId, systemPrompt);
                    List<Long> userIds = baseMapper.getUserIdByFriendId(friendId);
                    if (CollectionUtils.isNotEmpty(userIds)) {
                        for (Long uId : userIds) {
                            this.deleteFriendCache(uId, productType, roleType);
                        }
                    }
                    PromptConfig promptConfig = promptConfigService.getOne(roleType);
                    if (Objects.nonNull(promptConfig)) {
                        promptConfig.setSystemPrompt(systemPrompt);
                        promptConfigService.saveOrUpdate(promptConfig);
                    }
                }
            }
        }

        exists.setAvatar(request.getAvatar())
                .setName(request.getName())
                .setVoiceChat(request.getVoiceChat())
                .setWelcome(request.getWelcome())
                .setIntro(request.getIntro())
                .setCssAvatar(request.getCssAvatar())
                .setTag(request.getTag())
                .setSystemPrompt(request.getSystemPrompt())
                .setContentPrompt(request.getContentPrompt())
                .setMessageContextSize(request.getMessageContextSize())
                .setOpenaiRequestBody(JsonHelper.convert(request.getOpenaiRequestBody()))
                .setVariables(request.getVariables())
                .setModifyTime(LocalDateTime.now())
        ;

        updateById(exists);

        this.deleteFriendListCache(productType, user);
        this.deleteFriendCache(userId, productType, roleType);

        return FriendVo.of(friend, exists);
    }

    @Override
    public FriendVo updateMemoryConfig(FriendUpdateMemoryConfigRequest request, User user) {
        Long id = request.getId();
        Long userId = user.getId();
        UserFriend exists = getById(id);
        if (null == exists || !Objects.equals(userId, exists.getUserId())) {
            // 如果无效，或者不是这个用户的就不能修改
            throw new IllegalStateException("无效的好友");
        }
        exists.setIsSupportMemory(request.getIsSupportMemory());
        updateById(exists);
        this.deleteFriendListCache(exists.getProductType(), user);
        Friend friend = friendService.getById(exists.getFriendId());
        return FriendVo.of(friend, exists);
    }

    @Override
    public void deleteFriendListCache(Integer productType, User user) {
        updater.remove(RedisKeys.AboutFriend.stringFriendList(productType, user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public List<FriendVo> getMyFriends(Integer productType, User user) {
        return fetcher.fetchList(RedisKeys.AboutFriend.stringFriendList(productType, user), new DataExtractor<List<FriendVo>>() {
            @Override
            public List<FriendVo> extract() {
                if (Objects.isNull(user)) {
                    return getFriendsForGuest();
                }
                QueryWrapper<UserFriend> query = new QueryWrapper<>(new UserFriend().setUserId(user.getId()).setProductType(productType));
                query.orderByDesc(UserFriend.TOP);
                query.orderByAsc(UserFriend.ID);

                List<UserFriend> userFriends = list(query);
                if (CollectionUtils.isEmpty(userFriends)) {
                    return initFriends(productType, user);
                }
                return userFriends.stream().map(userFriend -> {
                    Friend friend = friendService.getCacheById(userFriend.getFriendId());
                    if (Objects.isNull(friend)) {
                        return null;
                    }
                    return FriendVo.of(friend, userFriend);
                }).filter(Objects::nonNull).collect(Collectors.toList());
            }
        }, FriendVo.class, 60);
    }

    @Override
    public List<FriendVo> getFriendsForGuest() {
        return getFriendsForGuestMap().values().stream().map(FriendVo::of).collect(Collectors.toList());
    }

    /**
     * 修改用户好友的系统提示词
     * @param friendId 好友id
     * @param systemPrompt 提示词
     * @return 影响行数
     */
    public int updateSystemPrompt(Long friendId, String systemPrompt) {
        return getBaseMapper().updateSystemPrompt(friendId, systemPrompt);
    }

    private long countSelfBuildFriend(Integer productType, User user) {
        List<UserFriend> friends = list(Wrappers.query(new UserFriend().setUserId(user.getId()).setProductType(productType)));
        return friends.stream()
                .filter(new Predicate<UserFriend>() {
                    @Override
                    public boolean test(UserFriend userFriend) {
                        return Objects.equals(Constants.Friend.SOURCE_SELF_BUILD, userFriend.getSource());
                    }
                })
                .count();
    }

    private List<FriendVo> initFriends(Integer productType, User user) {
        if (Objects.equals(productType, Constants.PRODUCT_TYPE_IDE_PLUGIN)) {
            return Lists.newArrayList();
        }
        Map<Long, Friend> friendsForGuestMap = getFriendsForGuestMap();
        List<UserFriend> userFriends = this.saveBatch(friendsForGuestMap, productType, user);
        return userFriends.stream()
                .map(userFriend -> FriendVo.of(friendsForGuestMap.get(userFriend.getFriendId()), userFriend))
                .collect(Collectors.toList());
    }

    private Map<Long, Friend> getFriendsForGuestMap() {
        QueryWrapper<Friend> query = Wrappers.query(new Friend().setIsDefaultFriend(Constants.YES).setDeleted(Constants.NO_DELETE));
        query.orderByAsc(Friend.ORDERED);
        return friendService.list(query).stream().collect(Collectors.toMap(Friend::getId, friend -> friend));
    }

    private List<UserFriend> saveBatch(Map<Long, Friend> friendsForGuestMap, Integer productType, User user) {
        List<UserFriend> userFriends = Lists.newArrayListWithCapacity(friendsForGuestMap.size());
        for (Friend vo : friendsForGuestMap.values()) {
            UserFriend userFriend = new UserFriend()
                    .setFriendId(vo.getId())
                    .setTop(Constants.NO)
                    .setOrdered(vo.getOrdered())
                    .setAvatar(vo.getAvatar())
                    .setName(vo.getName())
                    .setRoleType(vo.getRoleType())
                    .setVoiceChat(vo.getVoiceChat())
                    .setWelcome(vo.getWelcome())
                    .setIntro(vo.getIntro())
                    .setCssAvatar(vo.getCssAvatar())
                    .setTag(vo.getTag())
                    .setVariables(vo.getVariables());
            userFriend.setUserId(user.getId());
            userFriend.setProductType(productType);
            PromptConfig promptConfig = promptConfigService.getOne(userFriend.getRoleType());
            if (Objects.nonNull(promptConfig)) {
                userFriend.setMessageContextSize(promptConfig.getMessageContextSize());
                userFriend.setSystemPrompt(promptConfig.getSystemPrompt());
                userFriend.setContentPrompt(promptConfig.getContentPrompt());
                String json = promptConfig.getOpenaiRequestBody();
                if (StringUtils.isNotBlank(json)) {
                    userFriend.setOpenaiRequestBody(json);
                }
                userFriend.setGptsId(promptConfig.getGptsId());
            }
            userFriends.add(userFriend);
        }
        super.saveBatch(userFriends);
        return userFriends;
    }
}
