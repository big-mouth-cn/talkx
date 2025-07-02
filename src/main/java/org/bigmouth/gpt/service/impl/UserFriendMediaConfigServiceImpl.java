package org.bigmouth.gpt.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bigmouth.gpt.entity.UserFriendMediaConfig;
import org.bigmouth.gpt.entity.response.AudioConfigVo;
import org.bigmouth.gpt.mapper.talkx.UserFriendMediaConfigMapper;
import org.bigmouth.gpt.service.IUserFriendMediaConfigService;
import org.bigmouth.gpt.utils.ResourceFileUtils;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiAlibabaConfig;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiByteDanceConfig;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiTalkXConfig;
import org.bigmouth.gpt.xiaozhi.tts.TtsPlatformType;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author allen
 * @since 2025-02-27
 */
@Service
public class UserFriendMediaConfigServiceImpl extends ServiceImpl<UserFriendMediaConfigMapper, UserFriendMediaConfig> implements IUserFriendMediaConfigService {

    private final XiaozhiAlibabaConfig xiaozhiAlibabaConfig;
    private final XiaozhiByteDanceConfig xiaozhiByteDanceConfig;
    private final XiaozhiTalkXConfig xiaozhiTalkXConfig;

    public UserFriendMediaConfigServiceImpl(XiaozhiAlibabaConfig xiaozhiAlibabaConfig, XiaozhiByteDanceConfig xiaozhiByteDanceConfig, XiaozhiTalkXConfig xiaozhiTalkXConfig) {
        this.xiaozhiAlibabaConfig = xiaozhiAlibabaConfig;
        this.xiaozhiByteDanceConfig = xiaozhiByteDanceConfig;
        this.xiaozhiTalkXConfig = xiaozhiTalkXConfig;
    }

    @Override
    public UserFriendMediaConfig getByUserFriendId(Long userFriendId) {
        QueryWrapper<UserFriendMediaConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(UserFriendMediaConfig.USER_FRIEND_ID, userFriendId);
        return getOne(queryWrapper);
    }

    @Override
    public AudioConfigVo getDefConfig(String roleType) {
        String json = ResourceFileUtils.fetch("tts-def.json");
        return Optional.ofNullable(json)
                .map(JSONObject::parseObject)
                .map(jsonObject -> jsonObject.getObject(roleType, AudioConfigVo.class))
                .orElseGet(new Supplier<AudioConfigVo>() {
                    @Override
                    public AudioConfigVo get() {
                        if (!xiaozhiAlibabaConfig.isDisable()) {
                            return new AudioConfigVo()
                                    .setPlatformType(TtsPlatformType.Alibaba)
                                    .setModel(xiaozhiAlibabaConfig.getCosyVoiceDefaultModel())
                                    .setRole(xiaozhiAlibabaConfig.getCosyVoiceDefaultVoice());
                        }
                        if (!xiaozhiByteDanceConfig.isDisable()) {
                            return new AudioConfigVo()
                                    .setPlatformType(TtsPlatformType.ByteDance)
                                    .setRole(xiaozhiByteDanceConfig.getDefaultVoice());
                        }
                        return new AudioConfigVo()
                                .setPlatformType(xiaozhiTalkXConfig.getDefaultTtsPlatformType())
                                .setModel(xiaozhiTalkXConfig.getDefaultVoiceModel())
                                .setRole(xiaozhiTalkXConfig.getDefaultVoiceRole());
                    }
                });
    }
}
