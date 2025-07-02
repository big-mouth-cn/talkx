package org.bigmouth.gpt.service.impl;

import com.bxm.warcar.cache.Fetcher;
import com.bxm.warcar.cache.KeyGenerator;
import com.bxm.warcar.utils.JsonHelper;
import com.bxm.warcar.utils.KeyBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.ai.entity.OpenApiRequest;
import org.bigmouth.gpt.ai.entity.xinghuo.XinghuoApiRequest;
import org.bigmouth.gpt.entity.Prompt;
import org.bigmouth.gpt.entity.PromptConfig;
import org.bigmouth.gpt.entity.UserFriend;
import org.bigmouth.gpt.exceptions.AiStatusException;
import org.bigmouth.gpt.service.IPromptConfigService;
import org.bigmouth.gpt.service.IUserFriendService;
import org.bigmouth.gpt.service.PromptService;
import org.bigmouth.gpt.utils.Constants;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author allen
 * @date 2023/5/15
 * @since 1.0.0
 */
@Slf4j
@Service
public class PromptServiceImpl implements PromptService {

    private final IPromptConfigService promptConfigService;
    private final IUserFriendService userFriendService;
    private final Fetcher fetcher;

    public PromptServiceImpl(IPromptConfigService promptConfigService, IUserFriendService userFriendService, Fetcher fetcher) {
        this.promptConfigService = promptConfigService;
        this.userFriendService = userFriendService;
        this.fetcher = fetcher;
    }

    @Override
    public String createRoleType() {
        return promptConfigService.createRoleType();
    }

    @Override
    public Prompt getWithMacros(Long userId, Integer productType, String roleType) {
        Prompt promptConfig = get(roleType);
        if (Objects.isNull(userId)) {
            // 游客不需要查找好友
            return promptConfig;
        }
        if (Constants.PRODUCT_TYPE_IDE_PLUGIN == productType) {
            // 插件里不需要根据roleType去查找好友
            return promptConfig;
        }
        UserFriend userFriend = userFriendService.queryWithCache(userId, productType, roleType);
        if (Objects.isNull(userFriend)) {
            throw new AiStatusException("无效的好友");
        }
        return custom(promptConfig, userFriend);
    }

    private Prompt get(String roleType) {
        return fetcher.hfetch(keyGenerator(), roleType, () -> {
            PromptConfig promptConfig = promptConfigService.getOne(roleType);
            if (Objects.isNull(promptConfig)) {
                return null;
            }
            Prompt prompt = new Prompt()
                    .setCoinCostPer(promptConfig.getCoinCostPer())
                    .setRoleType(promptConfig.getRoleType())
                    .setFixedKey(promptConfig.isFixedKey0())
                    .setSystemPrompt(promptConfig.getSystemPrompt())
                    .setContentPrompt(promptConfig.getContentPrompt())
                    .setMacro(promptConfig.getMacro())
                    .setMessageContextSize(promptConfig.getMessageContextSize());

            String openaiRequestBody = promptConfig.getOpenaiRequestBody();
            if (StringUtils.isNotBlank(openaiRequestBody)) {
                try {
                    OpenApiRequest openApiRequest = JsonHelper.convert(openaiRequestBody, OpenApiRequest.class);
                    prompt.setRequest(openApiRequest);
                } catch (Exception e) {
                    log.error("convert: ", e);
                }
            }

            String xinghuoRequestBody = promptConfig.getXinghuoRequestBody();
            if (StringUtils.isNotBlank(xinghuoRequestBody)) {
                try {
                    XinghuoApiRequest xinghuoApiRequest = JsonHelper.convert(xinghuoRequestBody, XinghuoApiRequest.class);
                    prompt.setXinghuoApiRequest(xinghuoApiRequest);
                } catch (Exception e) {
                    log.error("conver: ", e);
                }
            }
            return prompt;
        }, Prompt.class, 60);
    }

    private Prompt custom(Prompt prompt, UserFriend userFriend) {
        OpenApiRequest request = null;
        try {
            request = JsonHelper.convert(userFriend.getOpenaiRequestBody(), OpenApiRequest.class);
        } catch (Exception e) {
            log.error("convert: ", e);
        }
        return prompt
                .setRequest(Optional.ofNullable(request).orElse(new OpenApiRequest()))
                .setSystemPrompt(userFriend.getSystemPrompt())
                .setContentPrompt(userFriend.getContentPrompt())
                .setRoleType(userFriend.getRoleType())
                .setGptsId(userFriend.getGptsId())
                .setMessageContextSize(userFriend.getMessageContextSize())
                .setVariables(userFriend.getVariables())
                ;
    }

    private static KeyGenerator keyGenerator() {
        return () -> KeyBuilder.build("talkx", "prompts", "default");
    }
}
