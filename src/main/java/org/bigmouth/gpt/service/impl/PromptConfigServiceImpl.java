package org.bigmouth.gpt.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.bigmouth.gpt.entity.PromptConfig;
import org.bigmouth.gpt.mapper.talkx.PromptConfigMapper;
import org.bigmouth.gpt.service.IPromptConfigService;
import org.bigmouth.gpt.utils.Constants;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Allen Hu
 * @since 2023-06-30
 */
@Slf4j
@Service
public class PromptConfigServiceImpl extends ServiceImpl<PromptConfigMapper, PromptConfig> implements IPromptConfigService {
    @Override
    public PromptConfig getOne(String roleType) {
        PromptConfig condition = new PromptConfig().setRoleType(roleType).setDeleted(Constants.NO_DELETE);
        List<PromptConfig> promptConfigs = list(Wrappers.query(condition).orderByDesc(PromptConfig.ID));
        if (CollectionUtils.isEmpty(promptConfigs)) {
            return null;
        }
        int size = promptConfigs.size();
        if (size > 1) {
            log.warn("{} prompt config count is {}", roleType, size);
        }
        return promptConfigs.get(0);
    }

    @Override
    public String createRoleType() {
        String roleType = "r-" + RandomStringUtils.randomAlphabetic(10);
        long count = count(Wrappers.query(new PromptConfig().setRoleType(roleType)));
        if (count != 0) {
            return createRoleType();
        }
        return roleType;
    }
}
