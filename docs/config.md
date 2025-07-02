## 配置文件说明

### config

基础配置项

| 属性名 | 默认值 | 说明                               |
| --- | --- |----------------------------------|
| config.llm-api-host | https://api.aigateway.work| 大模型服务接口地址                        |
| config.llm-api-key| sk-... | 大模型服务接口API                       |
| aliyun-dashscope-api-key | sk-... | 阿里云百炼平台接口API。创建的AI类型是阿里云百炼应用时使用。 |
| config.enable-redis-cache | false | 是否开启Redis。                       |
| config.jedis.host | 127.0.0.1 | Redis服务器连接IP                     |
| config.jedis.password | | Redis服务器连接密码                     | 
| config.jedis.database | 0 | Redis db                         |

**Redis 开启的区别**

| 开启 | 不开启 |
| --- | --- |
| 会话、登录状态保持 | 重启后丢失 |
| 查询性能更高 | 查询性能低 |
| 支持集群部署 | 不支持，会造成会话不共享、语音不互通等问题 |

### xiaozhi

小智智能设备相关配置项

> 下面关于使用 TalkX 作为服务器类别的（包括MQTT、asr、TTS等），我们只是提供了一个免费测试的代理。不保证服务可用性，请根据实际情况选择。

| 属性名            | 默认值 | 说明                                   |
|----------------| --- |--------------------------------------|
| xiaozhi.enable | false | 是否开启小智服务。当 `true` 时才会启动 MQTT 和 UDP 连接。 |

#### udp

| 属性名 | 默认值 | 说明 |
| --- | --- | --- |
| xiaozhi.udp.udp-server-host | 127.0.0.1 | 接收小智设备语音的UDP服务器IP |
| xiaozhi.udp.udp-server-port | 8884 | 接收小智设备语音的UDP服务器端口 |

#### mqtt

| 属性名 | 默认值 | 说明 |
| --- | --- | --- |
| xiaozhi.mqtt.type | talkx | MQTT服务器类别，可选值：`talkx`、`aliyun_ons` |
| xiaozhi.mqtt.instance-id | | 阿里云MQTT实例ID |
| xiaozhi.mqtt.endpoint | | 阿里云MQTT endpoint |
| xiaozhi.mqtt.cloud-port | 5672 | 阿里云MQTT 云开发端口|
| xiaozhi.mqtt.access-key | | 阿里云AK |
| xiaozhi.mqtt.secret-key | | 阿里云AK Secret Key |
| xiaozhi.mqtt.client-id-prefix-for-device | GID_test@@@ | 阿里云MQTT 客户端ID前缀 |
| xiaozhi.mqtt.topic-of-server | device-server | 阿里云MQTT接收客户端消息的主题 |
| xiaozhi.mqtt.topic-of-device | devices | 阿里云MQTT发送客户端的p2p主题 |

类别选择 `talkx` 时，必须配置：`forest.variables.talkxCenterBaseUrl`

#### asr

语音识别配置

| 属性名 | 默认值 | 说明 |
| --- | --- | --- |
| xiaozhi.asr.type | talkx | 语音识别服务类别，可选值：`talkx`、`sensevoice_fastapi` |

类别选择 `talkx` 时，必须配置：`forest.variables.talkxCenterBaseUrl`
类别选择`sensevoice_fastapi`时，必须配置：`forest.variables.sensevoiceFastApiUrl`

> 对接 SenseVoice 的实现是官方提供的 [FastAPI接口](https://github.com/FunAudioLLM/SenseVoice/blob/main/api.py)，所以当你自己部署的SenseVoice时，需要使用这个API来启动。

#### 声音复刻

声音复刻配置

| 属性名 | 默认值 | 说明                             |
| --- | --- |--------------------------------|
| xiaozhi.voice.reprint.type | talkx | 声音复刻服务类别，可选值：`talkx`、`alibaba` |

类别选择 `talkx` 时，必须配置：`forest.variables.talkxCenterBaseUrl`

#### silero

语音活动监测配置

| 属性名 | 默认值 | 说明 |
| --- | --- | --- |
| xiaozhi.silero.model-path | ../conf/silero_vad.onnx | VAD 活动监测模型路径 |
| xiaozhi.silero.start-threshold | 0.6 | 监测到说话开始的阈值 |
| xiaozhi.silero.end-threshold | 0.45 | 监测到说话沉默的阈值，低于该阈值的声音代表沉默 |
| xiaozhi.silero.min-silence-duration-ms | 1200 | 沉默超时时间，单位：毫秒。示例1.2秒内如果没有继续说话代表结束 |
| xiaozhi.silero.timeout-sec | 10 | 超时时间，单位：秒。指定时间内从开始监听一直未说话则结束对话 |

#### alibaba

开启这个配置后，小智通过这个账号使用阿里云百炼的音色、声音复刻功能。

| 属性名 | 默认值 | 说明 |
| --- | --- | --- |
| xiaozhi.alibaba.dashscope-api-key | | 阿里云百炼API Key |
| xiaozhi.alibaba.cosy-voice-default-model | cosyvoice-v2 | 阿里云语音生成默认模型 |
| xiaozhi.alibaba.cosy-voice-default-voice | longxiaoxia_v2 | 阿里云语音生成默认音色 |

#### bytedance

开启这个配置后，小智通过这个账号使用字节火山引擎的音色、声音复刻功能。

| 属性名 | 默认值 | 说明 |
| --- | --- | --- |
| xiaozhi.bytedance.tts-url | wss://openspeech.bytedance.com/api/v1/tts/ws_binary | 火山引擎TTS服务器地址 |
| xiaozhi.bytedance.app-id | | 火山引擎APP id |
| xiaozhi.bytedance.access-token | | 火山引擎APP access token |
| xiaozhi.bytedance.default-voice | zh_female_wanwanxiaohe_moon_bigtts | 火山引擎语音生成默认音色 |

#### talkx

TalkX 相关配置，包括语言模型、音色、声音复刻等。

| 属性名 | 默认值 | 说明                                   |
| --- | --- |--------------------------------------|
| default-tts-platform-type | bytedance | 默认的TTS平台类型，可选值：`bytedance`、`alibaba` |
| default-voice-model |  | 默认的音色模型 |
| default-voice-role | zh_female_wanwanxiaohe_moon_bigtts | 默认的音色 |
| tts-stream-buffer-size | 8000 | TTS流式缓冲区大小，单位：字节 |

当未设置 alibaba 或 bytedance 时，若在智体中选择了这些平台的语音音色，则会通过 TalkX 作为服务商实现语音生成。反之，则会使用已配置的对应平台。

使用 TalkX 作为TTS服务商类别时，必须配置：`forest.variables.talkxCenterBaseUrl`

#### memory

记忆管理相关配置

| 属性名 | 默认值 | 说明 |
| --- | --- | --- |
| xiaozhi.memory.type | none | 长期记忆服务类别。可选值：`none`、`mem0`

类别选择`mem0`时，必须配置：`forest.variables.mem0baseUrl`

> - `none` 指空的实现，不会管理记忆。
> - 对接 `mem0` 的实现是开源项目部署的API。因此当你部署好`mem0`之后，需要使用 [main.py](src%2Fmain%2Fjava%2Forg%2Fbigmouth%2Fgpt%2Fxiaozhi%2Fmemory%2Fmem0%2Fmain.py) 来启动。

