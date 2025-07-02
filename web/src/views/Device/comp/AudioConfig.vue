<template>
    <div v-if="!loading">
        <n-form class="formComp" ref="formRef" :model="formData" :rules="rules" label-placement="left" :label-width="112"
            require-mark-placement="right-hanging" size="medium">
            <div class="diver">声音设置</div>
            <n-form-item label="声音角色" path="role">
                <n-select class="tp_select" v-model:value="formData.role" :options="audioRoleOptions" @update:value="handleRoleChange" :loading="loading" />
            </n-form-item>
            <n-form-item label="试听" v-if="currentDemoUrl">
                <audio :src="currentDemoUrl" controls></audio>
            </n-form-item>

            <div class="diver">模型设置</div>
            <n-form-item label="自定义模型">
                <n-space>
                    <n-switch v-model:value="formData.customModel" :checked-value="1" :unchecked-value="0" />
                </n-space>
            </n-form-item>
            <div v-if="formData.customModel === 1">
                <n-form-item label=" " style="color: #b7b7b7; font-size: 12px; margin-top: -20px;" v-if="formData.friendType === 3">
                    💬 当前 {{ formData.friendName }} 已经是阿里云百炼应用，不建议再自定义模型。如果你一定要这么做，配置后将不会调用阿里云百炼应用。
                </n-form-item>
                <n-form-item label="模型地址" path="proxyBaseUrl">
                    <n-input class="_input" v-model:value="formData.proxyBaseUrl" placeholder="请输入模型的完整请求地址，如：https://api.aigateway.work/v1/chat/completions" />
                </n-form-item>
                <n-form-item label=" " style="color: #b7b7b7; font-size: 12px; margin-top: -20px;">
                        自定义模型服务，必须支持OpenAI协议。
                </n-form-item>
                <n-form-item label="模型密钥" path="apiKey">
                    <n-input class="_input" v-model:value="formData.apiKey" placeholder="请输入模型密钥，如：sk-8P7p27Stw5lkMn0jhHx6u6g8hfa7zXO1Jw8CPofz" />
                </n-form-item>
            </div>
            <div v-if="formData.customModel === 1 || formData.friendType === 1">
                <n-form-item label="设置模型">
                    <n-select
                        v-if="formData.customModel === 1"
                        class="tp_select"
                        v-model:value="formData.llmModel"
                        :options="modelOptions"
                        filterable
                        tag
                        @update:value="handleModelChange"
                    />
                    <n-select
                        v-if="formData.customModel === 0"
                        class="tp_select"
                        v-model:value="formData.llmModel"
                        :options="modelOptions"
                        @update:value="handleModelChange"
                    />
                </n-form-item>
                <n-form-item label=" " style="color: #b7b7b7; font-size: 12px; margin-top: -20px;">
                        设置这个AI的模型，仅使用“智体”绑定的设备聊天时有效。使用自定义模型时允许自由输入。
                </n-form-item>
                <n-form-item label="是否支持IoT">
                    <n-space>
                        <n-switch v-model:value="formData.isSupportTool" :checked-value="1" :unchecked-value="0" />
                    </n-space>
                </n-form-item>
            </div>
            <n-form-item>
                <div class="footer flex">
                    <n-button v-if="formData.customModel === 1" class="testbtn" type="default" @click="handleTest" :loading="testing">
                        测试自定义模型服务
                    </n-button>
                    <n-button  class="_confirm_btn" type="info" @click="handleSubmit" :loading="submitting">保存</n-button>
                </div>
            </n-form-item>
        </n-form>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { NForm, NFormItem, NSelect, NIcon, NInput, NButton, NSpace, NSwitch, useMessage } from 'naive-ui';
import server from '@/api/require';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const message = useMessage();
const formRef = ref(null);
const audioRoleOptions = ref([]);
const modelOptions = ref([]);
const currentDemoUrl = ref('');
const testing = ref(false);
const submitting = ref(false);

const formData = ref({
    role: '',
    customModel: 0,
    proxyBaseUrl: '',
    apiKey: '',
    llmModel: ref(null),
    isSupportTool: 0,
    friendType: 1,
    friendName: ''
});

const rules = {
    role: {
        required: true,
        message: '请选择声音角色',
        trigger: ['blur', 'change']
    },
    proxyBaseUrl: {
        required: computed(() => formData.value.customModel === 1),
        message: '',
        trigger: ['blur', 'change']
    },
    apiKey: {
        required: computed(() => formData.value.customModel === 1),
        message: '',
        trigger: ['blur', 'change']
    }
};
const loading = ref(false);

const getCurrentConfig = async () => {
    try {
        const res = await server({
            url: '/friend/media/get',
            method: 'GET',
            params: {
                userFriendId: route.query.userFriendId
            }
        });
        if (res) {
            const { userFriendMediaConfig, user, friendVo } = res;
            
            formData.value = {
                role: userFriendMediaConfig.audioRole,
                customModel: userFriendMediaConfig.customModel,
                llmModel: userFriendMediaConfig.llmModel,
                proxyBaseUrl: userFriendMediaConfig.proxyBaseUrl,
                apiKey: userFriendMediaConfig.apiKey,
                isSupportTool: userFriendMediaConfig.isSupportTool,
                friendType: friendVo.friendType,
                friendName: friendVo.name
            };
            return res;
        }
        return null;
    } catch (err) {
        message.error(err.response?.data || '获取当前配置失败');
        return null;
    }
};

// 获取用户复刻的声音列表
const getUserVoiceList = async () => {
    try {
        const res = await server({
            url: '/user/voice/list',
            method: 'GET'
        });

        if (res && res.length > 0) {
            // 处理用户复刻的声音数据
            const userVoices = res.map(item => ({
                label: item.voiceName,
                value: item.audioRole,
                demoUrl: item.voiceSrcUrl,
                platformType: item.audioPlatformType || 'Alibaba',
                model: item.audioModel || 'cosyvoice-v1',
                isUserVoice: true // 标记为用户复刻的声音
            }));

            // 如果有用户复刻的声音，创建一个分组
            if (userVoices.length > 0) {
                return [{
                    type: 'group',
                    label: '我的声音',
                    key: 'user-voices',
                    children: userVoices
                }];
            }

            return [];
        }
        return [];
    } catch (err) {
        console.error('获取用户复刻声音失败:', err);
        return [];
    }
};

const getModelList = async () => {
    // 获取模型列表
    const modelRes = await server({
        url: '/user/info/list_model',
        method: 'GET'
    });
    modelOptions.value = modelRes.map(item => ({
        label: item.model,
        value: item.model,
        icon: item.icon,
        costCoin: item.costCoin,
        canSelection: item.canSelection,
        supportTool: item.supportTool
    }));
}

const getVoiceConfig = async () => {
    try {
        loading.value = true;

        // 获取用户复刻的声音列表
        const userVoices = await getUserVoiceList();

        const res = await server({
            url: '/friend/media/audio_configs',
            method: 'GET'
        });
        if (res && res.length > 0) {
            // 处理系统声音数据
            const systemVoices = res.map(item => ({
                label: item.name + ' (' + item.languages + ')',
                value: item.role,
                demoUrl: item.demoUrl,
                platformType: item.platformType || 'Alibaba',
                model: item.model || 'cosyvoice-v1',
                isUserVoice: false // 标记为系统声音
            }));

            // 创建系统声音分组
            const systemVoicesGroup = {
                type: 'group',
                label: '系统声音',
                key: 'system-voices',
                children: systemVoices
            };

            // 合并用户复刻声音和系统声音分组
            audioRoleOptions.value = [...userVoices, systemVoicesGroup];
            // 如果没有当前选中的角色，设置第一个选项为默认值
            if (!formData.value.role) {
                const firstGroup = audioRoleOptions.value[0];
                if (firstGroup.type === 'group' && firstGroup.children?.length > 0) {
                    formData.value.role = firstGroup.children[0].value;
                    currentDemoUrl.value = firstGroup.children[0].demoUrl;
                }
            } else {
                // 如果有当前选中的角色，设置对应的试听URL
                const selectedRole = findRoleInOptions(formData.value.role, audioRoleOptions.value);
                if (selectedRole) {
                    currentDemoUrl.value = selectedRole.demoUrl;
                }
            }
        }
    } catch (err) {
        message.error(err.response?.data || '获取配置失败');
    } finally {
        loading.value = false;
    }
};

const findRoleInOptions = (value, options) => {
    for (const group of options) {
        if (group.type === 'group' && group.children) {
            const found = group.children.find(item => item.value === value && !item.disabled);
            if (found) return found;
        } else if (group.value === value && !group.disabled) {
            return group;
        }
    }
    return null;
};

const handleRoleChange = (value) => {
    const selectedRole = findRoleInOptions(value, audioRoleOptions.value);
    if (selectedRole) {
        currentDemoUrl.value = selectedRole.demoUrl;
    }
};

const handleModelChange = (value) => {
    const selectedModel = modelOptions.value.find(option => option.value === value);
    if (selectedModel) {
        formData.value.isSupportTool = selectedModel.supportTool ? 1 : 0;
    }
};

const handleTest = async () => {
    await formRef.value?.validate();
    testing.value = true;
    
    server({
        url: '/friend/media/test',
        method: 'POST',
        data: {
            llmModel: formData.value.llmModel,
            proxyBaseUrl: formData.value.proxyBaseUrl,
            apiKey: formData.value.apiKey
        }
    }).then((res) => {
        message.success('测试通过！');
    }).catch((err) => {
        message.error(err.response?.data || '测试失败');
    }).finally(() => {
        testing.value = false;
    });
}

const handleSubmit = async () => {
    try {
        await formRef.value?.validate();
        submitting.value = true;

        // 获取当前选中角色的信息
        const selectedRole = findRoleInOptions(formData.value.role, audioRoleOptions.value);
        if (!selectedRole) {
            throw new Error('未找到选中的声音角色');
        }

        const audioDemoUrl = selectedRole.demoUrl || '';
        const audioPlatformType = selectedRole.platformType || 'Alibaba';
        const audioModel = selectedRole.model || 'cosyvoice-v1';

        const res = await server({
            url: '/friend/media/update',
            method: 'POST',
            data: {
                userFriendId: route.query.userFriendId,
                audioPlatformType,
                audioModel,
                audioRole: formData.value.role,
                audioDemoUrl,
                customModel: formData.value.customModel,
                llmModel: formData.value.llmModel,
                proxyBaseUrl: formData.value.proxyBaseUrl,
                apiKey: formData.value.apiKey,
                isSupportTool: formData.value.isSupportTool
            }
        });

        message.success('保存成功');
    } catch (err) {
        if (err.response?.data) {
            message.error(err.response.data);
        } else {
            message.error('保存失败');
        }
    } finally {
        submitting.value = false;
    }
};

onMounted(async () => {
    loading.value = true;
    try {
        // 获取模型列表
        await getModelList();

        // 获取音频角色列表
        await getVoiceConfig();

        // 再获取当前配置
        const config = await getCurrentConfig();
        // 如果有配置数据，使用配置中的数据
        let defaultSelectRole = null;
        if (config) {
            // 需要匹配audioPlatformType、audioModel和audioRole三个属性
            // 先尝试精确匹配三个属性
            let selectedRole = findRoleInOptions(config.userFriendMediaConfig.audioRole, audioRoleOptions.value);
            defaultSelectRole = selectedRole;
        } 
        if (!defaultSelectRole) {
            if (audioRoleOptions.value.length > 0) {
                // 如果没有配置数据，使用第一个可用的角色作为默认值
                const firstGroup = audioRoleOptions.value[0];
                if (firstGroup.type === 'group' && firstGroup.children?.length > 0) {
                    defaultSelectRole = firstGroup.children[0];
                }
            }
        }
        if (defaultSelectRole) {
            // 找到匹配的选项
            formData.value.role = defaultSelectRole.value;
            currentDemoUrl.value = defaultSelectRole.demoUrl;
        }
    } catch (err) {
        message.error('初始化失败');
    } finally {
        loading.value = false;
    }
});
</script>

<style lang="scss" scoped>
.formComp {
    color: var(--lebel_color);
    padding: 0px 20px 20px 20px;

    audio {
        height: 30px;
    }

    .diver {
        font-size: 15px;
        line-height: 30px;
        padding: 15px 0px;
    }

    .flex {
        width: 100%;
        align-content: center;
    }

    .slider {
        flex: 1;
        margin-top: 8px;
    }

    .item_contet {
        width: 100%;

        .desc {
            line-height: 30px;
            margin-bottom: -20px;
            color: var(--border_color);

            .model_look {
                cursor: pointer;
            }
        }

        .rowInput {
            display: flex;
            margin-bottom: 5px;

            &:nth-last-child(2) {
                margin-bottom: 0px;
            }

            ._close_btn {
                width: 40px;
            }
        }
    }

    .footer {
        width: 100%;
        text-align: center;
        justify-content: right;
        margin-top: 20px;

        button {
            min-width: 100px;
            margin-left: 15px;
        }

        .testbtn {
            width: v-bind(itemW);
            min-width: 100px;
            cursor: pointer;
            line-height: 30px;
            text-align: center;
            border: 0px !important;

            color: var(--tp_FriendCard_color);
            background-color: var(--tp_FriendCard_bg_color);

            &:hover {
                color: var(--tp_FriendCard_hover_color);
                background: var(--tp_FriendCard_hover_bg_color);
                border: 0px !important;
            }
            &.active {
                color: var(--tp_FriendCard_active_color);
                background: var(--tp_FriendCard_active_bg_color);
                border: 0px !important;
            }
        }
    }

    .modelSet {
        &::v-deep(.n-form-item-feedback-wrapper) {
            min-height: 15px;
        }
    }

    &::v-deep(.n-form-item) {
        .n-form-item-label {
            color: var(--lebel_color);
        }

        .n-input {
            .n-input__border {}
        }

        .n-form-item-blank {
            .n-radio-group {
                .n-radio {
                    .n-radio__label {
                        color: var(--tp_aside_color);
                    }
                }
            }
        }
        &::v-deep(.n-select-menu .n-base-select-option) {
            color: var(--tp_textcolor);
        }
    }
    &::v-deep(.n-base-selection) {
        .n-base-selection-label {
            .n-base-selection-label__render-label {
                color: var(--tp_textcolor) !important;
            }
        }
    }

}
</style>
