package org.bigmouth.gpt.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxm.warcar.cache.Counter;
import com.bxm.warcar.cache.Fetcher;
import com.bxm.warcar.cache.Updater;
import com.bxm.warcar.integration.eventbus.EventPark;
import com.bxm.warcar.utils.KeyBuilder;
import com.bxm.warcar.utils.UUIDHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.event.NewUserRegisterEvent;
import org.bigmouth.gpt.mapper.talkx.UserMapper;
import org.bigmouth.gpt.service.IUserService;
import org.bigmouth.gpt.service.InviteCodeService;
import org.bigmouth.gpt.utils.Constants;
import org.bigmouth.gpt.utils.RedisKeys;
import org.bigmouth.gpt.utils.UserTokenUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

import static org.bigmouth.gpt.utils.RedisKeys.stringUserTokenCache;
import static org.bigmouth.gpt.utils.RedisKeys.stringVerifyCodeCache;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Allen Hu
 * @since 2023-06-12
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final static String VERIFY_CODE_MSG = "【TalkX】您的登录验证码是：%s";
    private final Counter counter;
    private final Updater updater;
    private final Fetcher fetcher;
    private final EventPark eventPark;
    private final InviteCodeService inviteCodeService;
    private final ApplicationConfig applicationConfig;

    public UserServiceImpl(Counter counter, Updater updater, Fetcher fetcher,
                           EventPark eventPark, InviteCodeService inviteCodeService, ApplicationConfig applicationConfig) {
        this.counter = counter;
        this.updater = updater;
        this.fetcher = fetcher;
        this.eventPark = eventPark;
        this.inviteCodeService = inviteCodeService;
        this.applicationConfig = applicationConfig;
    }

    @Override
    public void sendVerifyCode(String ip, String phoneNum) {
        if (!isCheckIp(ip)) {
            throw new IllegalStateException("操作过于频繁，请稍后再试。");
        }
        if (!isCheckPhoneNum(phoneNum)) {
            throw new IllegalStateException("操作过于频繁，请稍后再试。");
        }

        String verifyCode = RandomStringUtils.randomNumeric(6);
        String registerVerifyCode = applicationConfig.getRegisterVerifyCode();
        if (StringUtils.isNotBlank(registerVerifyCode)) {
            verifyCode = registerVerifyCode;
        }
        this.saveVerifyCode(phoneNum, verifyCode, 5);
        String msg = this.createVerifyCodeMsg(verifyCode);
        if (!this.sendMessageToPhoneNum(phoneNum, msg)) {
            throw new RuntimeException("Sms message send fail!");
        }
    }

    @Override
    public String login(String phoneNum, String verifyCode, String inviteCode, String ip) {
        String itVerifyCode = fetcher.fetch(stringVerifyCodeCache(phoneNum), String.class);
        if (StringUtils.isBlank(itVerifyCode)) {
            throw new IllegalStateException("验证码已过期");
        }
        if (!StringUtils.equals(itVerifyCode, verifyCode)) {
            throw new IllegalArgumentException("验证码不正确");
        }
        // 注册或创建用户
        User exists = super.getOne(Wrappers.query(new User().setPhoneNum(phoneNum)));
        if (Objects.isNull(exists)) {
            // 注册新用户
            User inviter = null;
            if (StringUtils.isNotBlank(inviteCode)) {
                // 邀请码不为空，则需要校验正确性
                inviter = super.getOne(Wrappers.query(new User().setInviteCode(inviteCode)));
                if (Objects.isNull(inviter)) {
                    throw new IllegalStateException("邀请码不正确");
                }
            }
            User user = new User()
                    .setPhoneNum(phoneNum)
                    .setModel(applicationConfig.getLlmModelName())
                    .setName(phoneNum)
                    .setInviteCode(inviteCodeService.getInviteCode());
            super.saveOrUpdate(user);
            exists = user;
            // 发送注册事件
            NewUserRegisterEvent eventObject = new NewUserRegisterEvent(inviter, exists, this);
            eventObject.setIp(ip);
            eventPark.post(eventObject);
        } else {
            if (!exists.isAvailableStatus()) {
                throw new IllegalStateException("账号不可用");
            }
        }

        // 保存token
        String token = UUIDHelper.generate();
        this.saveUserToken(token, exists, Constants.DEFAULT_SESSION_EXPIRE_TIME);
        return token;
    }

    @Override
    public User getUserFromToken(String token) {
        return getUserFromToken(token, true);
    }

    @Override
    public User getUserFromToken(String token, boolean refreshExpireTime) {
        User user = this.getUser(token);
        if (refreshExpireTime && Objects.nonNull(user)) {
            this.refreshExpireTime(token);
        }
        return user;
    }

    @Override
    public void logout(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        this.deleteUserToken(token);
    }

    @Override
    public void checkCode(String phoneNum, String code) {
        String itVerifyCode = fetcher.fetch(stringVerifyCodeCache(phoneNum), String.class);
        if (StringUtils.isBlank(itVerifyCode)) {
            throw new IllegalStateException("验证码已过期");
        }
        if (!StringUtils.equals(itVerifyCode, code)) {
            throw new IllegalArgumentException("验证码不正确");
        }

        updater.update(RedisKeys.stringCodePassCache(phoneNum), code, 30 * 60);
    }

    @Override
    public void refreshUserCache() {
        String token = UserTokenUtils.getToken();
        User userFromToken = this.getUserFromToken(token);
        User user = super.getById(userFromToken.getId());
        this.saveUserToken(token, user, Constants.DEFAULT_SESSION_EXPIRE_TIME);
    }

    @Override
    public BigDecimal getCoin(long id) {
        return baseMapper.getCoin(id);
    }

    @Override
    public boolean plusCoin(long id, BigDecimal value) {
        return baseMapper.plusCoin(id, value) > 0;
    }

    @Override
    public boolean minusCoin(long id, BigDecimal value) {
        return baseMapper.minusCoin(id, value) > 0;
    }

    @Override
    public void sendSms(long id, String content) {
        User user = getById(id);
        if (Objects.isNull(user)) {
            return;
        }
        String phoneNum = user.getPhoneNum();
        if (StringUtils.isBlank(phoneNum)) {
            return;
        }
        if (StringUtils.isBlank(content)) {
            return;
        }
        this.sendMessageToPhoneNum(phoneNum, content);
    }

    @Override
    public User authWebSocket(String webSocketApiKey, String sessionId) {
        User user = getOne(Wrappers.query(new User().setWebsocketApiKey(webSocketApiKey)));
        if (user == null) {
            throw new NullPointerException();
        }
        updater.update(stringUserTokenCache(sessionId), user);
        return user;
    }

    @Override
    public User checkWebSocket(String sessionId) {
        return getUser(sessionId);
    }

    @Override
    public void cancelWebSocket(String sessionId) {
        updater.remove(stringUserTokenCache(sessionId));
    }

    private boolean sendMessageToPhoneNum(String phoneNum, String msg) {
        // TODO 发送验证码
        log.info("Send message to phoneNum: {}, msg: {}", phoneNum, msg);
        return true;
    }

    private User getUser(String token) {
        return fetcher.fetch(stringUserTokenCache(token), User.class);
    }

    private void refreshExpireTime(String token) {
        updater.expire(stringUserTokenCache(token), (int) Constants.DEFAULT_SESSION_EXPIRE_TIME.getSeconds());
    }

    private void saveUserToken(String token, User user, Duration expired) {
        updater.update(stringUserTokenCache(token), user, (int) expired.getSeconds());
    }

    private void deleteUserToken(String token) {
        updater.remove(stringUserTokenCache(token));
    }

    /**
     * 有效期内保存指定手机号码的验证码
     * @param phoneNum 手机号码
     * @param code 验证码
     * @param expiredTimeInMin 过期时间，分钟
     */
    private void saveVerifyCode(String phoneNum, String code, int expiredTimeInMin) {
        updater.update(stringVerifyCodeCache(phoneNum), code, expiredTimeInMin * 60);
    }

    /**
     * 5分钟内最多30次
     * @param ip IP
     * @return 是否检查通过
     */
    private boolean isCheckIp(String ip) {
        return counter.incrementAndGet(() -> KeyBuilder.build("talkx", "user", "check_ip", ip), 5 * 60) <= 30;
    }

    /**
     * 5分钟内最多3次
     * @param phoneNum 手机号码
     * @return 是否检查通过
     */
    private boolean isCheckPhoneNum(String phoneNum) {
        return counter.incrementAndGet(() -> KeyBuilder.build("talkx", "user", "check_phone_num", phoneNum), 5 * 60) <= 3;
    }

    private String createVerifyCodeMsg(String code) {
        return String.format(VERIFY_CODE_MSG, code);
    }
}
