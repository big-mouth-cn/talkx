package org.bigmouth.gpt.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.UserMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.entity.*;
import org.bigmouth.gpt.entity.request.TestModelRequest;
import org.bigmouth.gpt.entity.request.UpdateMediaConfigRequest;
import org.bigmouth.gpt.entity.response.AudioConfigVo;
import org.bigmouth.gpt.entity.response.UserFriendMediaConfigVo;
import org.bigmouth.gpt.interceptor.ContextFactory;
import org.bigmouth.gpt.service.*;
import org.bigmouth.gpt.utils.Constants;
import org.bigmouth.gpt.utils.ResourceFileUtils;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 好友多媒体配置接口
 *
 * @author Allen Hu
 * @date 2025/2/28
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/friend/media")
public class FriendMediaController {

    private final XiaozhiConfig xiaozhiConfig;
    private final IUserFriendMediaConfigService userFriendMediaConfigService;
    private final IFriendService friendService;
    private final IUserFriendService userFriendService;
    private final IUserService userService;
    private final IAiModelService aiModelService;
    private final ApplicationConfig applicationConfig;

    @GetMapping("/get")
    public ResponseEntity<UserFriendMediaConfigVo> getConfig(@RequestParam Long userFriendId) {
        UserFriendMediaConfigVo res = new UserFriendMediaConfigVo();
        User user = ContextFactory.getLoginUser();
        UserFriendMediaConfig config = userFriendMediaConfigService.lambdaQuery()
                .eq(UserFriendMediaConfig::getUserId, user.getId())
                .eq(UserFriendMediaConfig::getUserFriendId, userFriendId)
                .one();
        UserFriend userFriend = userFriendService.getById(userFriendId);
        Friend friend = friendService.getById(userFriend.getFriendId());
        if (null == config) {
            AudioConfigVo defConfig = userFriendMediaConfigService.getDefConfig(userFriend.getRoleType());
            config = new UserFriendMediaConfig();
            if (null != defConfig) {
                config.setAudioPlatformType(defConfig.getPlatformType().name());
                config.setAudioModel(defConfig.getModel());
                config.setAudioRole(defConfig.getRole());
                config.setAudioDemoUrl(defConfig.getDemoUrl());
            }
            config.setCustomModel(Constants.NO);
            String model = xiaozhiConfig.getDefaultLlmModel();
            config.setLlmModel(model);
            Integer isSupportTool = Optional.ofNullable(aiModelService.get(model)).map(AiModel::getIsSupportTool).orElse(Constants.NO);
            config.setIsSupportTool(isSupportTool);
        }
        res.setUserFriendMediaConfig(config).setUser(user).setFriendVo(FriendVo.of(friend, userFriend));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/update")
    public ResponseEntity<Object> updateConfig(@Validated @RequestBody UpdateMediaConfigRequest request) {
        User user = ContextFactory.getLoginUser();
        Long userId = user.getId();
        Long userFriendId = request.getUserFriendId();

        // 验证好友关系
        UserFriend userFriend = userFriendService.getById(userFriendId);
        if (null == userFriend || !userFriend.getUserId().equals(userId)) {
            throw new IllegalStateException("无效的好友关系");
        }

        // 查询现有配置
        UserFriendMediaConfig existingConfig = userFriendMediaConfigService.lambdaQuery()
                .eq(UserFriendMediaConfig::getUserId, userId)
                .eq(UserFriendMediaConfig::getUserFriendId, userFriendId)
                .one();

        UserFriendMediaConfig config;
        if (existingConfig != null) {
            // 更新现有配置
            config = existingConfig;
        } else {
            // 创建新配置
            config = new UserFriendMediaConfig();
            config.setUserId(userId);
            config.setFriendId(userFriend.getFriendId());
            config.setUserFriendId(userFriendId);
        }

        // 更新配置信息
        config.setAudioPlatformType(request.getAudioPlatformType());
        config.setAudioModel(request.getAudioModel());
        config.setAudioRole(request.getAudioRole());
        config.setAudioDemoUrl(request.getAudioDemoUrl());
        config.setCustomModel(request.getCustomModel());
        config.setLlmModel(request.getLlmModel());
        config.setIsSupportTool(request.getIsSupportTool());

        // 保存自定义密钥
        if (Objects.equals(Constants.YES, request.getCustomModel())) {
            String proxyBaseUrl = request.getProxyBaseUrl();
            String apiKey = request.getApiKey();
            Preconditions.checkNotNull(proxyBaseUrl);
            Preconditions.checkNotNull(apiKey);
            config.setProxyBaseUrl(request.getProxyBaseUrl());
            config.setApiKey(request.getApiKey());
        }
        
        userFriendMediaConfigService.saveOrUpdate(config);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/audio_configs")
    public ResponseEntity<List<AudioConfigVo>> getAudioConfigs() {
        String json = ResourceFileUtils.fetch("tts-list.json");
        List<AudioConfigVo> audioConfigVos = JSONObject.parseArray(json, AudioConfigVo.class);
        return ResponseEntity.ok(audioConfigVos);
    }

    @PostMapping("/test")
    public ResponseEntity<Object> testModel(@RequestBody @Validated TestModelRequest request) {
        Duration timeout = Duration.ofMinutes(5);
        String apiKey = request.getApiKey();
        String proxyBaseUrl = request.getProxyBaseUrl();
        String baseUrl = UriComponentsBuilder.fromUriString(proxyBaseUrl.replace("chat/completions", ""))
                .build()
                .toString();
        OpenAiService openAiService = new OpenAiService(apiKey, timeout, baseUrl);
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(request.getLlmModel());
        chatCompletionRequest.setMaxTokens(1);
        chatCompletionRequest.setMessages(Lists.newArrayList(new UserMessage("Hi")));
        try {
            ChatCompletionResult chatCompletion = openAiService.createChatCompletion(chatCompletionRequest);
            return ResponseEntity.ok(chatCompletion);
        } catch (Exception e) {
            log.error("testModel: ", e);
            throw new IllegalStateException("测试失败，请检查配置。" + StringUtils.substring(e.getMessage(), 0, 32));
        }
    }
}
