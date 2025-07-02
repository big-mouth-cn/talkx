import { ref } from 'vue'
import { isInteger } from "@/common/utils";
import { default_friend_avatar } from "@/common/config";

export const formRef = ref(null)

// 添加来源
export const SourceType = {
    plaza: 1, // 1 AI广场
    created: 2, //2  自建
}

export const defaultFormData = {
    userFriendId: 0,
    avatar: default_friend_avatar,
    cssAvatar: "",
    name: "",
    friendType: 1, // 新增类型字段，默认为基础对话
    tag: "",
    intro: "",
    welcome: "",
    systemPrompt: "",
    contentPrompt: "",
    messageContextSize: 32,
    friendSource: SourceType.created,  
    conversationStart: [''],
    aliyunDashscopeWorkspaceId: "",
    aliyunDashscopeAppId: "",
    aliyunDashscopeApiKey: "",
    cozeBotId: "",
    cozeAccessToken: "",
    variables: "",
    openaiRequestBody: {
        maxTokens: 1000,
        temperature: 1.0,
        topP: 0,
        presencePenalty: 0,
        frequencyPenalty: 0,
    }
}

export const formData = ref(JSON.parse(JSON.stringify(defaultFormData)))

const verify = (val, attrs) => {
    let v = val
    const { step, max, min } = attrs
    if (step) {
        if (isInteger(step)) {
            const n = Math.floor(val / step)
            v = step * n
        } else {
            // 带小数处理
            const vstr = String(step)
            const [integer, decimals] = vstr.split('.')
            let scale = 1
            for (let i = 0; i < decimals.length; i++) { scale *= 10 }
            const newStep = step * scale
            const n = Math.floor(val * scale / newStep)
            v = newStep * n / scale
        }
    }
    if (max) { v = v > max ? max : v; }
    if (min) { v = v < min ? min : v; }
    return v
}

export const config = {
    messageContextSize: {
        label: '上下文数量',
        popover: "每次聊天发送消息时携带的历史消息数量越多，话题关联性就越高；反之，数量越少，关联性就越低。如果设置的消息数量超过模型的最大限制，系统会自动截取，以确保话题的正常进行。",
        attrs: { min: 2, max: 64, step: 2 },
        stepVerify: (val) => verify(Number(val), config.messageContextSize.attrs)
    },
    openaiRequestBody: {
        maxTokens: {
            label: '最大回复数',
            popover: "生成内容的最大 token 数量。输入和生成的总长度受模型上下文长度的限制。",
            attrs: { min: 100, max: 2800, step: 50 },
            stepVerify: (val) => verify(Number(val), config.openaiRequestBody.maxTokens.attrs)
        },
        temperature: {
            label: '随机性',
            popover: "生成内容的随机性，在0和2之间。较高的值如0.8会使输出更随机，而较低的值如0.2会使其更加集中和确定性。我们通常建议修改这个或者「词汇熟悉」，但不要同时修改两者。",
            attrs: { min: 0, max: 2, step: 0.1 },
            stepVerify: (val) => verify(Number(val), config.openaiRequestBody.temperature.attrs)
        },
        topP: {
            label: "词汇属性",
            popover: "一种与随机性相对的方法，模型考虑的是具有 top_p 概率质量的标记的结果。因此，0.1 表示只考虑组成前 10% 概率质量的标记。我们通常建议修改这个或者「随机性」，但不要同时修改两者。",
            attrs: { min: 0, max: 1, step: 0.1 },
            stepVerify: (val) => verify(Number(val), config.openaiRequestBody.topP.attrs)
        },
        presencePenalty: {
            label: "话题新鲜度",
            popover: "默认值为 0，值越大，越能够让Ai更好地控制新话题的引入，建议微调或不变。",
            attrs: { min: -2, max: 2, step: 0.1 },
            stepVerify: (val) => verify(Number(val), config.openaiRequestBody.presencePenalty.attrs)
        },
        frequencyPenalty: {
            label: "频率惩罚度",
            popover: "在-2.0和2.0之间的数字。正值根据文本中新标记的现有频率对其进行惩罚，从而降低模型重复相同行的可能性。",
            attrs: { min: -2, max: 2, step: 0.1 },
            stepVerify: (val) => verify(Number(val), config.openaiRequestBody.frequencyPenalty.attrs)
        },
    }
}

export const rules = {
    name: {
        required: true,
        message: ' ',
        trigger: 'blur'
    },
    aliyunDashscopeWorkspaceId: {
        required: () => formData.value.friendType === 3,
        message: ' ',
        trigger: 'blur'
    },
    aliyunDashscopeAppId: {
        required: () => formData.value.friendType === 3,
        message: ' ',
        trigger: 'blur'
    },
    cozeBotId: {
        required: () => formData.value.friendType === 4 || formData.value.friendType === 5,
        message:'',
        trigger: 'blur'
    },
    cozeAccessToken: {
        required: () => formData.value.friendType === 4 || formData.value.friendType === 5,
        message:'',
        trigger: 'blur'
    },
}

export const formDataClear = () => {
    formData.value = JSON.parse(JSON.stringify(defaultFormData))
}