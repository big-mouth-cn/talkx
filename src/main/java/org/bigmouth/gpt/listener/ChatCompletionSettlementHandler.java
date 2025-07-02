package org.bigmouth.gpt.listener;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bxm.warcar.integration.eventbus.EventListener;
import com.bxm.warcar.integration.eventbus.core.AllowConcurrentEvents;
import com.bxm.warcar.integration.eventbus.core.Subscribe;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.ai.entity.ApiKey;
import org.bigmouth.gpt.ai.entity.Message;
import org.bigmouth.gpt.ai.entity.Usage;
import org.bigmouth.gpt.entity.AiModel;
import org.bigmouth.gpt.entity.Friend;
import org.bigmouth.gpt.entity.Prompt;
import org.bigmouth.gpt.entity.User;
import org.bigmouth.gpt.event.ChatCompletionEvent;
import org.bigmouth.gpt.service.CoinService;
import org.bigmouth.gpt.service.IFriendService;
import org.bigmouth.gpt.utils.Constants;
import org.bigmouth.gpt.utils.TikTokensUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 聊天完成结算处理器。
 *
 * @author huxiao
 * @date 2023/11/17
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class ChatCompletionSettlementHandler implements EventListener<ChatCompletionEvent> {

    private final CoinService coinService;
    private final IFriendService friendService;

    public ChatCompletionSettlementHandler(CoinService coinService, IFriendService friendService) {
        this.coinService = coinService;
        this.friendService = friendService;
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void consume(ChatCompletionEvent event) {
        ChatCompletionEvent.Parameter parameter = event.getParameter();
        User user = parameter.getUser();
        // 判断模型是否收费
        ApiKey apiKey = parameter.getApiKey();
        if (apiKey.isUserPrivate()) {
            return;
        }
        Prompt prompt = parameter.getPrompt();
        String roleType = prompt.getRoleType();

        AiModel aiModel = parameter.getAiModel();
        List<String> feeItems = Lists.newArrayList();

        BigDecimal totalCoins = BigDecimal.ZERO;

        // 默认是按模型收费，如果角色按次收费，那么将不按模型收费了
        String title = aiModel.getModelName();

        // 计算角色消耗
        BigDecimal promptCost = prompt.getCoinCostPer();
        boolean settleOnPer = isGreaterThanZero(promptCost);
        if (settleOnPer) {
            totalCoins = totalCoins.add(promptCost);
            feeItems.add(String.format("好友：(%d 次)", 1));
            title = roleType;
        }

        // 计算Input
        BigDecimal inputCoins = aiModel.getInputCoins();
        if (!settleOnPer && isGreaterThanZero(inputCoins)) {
            int requestTokens = computeInputTokens(parameter);
            BigDecimal inputCost = TikTokensUtils.computePrice(requestTokens, inputCoins);
            totalCoins = totalCoins.add(inputCost);
            feeItems.add(String.format("提问：(%d tokens)", requestTokens));
        }

        // 计算Output
        BigDecimal outputCoins = aiModel.getOutputCoins();
        if (!settleOnPer && isGreaterThanZero(outputCoins)) {
            int completionTokens = computeCompletionTokens(parameter);
            BigDecimal outputCost = TikTokensUtils.computePrice(completionTokens, outputCoins);
            totalCoins = totalCoins.add(outputCost);
            feeItems.add(String.format("回答：(%d tokens)", completionTokens));
        }

        if (!isGreaterThanZero(totalCoins)) {
            return;
        }

        String billDesc = createBillDesc(feeItems, totalCoins);

        // 扣除余额
        if (Objects.nonNull(user)) {
            int type = Constants.Coin.BILL_TYPE_USE;
            Long userId = user.getId();
            boolean success = coinService.minus(userId, type, totalCoins, title, billDesc);
            if (success) {
                log.info("用户的蒜粒扣除成功。UserID={}, Type={}, Total={}, Title={}, Desc={}",
                        userId, type, totalCoins, title, billDesc);
            } else {
                log.warn("用户的蒜粒扣除失败！UserID={}, Type={}, Total={}, Title={}, Desc={}",
                        userId, type, totalCoins, title, billDesc);
            }
        }
    }

    private int computeInputTokens(ChatCompletionEvent.Parameter parameter) {
        Usage usage = parameter.getUsage();
        if (null != usage && usage.getPromptTokens() > 0) {
            return usage.getPromptTokens();
        }
        Message lastUserMessage = parameter.getLastUserMessage();
        if (null != lastUserMessage) {
            String modelName = parameter.getAiModel().getModelName();
            return TikTokensUtils.tokens(modelName, lastUserMessage.getContent());
        }
        return 0;
    }

    private int computeCompletionTokens(ChatCompletionEvent.Parameter parameter) {
        Usage usage = parameter.getUsage();
        if (null != usage && usage.getCompletionTokens() > 0) {
            return usage.getCompletionTokens();
        }
        String modelName = parameter.getAiModel().getModelName();
        return TikTokensUtils.tokens(modelName, parameter.getCompletion());
    }

    private boolean isFriend(String roleType) {
        return friendService.count(Wrappers.query(new Friend().setRoleType(roleType))) > 0;
    }

    @NotNull
    private static String createBillDesc(List<String> feeItems, BigDecimal totalCoins) {
        StringBuilder title = new StringBuilder();
        // 创建订单说明
        int size = feeItems.size();
        for (int i = 0; i < size; i++) {
            String feeItem = feeItems.get(i);
            title.append(feeItem);
            if (i != size - 1) {
                title.append("、");
            }
        }
        title.append(String.format("，合计：%s 蒜粒", totalCoins.stripTrailingZeros().toPlainString()));
        return title.toString();
    }

    private static boolean isGreaterThanZero(BigDecimal value) {
        if (Objects.isNull(value)) {
            return false;
        }
        return value.compareTo(BigDecimal.ZERO) > 0;
    }
}
