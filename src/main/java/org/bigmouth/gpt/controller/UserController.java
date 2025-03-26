package org.bigmouth.gpt.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bxm.warcar.utils.IpHelper;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.entity.response.CoinCatalogVo;
import org.bigmouth.gpt.interceptor.ContextFactory;
import org.bigmouth.gpt.service.CoinService;
import org.bigmouth.gpt.service.IUserService;
import org.bigmouth.gpt.utils.UserTokenUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *  用户相关前端控制器
 * </p>
 *
 * @author Allen Hu
 * @since 2023-06-12
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;
    private final CoinService coinService;
    private final ApplicationConfig applicationConfig;

    public UserController(IUserService userService, CoinService coinService, ApplicationConfig applicationConfig) {
        this.userService = userService;
        this.coinService = coinService;
        this.applicationConfig = applicationConfig;
    }

    @RequestMapping(value = "/send_code", method = RequestMethod.POST)
    public ResponseEntity<Object> sendCode(@RequestBody @Validated SendCodeRequest request,
                                           HttpServletRequest httpServletRequest) {
        String ip = IpHelper.getIpFromHeader(httpServletRequest);
        String phoneNum = request.getPhoneNum();
        userService.sendVerifyCode(ip, phoneNum);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/check_code", method = RequestMethod.POST)
    public ResponseEntity<Void> checkCode(@RequestBody @Validated CheckCodeRequest request) {
        String phoneNum = request.getPhoneNum();
        String code = request.getCode();
        userService.checkCode(phoneNum, code);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<LoginResponse> login(@RequestBody @Validated LoginRequest request) {
        String ip = ContextFactory.get().getIp();
        String phoneNum = request.getPhoneNum();
        String code = request.getCode();
        String inviteCode = request.getInviteCode();
        String token = userService.login(phoneNum, code, inviteCode, ip);
        return ResponseEntity.ok(new LoginResponse().setToken(token));
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = UserTokenUtils.getToken(request);
        if (Objects.isNull(token)) {
            throw new IllegalStateException("No token");
        }
        userService.logout(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check_exists_user")
    public ResponseEntity<Void> checkExistsUser(@RequestBody @Validated CheckExistsUserRequest checkExistsUserRequest) {
        long count = userService.count(Wrappers.query(new User().setPhoneNum(checkExistsUserRequest.getPhoneNum())));
        if (count > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/check_exists_invite_code")
    public ResponseEntity<Void> checkExistsInviteCode(@RequestBody @Validated CheckExistsInviteCodeRequest checkExistsUserRequest) {
        long count = userService.count(Wrappers.query(new User().setInviteCode(checkExistsUserRequest.getInviteCode())));
        if (count > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/coin_catalog")
    public ResponseEntity<List<CoinCatalogVo>> coinCatalog() {
        return ResponseEntity.ok(coinService.getCoinCatalog().stream().map(CoinCatalogVo::new).collect(Collectors.toList()));
    }

    @GetMapping("/get_invite_reward")
    public ResponseEntity<Object> getInviteReward() {
        return ResponseEntity.ok(applicationConfig.getInviteRegisterRewardCoins());
    }

    @Data
    public static class SendCodeRequest {
        @NotBlank(message = "未填写手机号码") private String phoneNum;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "未填写手机号码") private String phoneNum;
        @NotBlank(message = "未填写验证码") private String code;
        private String inviteCode;
    }

    @Data
    @Accessors(chain = true)
    public static class LoginResponse {
        private String token;
    }

    @Data
    public static class CheckCodeRequest {
        @NotBlank(message = "未填写手机号码") private String phoneNum;
        @NotBlank(message = "未填写验证码") private String code;
    }

    @Data
    public static class CheckExistsUserRequest {
        @NotBlank(message = "未填写手机号码") private String phoneNum;
    }

    @Data
    public static class CheckExistsInviteCodeRequest {
        @NotBlank(message = "未填写邀请码") private String inviteCode;
    }
}
