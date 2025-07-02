package org.bigmouth.gpt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bxm.warcar.cache.Fetcher;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.entity.AiModel;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.entity.request.UserUpdateRequest;
import org.bigmouth.gpt.service.IAiModelService;
import org.bigmouth.gpt.service.IUserInfoService;
import org.bigmouth.gpt.service.IUserService;
import org.bigmouth.gpt.utils.RedisKeys;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author tangxiao
 * @date 2023/7/21
 * @since 1.0
 */
@Service
public class UserInfoServiceImpl implements IUserInfoService {
    private final Fetcher fetcher;
    private final IUserService userService;
    private final IAiModelService aiModelService;

    public UserInfoServiceImpl(Fetcher fetcher, IUserService userService, IAiModelService aiModelService) {
        this.fetcher = fetcher;
        this.userService = userService;
        this.aiModelService = aiModelService;
    }

    @Override
    public void update(User user, UserUpdateRequest request) {
        checkArgument(request, user);
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(User.ID, user.getId())
                .set(request.getField(), request.getValue());
        userService.update(updateWrapper);
        userService.refreshUserCache();
    }

    private void checkArgument(UserUpdateRequest request, User user) {
        String value = request.getValue();
        switch (request.getField()) {
            case User.NAME:
                // 不为空
                Preconditions.checkArgument(StringUtils.isNotBlank(value) && value.length() <= 16);
                break;
            case User.EMAIL:
                // 无校验
                break;
            case User.PHONE_NUM:
                // 不为空
                Preconditions.checkArgument(StringUtils.isNotBlank(value));
                checkPhoneNum(request, user);
                break;
            case User.API_KEY:
                // 不需要校验
                break;
            case User.PROXY_BASE_URL:
                if (StringUtils.isNotBlank(value)) {
                    try {
                        UriComponentsBuilder.fromUriString(value);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("不正确的代理地址");
                    }
                }
                break;
            case User.MODEL:
                // 需要在model枚举中
                Set<String> enableModels = aiModelService.getValidityList()
                        .stream()
                        .map(AiModel::getModelName)
                        .collect(Collectors.toSet());
                // 不检查，因为用户支持自定义模型
//                Preconditions.checkArgument(enableModels.contains(value));
                break;
            case User.AVATAR:
                // 不为空
                Preconditions.checkArgument(StringUtils.isNotBlank(value));
                break;
            default:
                throw new IllegalArgumentException("非法参数");
        }
    }

    private void checkPhoneNum(UserUpdateRequest request, User user) {
        String oldPhoneNum = user.getPhoneNum();
        String newPhoneNum = request.getValue();
        String code = request.getExtra();
        boolean isOldPhoneCheckPass = fetcher.exists(RedisKeys.stringCodePassCache(oldPhoneNum));
        Preconditions.checkState(isOldPhoneCheckPass, "非法请求，请重新操作");
        Preconditions.checkArgument(StringUtils.isNotBlank(newPhoneNum));
        Preconditions.checkArgument(StringUtils.isNotBlank(code));

        String itVerifyCode = fetcher.fetch(RedisKeys.stringVerifyCodeCache(newPhoneNum), String.class);
        if (StringUtils.isBlank(itVerifyCode)) {
            throw new IllegalStateException("验证码已过期");
        }
        if (!StringUtils.equals(itVerifyCode, code)) {
            throw new IllegalArgumentException("验证码不正确");
        }

        QueryWrapper<User> queryWrapper = Wrappers.query(new User().setPhoneNum(newPhoneNum));
        long newPhoneNumCount = userService.count(queryWrapper);
        if (newPhoneNumCount > 0) {
            throw new IllegalStateException("该手机号码已被使用");
        }
    }
}
