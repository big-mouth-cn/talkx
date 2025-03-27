# TalkX

TalkX 是一个基于大模型实现的聊天平台，界面适配移动端，并且衍生出了 [编程插件](https://github.com/big-mouth-cn/talkx-plugin-for-jetbrains)、[小智ESP智能机器人](https://github.com/big-mouth-cn/xiaozhi-esp32-for-talkx)等能力。

**TalkX 在生产环境上稳定运行时间超过两年，当搭建中间件配置后，即可支持集群生产部署。**

> 学习交流QQ群：953272742

## 什么场景下需要部署TalkX？
- 私有化部署自己的“Chat”聊天应用。
- 私有化部署小智ESP32服务端，自定义大模型、记忆及声音复刻等。（🔥仅需创建数据库即可快速体验）  
[【点击这里】](docs%2FXIAOZHI-GUIDE.md)*进一步了解小智ESP32固件与绑定相关资料。*
- 研究与学习大模型的应用。
- 在此基础上进行二次开发。

![talkx.png](other%2Ftalkx.png)

## 视频教程
[Bilibili（一）两分钟在本地部署一个TalkX](https://www.bilibili.com/video/BV1C3QdYeErz/)

[Bilibili（二）小智后台开源项目之TalkX部署](https://www.bilibili.com/video/BV17WQdYxE41/)

## 快速部署

### 1、安装Java运行环境：1.8+
打开官网：https://www.oracle.com/java/technologies/downloads/#java8

> 下载对应操作系统的JDK，然后按提示安装。

### 2、下载安装包并解压：
> 打开下载页面，下载需要的版本。

可选下载地址：[百度网盘](https://pan.baidu.com/s/1YNqN3KUfNe730LnX6WZNAg?pwd=cvt8)、[发行页面](https://github.com/big-mouth-cn/talkx/releases)

### 3、执行数据库脚本：
数据库要求：MySQL 5.6.x+

> 创建数据库并导入初始化脚本：[talkx.sql](src%2Fmain%2Fresources%2Ftalkx.sql)
### 4、修改配置文件：
> 首先打开目录：conf/

> 然后直接复制一份配置文件 [default.application.yml](src%2Fmain%2Fconf%2Fdefault.application.yml) 然后重命名为 application.yml，按下图所示修改配置。

![iShot_2025-03-18_19.19.18.png](other%2FiShot_2025-03-18_19.19.18.png)
### 5、启动：
> 进入目录：bin/
#### 【Windows】
> 运行：start.bat
#### 【macOS、Linux】
> 运行：start.sh

### 6、登录TalkX：
> 打开 http://127.0.0.1:8086 IP地址需要按实际修改。
> 然后左下角登录，不需要填写邀请码，手机号码的验证码默认是：`123456`


## 配置文件说明

### config

基础配置项

| 属性名 | 默认值 | 说明 |
| --- | --- | --- |
| config.llm-api-host | https://api.aigateway.work| 大模型服务接口地址 |
| config.llm-api-key| sk-... | 大模型服务接口API |
| config.enable-redis-cache | false | 是否开启Redis。|
| config.jedis.host | 127.0.0.1 | Redis服务器连接IP|
| config.jedis.password | | Redis服务器连接密码 | 
| config.jedis.database | 0 | Redis db |

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

开启这个配置后，通过这个账号使用阿里云百炼的大模型、音色、声音复刻功能。

| 属性名 | 默认值 | 说明 |
| --- | --- | --- |
| xiaozhi.alibaba.dashscope-api-key | | 阿里云百炼API Key |
| xiaozhi.alibaba.cosy-voice-default-model | cosyvoice-v2 | 阿里云语音生成默认模型 |
| xiaozhi.alibaba.cosy-voice-default-voice | longxiaoxia_v2 | 阿里云语音生成默认音色 |

#### bytedance

开启这个配置后，通过这个账号使用字节火山引擎的大模型、音色、声音复刻功能。

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




## ❓常见问题
- **Windows下运行 `start.bat` 直接闪退：**  
请查看 `/logs` 目录下的日志文件，根据报错信息去解决。


- **如何添加新的模型？**  
目前只能通过执行SQL脚本的方式添加新的模型。以下是一个示例，请在数据库里执行：
    ```sql
    insert into `ai_model` (`model_name`, `max_token`, `settle_currency`, `icon`) 
    values ('deepseek-r1', '128000', 2, 'https://plugin-web.talkx.cn/images/model/gpt4.png');
    ```

- **小智设备是使用WebSocket协议通信的吗？**  
不是。与小虾官方一致，使用的是 MQTT+UDP 协议。


- **小智设备在AI语音回答时慢是什么原因？**  
默认配置下，为了便于大家快速测试。TalkX官方提供了代理服务（包括ASR、LLM、TTS），对话过程中的语音识别、语音生成默认都是经过了TalkX官方，整个过程多了一层“代理转发”，且未使用chunked处理，所以会比较慢。  
**你可以通过配置自己的ASR、TTS服务商来提高处理速度。**


- **支持IoT控制小智设备吗？**  
支持。通过大模型的意图识别和Function_Call实现的，因此你选择的大模型需要支持才可以。


- **更多问题**  
可以加QQ群交流：953272742