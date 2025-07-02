package org.bigmouth.gpt.ai;

import com.bxm.warcar.cache.Fetcher;
import com.bxm.warcar.integration.eventbus.EventPark;
import com.bxm.warcar.utils.TypeHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.ai.entity.*;
import org.bigmouth.gpt.ai.prompt.SystemPromptParser;
import org.bigmouth.gpt.ai.prompt.SystemPromptParserFactory;
import org.bigmouth.gpt.ai.prompt.UserPromptParser;
import org.bigmouth.gpt.ai.prompt.UserPromptParserFactory;
import org.bigmouth.gpt.entity.*;
import org.bigmouth.gpt.event.ChatRetryEvent;
import org.bigmouth.gpt.exceptions.AiAccountException;
import org.bigmouth.gpt.exceptions.AiException;
import org.bigmouth.gpt.exceptions.AiStatusException;
import org.bigmouth.gpt.exceptions.CoinNotEnoughException;
import org.bigmouth.gpt.service.*;
import org.bigmouth.gpt.utils.Constants;
import org.bigmouth.gpt.utils.RedisKeys;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * @author allen
 * @date 2023/5/25
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class ChatServiceFactory implements BeanPostProcessor {

    private final ApplicationConfig applicationConfig;
    private final Fetcher fetcher;
    private final IAiModelService aiModelService;
    private final IFriendService friendService;
    private final PromptService promptService;
    private final AccessKeyService accessKeyService;
    private final IUserService userService;
    private final ITableSchemaService tableSchemaService;
    private final IFriendPermissionService friendPermissionService;
    private final EventPark eventPark;
    private final SystemPromptParserFactory systemPromptParserFactory;
    private final UserPromptParserFactory userPromptParserFactory;
    private final ConcurrentMap<Integer, List<ChatService>> chatServiceMap = Maps.newConcurrentMap();

    public ChatServiceFactory(ApplicationConfig applicationConfig, Fetcher fetcher, IAiModelService aiModelService, IFriendService friendService, PromptService promptService,
                              AccessKeyService accessKeyService, IUserService userService, ITableSchemaService tableSchemaService, IFriendPermissionService friendPermissionService, EventPark eventPark, SystemPromptParserFactory systemPromptParserFactory, UserPromptParserFactory userPromptParserFactory) {
        this.applicationConfig = applicationConfig;
        this.fetcher = fetcher;
        this.aiModelService = aiModelService;
        this.friendService = friendService;
        this.promptService = promptService;
        this.accessKeyService = accessKeyService;
        this.userService = userService;
        this.tableSchemaService = tableSchemaService;
        this.friendPermissionService = friendPermissionService;
        this.eventPark = eventPark;
        this.systemPromptParserFactory = systemPromptParserFactory;
        this.userPromptParserFactory = userPromptParserFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ChatService) {
            ChatService service = (ChatService) bean;
            chatServiceMap.computeIfAbsent(service.platformType(), integer -> Lists.newArrayList()).add(service);
            chatServiceMap.get(service.platformType()).sort(Comparator.comparingInt(Ordered::getOrder));
        }
        return bean;
    }

    public ChatService getFirst(Integer platformType) {
        List<ChatService> chatServices = chatServiceMap.get(platformType);
        if (CollectionUtils.isEmpty(chatServices)) {
            throw new AiStatusException("Unknown platformType: " + platformType);
        }
        return chatServices.get(0);
    }

    public void chat(ChatServiceArgument argument) {
        ChatRequest chatRequest = argument.getChatRequest();
        String roleType = chatRequest.getRoleType();

        // about user
        User user = argument.getUser();
        Long userId = Optional.ofNullable(user).map(User::getId).orElse(null);

        // about prompt
        int productType = chatRequest.toProductTypeByProductId();
        Prompt prompt = promptService.getWithMacros(userId, productType, roleType);
        if (Objects.isNull(prompt)) {
            throw new AiStatusException("Unknown roleType: " + roleType);
        }
        argument.setPrompt(prompt);

        String modelName = applicationConfig.getLlmModelName();

        Friend friend = friendService.getByRoleType(roleType);
        if (Objects.nonNull(friend) && Objects.equals(Constants.YES, friend.getIsPermission())) {
            if (Objects.isNull(user)) {
                throw new AiStatusException("游客不能使用这个好友");
            }
            // 检查权限
            if (!friendPermissionService.isAuthorized(friend.getId(), userId)) {
                log.warn("User [{}] try to use friend [{} - {}] without permission", userId, friend.getId(), friend.getName());
                throw new AiStatusException("对不起，你没有权限访问这个好友，申请权限请联系管理员。");
            }
        }
        argument.setFriend(friend);

        String friendFixedModel = Optional.ofNullable(friend).map(Friend::getFixedModel).orElse(null);

        AiModel aiModel = argument.getAiModel();
        // 如果没有指定模型，那么使用系统设置的模型。
        if (null == aiModel) {
            if (prompt.isFixedKey()) {
                // 如果角色需要固定的Key，那么同时使用基础模型。
                // 并且需要在 api_keys 里配置对应 role_type 的密钥，否则不能使用。
                aiModel = aiModelService.get(modelName);
            } else if (Objects.nonNull(friend) && friend.isAgentFriend()) {
                // 如果是阿里云百炼应用，那么根据 workspace_id 和 app_id 来获取对应的模型。
                modelName = friend.getAgentSpecialModelName();
                aiModel = aiModelService.get(modelName);
                if (Objects.isNull(aiModel)) {
                    // 如果没有配置对应的模型，那么使用一个空的模型。
                    aiModel = AiModel.ofBasic(friend.getAgentImplementAiPlatformType(), modelName, null, Constants.NO);
                }
            } else if (prompt.isGpts()) {
                modelName = Model.GPT_4_GIZMO.getName();
                if (Objects.nonNull(friendFixedModel)) {
                    modelName = friendFixedModel;
                }
                aiModel = aiModelService.get(modelName);
            } else {
                modelName = Optional.ofNullable(friendFixedModel)
                        .orElseGet(() -> Optional.ofNullable(user)
                                .map(User::getSelectedModelName)
                                .orElse(applicationConfig.getLlmModelName()));
                aiModel = aiModelService.get(modelName);
            }
            if (Objects.isNull(aiModel)) {
                throw new AiStatusException(String.format("模型[%s]已经下线了，请切换到其他模型。", modelName));
            }
            argument.setAiModel(aiModel);
        }

        // execute chat flow
        ChatService impl = getFirst(aiModel.getPlatformType());
        boolean notifyIfAccessKeyNotEnough = impl.isNotifyIfAccessKeyNotEnough();

        // check platform server status
        Boolean serverError = fetcher.hfetch(RedisKeys.hashInternalServerError(), TypeHelper.castToString(aiModel.getPlatformType()), Boolean.class);
        if (Optional.ofNullable(serverError).orElse(false)) {
            throw new AiStatusException("当前模型的服务器正在发生故障，请5分钟后再试。或者切换到其他模型。");
        }

        // about access key
        GetAccessKeyParam getAccessKeyParam = new GetAccessKeyParam()
                .setUser(user)
                .setFriend(friend);
        ApiKey key = argument.getApiKey();
        // 如果没有指定密钥，那么使用系统设置的密钥。
        if (null == key) {
            key = accessKeyService.getAvailable(getAccessKeyParam);
        }
        argument.setApiKey(key);

        String accessKey = key.getApiKey();
        if (StringUtils.isBlank(accessKey)) {
            throw new AiStatusException("当前模型的密钥不存在或已经失效了。请5分钟后再试。");
        }

        // 先判断是否有余额
        if (!key.isUserPrivate()) {
            BigDecimal roleTypeCost = Optional.ofNullable(prompt.getCoinCostPer()).orElse(BigDecimal.ZERO);
            BigDecimal modelInputCost = Optional.ofNullable(aiModel.getInputCoins()).orElse(BigDecimal.ZERO);
            BigDecimal modelOutputCost = Optional.ofNullable(aiModel.getOutputCoins()).orElse(BigDecimal.ZERO);
            BigDecimal totalCost = roleTypeCost.add(modelInputCost).add(modelOutputCost).add(roleTypeCost);

            if (totalCost.compareTo(BigDecimal.ZERO) > 0 && Objects.nonNull(user)) {
                BigDecimal coin = userService.getCoin(user.getId());
                if (coin.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new CoinNotEnoughException();
                }
            }
        }

        // 获取table_schema
        TableSchema tableSchema = null;
        Long tableSchemaId = chatRequest.getTableSchemaId();
        if (Objects.nonNull(tableSchemaId)) {
            tableSchema = tableSchemaService.getById(tableSchemaId);
            if (Objects.isNull(tableSchema) || !Objects.equals(userId, tableSchema.getUserId())) {
                throw new AiStatusException(String.format("表结构[%s]不存在，请刷新后重试。", tableSchemaId));
            }
        }

        // replace macro with system
        SystemPromptParser systemPromptParser = Optional.ofNullable(systemPromptParserFactory.getByRoleType(prompt.getRoleType()))
                .orElseGet(systemPromptParserFactory::getVelocitySystemPromptParser);
        systemPromptParser.parse(user, prompt);

        // replace user prompt
        UserPromptParser userPromptParser = Optional.ofNullable(userPromptParserFactory.getByRoleType(prompt.getRoleType()))
                .orElseGet(userPromptParserFactory::getVelocityMergedUserPromptParser);
        List<Message> messages = chatRequest.getMessages();
        Message last = messages.get(messages.size() - 1);
        String userPrompt = last.getContent();
        userPromptParser.parser(user, prompt, messages);

        // replace api parameter, example: o1
        this.replaceApiParameterForModel(aiModel.getModelName(), prompt);

        // replace sql role with content
        this.replaceContentForMacro(chatRequest, prompt, tableSchema);

        int retry = 0;
        while (true) {
            try {
                impl.chat(argument);
                break;
            } catch (AiException e) {
                if (retry >= applicationConfig.getRetryTimes()) {
                    // 已经重试过 2 次，就不处理了
                    throw e;
                }

                retry++;

                if (e.getCause() instanceof ConnectionPoolTimeoutException) {
                    // 连接池不足了，就抛出异常
                    throw e;
                }

                if (e instanceof AiAccountException) {
                    // 如果是账号异常。
                    AiAccountException ae = (AiAccountException) e;

                    // 如果是 400 那请求参数有误，不用重试了。
                    if (ae.getSc() == HttpStatus.BAD_REQUEST.value()) {
                        throw e;
                    }
                    // 如果是私钥，那么就不需要重试了。。
                    if (key.isUserPrivate()) {
                        throw e;
                    }

                    // 使用 oneapi 代理管理ApiKey，所以这里同一个Key继续重试。
//                    getAccessKeyParam.setExclude(Sets.newHashSet(apiKey));
//                    ApiKey nextKey = accessKeyService.getAvailable(getAccessKeyParam);
//                    // 没有密钥就不需要重试了。
//                    if (StringUtils.isBlank(nextKey.getApiKey())) {
//                        throw e;
//                    }
//                    argument.setApiKey(nextKey);

                    // 发送重试的事件
                    eventPark.post(new ChatRetryEvent(argument, ae));
                }

                log.warn("Retry with AiException: {} - {}" + argument.getId(), e.getClass(), e.getMessage());
            }
        }
    }

    private void replaceApiParameterForModel(String model, Prompt prompt) {
        if (StringUtils.startsWith(model, "o1")) {
            // Currently unsupported API parameters: temperature, top_p, presence_penalty, frequency_penalty, logprobs, top_logprobs, logit_bias
            OpenApiRequest request = prompt.getRequest();
            if (Objects.nonNull(request)) {
                if (Objects.isNull(request.getMaxCompletionTokens())) {
                    request.setMaxCompletionTokens(request.getMaxTokens());
                }
                request.setMaxTokens(null);
                request.setTemperature(null);
                request.setTopP(null);
                request.setPresencePenalty(null);
                request.setFrequencyPenalty(null);
                request.setLogitBias(null);
            }
        }
    }

    private void replaceContentForMacro(ChatRequest chatRequest, Prompt prompt, TableSchema tableSchema) {
        if (prompt.isEmptyContentPrompt()) {
            return;
        }
        List<Message> messages = chatRequest.getMessages();
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        String contentPrompt = prompt.getContentPrompt();
        if (StringUtils.isBlank(contentPrompt)) {
            return;
        }

        Message last = messages.get(messages.size() - 1);
        String content = last.getContent();

        // 固定宏
        contentPrompt = replaceTableMetadata(contentPrompt, tableSchema);

        last.setContent(contentPrompt);

        log.debug("Replace content prompt: {} -> {}", content, contentPrompt);
    }

    private String replaceTableMetadata(String contentPrompt, TableSchema tableSchema) {
        if (Objects.isNull(tableSchema)) {
            return contentPrompt;
        }
        contentPrompt = contentPrompt.replaceAll(Pattern.quote("{{table_metadata_string}}"), tableSchema.getSchema());
        contentPrompt = contentPrompt.replaceAll(Pattern.quote("{{sql_type}}"), tableSchema.getSqlType());
        return contentPrompt;
    }
}
