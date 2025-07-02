package org.bigmouth.gpt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Data;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.entity.UserFriendMediaConfig;
import org.bigmouth.gpt.entity.UserVoiceReprint;
import org.bigmouth.gpt.interceptor.ContextFactory;
import org.bigmouth.gpt.service.IUserFriendMediaConfigService;
import org.bigmouth.gpt.service.IUserVoiceReprintService;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintRequest;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintResult;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintService;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintServiceFactory;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Allen Hu
 * @date 2025/3/2
 */
@RestController
@RequestMapping("/user/voice")
public class UserVoiceController {

    private final IUserVoiceReprintService userVoiceReprintService;
    private final IUserFriendMediaConfigService userFriendMediaConfigService;
    private final VoiceReprintServiceFactory voiceReprintServiceFactory;

    public UserVoiceController(IUserVoiceReprintService userVoiceReprintService, IUserFriendMediaConfigService userFriendMediaConfigService, VoiceReprintServiceFactory voiceReprintServiceFactory) {
        this.userVoiceReprintService = userVoiceReprintService;
        this.userFriendMediaConfigService = userFriendMediaConfigService;
        this.voiceReprintServiceFactory = voiceReprintServiceFactory;
    }

    @GetMapping("/list")
    public ResponseEntity<Object> list() {
        User user = ContextFactory.getLoginUser();
        Long id = user.getId();
        LambdaQueryWrapper<UserVoiceReprint> query = Wrappers.lambdaQuery();
        query.eq(UserVoiceReprint::getUserId, id)
                .orderByDesc(UserVoiceReprint::getCreateTime);
        List<UserVoiceReprint> list = userVoiceReprintService.list(query);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/delete")
    public ResponseEntity<Object> delete(@RequestBody @Validated DeleteReq req) {
        UserVoiceReprint reprint = userVoiceReprintService.getById(req.getId());
        if (reprint == null || !reprint.getUserId().equals(ContextFactory.getLoginUser().getId())) {
            return ResponseEntity.notFound().build();
        }
        // 删除前检查是否被使用
        if (userFriendMediaConfigService.count(Wrappers.lambdaQuery(UserFriendMediaConfig.class)
                .eq(UserFriendMediaConfig::getAudioPlatformType, reprint.getAudioPlatformType())
                .eq(UserFriendMediaConfig::getAudioModel, reprint.getAudioModel())
                .eq(UserFriendMediaConfig::getAudioRole, reprint.getAudioRole())) > 0) {
            return ResponseEntity.badRequest().body("该声音已被使用，无法删除。");
        }
        boolean b = userVoiceReprintService.removeById(req.getId());
        return ResponseEntity.ok(b);
    }

    @PostMapping("/add")
    public ResponseEntity<Object> add(@RequestBody @Validated AddReq req) {
        Long userId = ContextFactory.getLoginUser().getId();
        // 同一个用户最多创建5个
        if (userVoiceReprintService.count(Wrappers.lambdaQuery(UserVoiceReprint.class)
                .eq(UserVoiceReprint::getUserId, userId)) >= 3) {
            return ResponseEntity.badRequest().body("一个用户最多只能复刻3个声音。");
        }
        VoiceReprintService voiceReprintService = voiceReprintServiceFactory.get();
        if (null == voiceReprintService) {
            return ResponseEntity.badRequest().body("不支持的语音平台。");
        }
        String voiceSrcUrl = req.getVoiceSrcUrl();
        String modelNamePrefix = "opentalkx";
        VoiceReprintResult result = voiceReprintService.reprint(new VoiceReprintRequest().setVoiceSrcUrl(voiceSrcUrl).setModelNamePrefix(modelNamePrefix));
        if (null == result) {
            return ResponseEntity.badRequest().body("创建失败，请联系管理员。");
        }
        String modelId = result.getAudioRole();
        UserVoiceReprint reprint = new UserVoiceReprint();
        reprint.setUserId(userId);
        reprint.setVoiceName(req.getVoiceName());
        reprint.setVoiceSrcUrl(voiceSrcUrl);
        reprint.setAudioPlatformType(result.getTtsPlatformType().name());
        reprint.setAudioModel(result.getAudioModel());
        reprint.setAudioRole(modelId);
        userVoiceReprintService.save(reprint);
        return ResponseEntity.ok(reprint);
    }

    @Data
    public static class AddReq {

        @NotBlank(message = "音频名称不能为空")
        @Length(max = 32, message = "音频名称不能超过32个字符")
        private String voiceName;
        /**
         * 音频源文件地址
         */
        @NotBlank(message = "音频源文件地址不能为空")
        private String voiceSrcUrl;
    }

    @Data
    public static class DeleteReq {
        @NotNull(message = "id不能为空")
        private Long id;
    }
}
