package org.bigmouth.gpt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.bigmouth.gpt.entity.User;

import java.math.BigDecimal;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Allen Hu
 * @since 2023-06-12
 */
public interface IUserService extends IService<User> {

    /**
     * 方法用于发送验证码，需要传入IP地址和手机号码。如果检测到IP地址或手机号码操作过于频繁，则会抛出异常。
     * 生成一个6位的随机验证码，并将其保存到数据库中，有效期为5分钟。如果发送成功，则返回true。
     *
     * @param ip       IP地址
     * @param phoneNum 手机号码
     * @throws IllegalStateException 如果操作过于频繁
     * @throws RuntimeException      如果短信发送失败，则抛出此异常。
     */
    void sendVerifyCode(String ip, String phoneNum) throws IllegalStateException;

    /**
     * 该方法用于用户登录，需要提供手机号和验证码作为参数。首先，从缓存中获取该手机号对应的验证码，
     * 并判断是否过期或为空。如果验证码不正确，则抛出异常。接着，检查该手机号是否已经注册过，
     * 如果没有，则创建一个新用户。最后，生成一个唯一的token，并将其与用户ID保存到数据库中，返回该token。
     * 需要注意的是，该方法可能会抛出两种异常：验证码已过期或验证码不正确。如果出现这些异常，
     * 应该向用户返回相应的错误信息。如果一切顺利，将返回一个有效的token，可以用于后续的用户操作。
     *
     * @param phoneNum 手机号码
     * @param verifyCode 验证码
     * @param inviteCode 邀请码
     * @param ip IP
     * @return token
     * @throws IllegalStateException 验证码已过期
     * @throws IllegalArgumentException 验证码不正确
     */
    String login(String phoneNum, String verifyCode, String inviteCode, String ip);

    /**
     * 获取User，从token缓存
     * @param token token
     * @return User对象
     */
    User getUserFromToken(String token);

    /**
     * 获取User，从token缓存
     * @param token token
     * @param refreshExpireTime 是否刷新过期时间
     * @return User对象
     */
    User getUserFromToken(String token, boolean refreshExpireTime);

    /**
     * 退出登录
     * @param token token
     */
    void logout(String token);

    /**
     * 校验手机号验证码
     * @param phoneNum
     * @param code
     */
    void checkCode(String phoneNum, String code);

    /**
     * 刷新用户缓存
     */
    @Deprecated
    void refreshUserCache();

    /**
     * 获取指定用户的蒜粒数量。
     *
     * @param id 用户ID
     * @return 当前用户的硬币数量，如果用户ID无效，则返回-1
     */
    BigDecimal getCoin(long id);

    /**
     * 增加指定用户的蒜粒数量。
     *
     * @param id    用户ID
     * @param value 硬币数量，正数表示增加，负数表示减少
     * @return true表示成功增加硬币数量，false表示失败或无效用户ID
     */
    boolean plusCoin(long id, BigDecimal value);

    /**
     * 减少指定用户的蒜粒数量。
     *
     * @param id    用户ID
     * @param value 要减少的硬币数量，正数表示减少，负数表示增加
     * @return true表示成功减少硬币数量，false表示失败或无效用户ID
     */
    boolean minusCoin(long id, BigDecimal value);

    /**
     * 给用户发送一个短信
     *
     * @param id 用户ID
     * @param content 内容
     */
    void sendSms(long id, String content);

    User authWebSocket(String webSocketApiKey, String sessionId);

    User checkWebSocket(String sessionId);

    void cancelWebSocket(String sessionId);
}
