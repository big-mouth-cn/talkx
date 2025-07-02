# TalkX

TalkX 是一个开源的大模型聊天平台，界面适配移动端，并且衍生出了 [编程插件](https://github.com/big-mouth-cn/talkx-plugin-for-jetbrains)、[小智ESP智能机器人](https://github.com/big-mouth-cn/xiaozhi-esp32-for-talkx)等能力。

> 学习交流QQ群：953272742

**TalkX 在生产环境上稳定运行时间超过两年，当搭建中间件配置后，即可支持集群生产部署。**

## 💡 什么场景下需要部署TalkX？
- 私有化部署自己的“Chat”聊天应用。
- 私有化部署小智ESP32服务端，自定义大模型、记忆及声音复刻等。（🔥仅需创建数据库即可快速体验）  
[【点击这里】](https://github.com/big-mouth-cn/xiaozhi-esp32-for-talkx)*进一步了解小智ESP32固件与绑定相关资料。*
- 研究与学习大模型的应用。
- 在此基础上进行二次开发。

![talkx.png](other%2Ftalkx.png)

## 🎬 视频教程
[Bilibili（一）两分钟在本地部署一个TalkX](https://www.bilibili.com/video/BV1C3QdYeErz/)

[Bilibili（二）小智后台开源项目之TalkX部署](https://www.bilibili.com/video/BV17WQdYxE41/)

## ✅ 快速部署

[点击查看如何快速在本地部署一个TalkX](docs%2Fquick-start.md)

## ⚙️ 配置文件说明

[点击查看配置文件说明](docs%2Fconfig.md)

## ️🎛 功能特性
### 基础功能
- [x] 支持用户注册与登录功能
- [x] 兼容OpenAI协议模型，支持用户配置代理服务器
- [x] 支持多种AI对话类型，涵盖话题管理与多轮会话
- [x] 支持Prompt工程，集成Velocity等模板引擎
- [x] 提供AI广场功能，支持用户创建、编辑和删除AI
- [x] 支持平台按模型收费、用户按蒜粒结算及订单支付（付款模块未开源）
- [x] 支持小智设备全生态功能，包括设备管理、语音对话、声音复刻及自定义大模型
- [x] 支持JetBrains、VSCode等IDE插件

### 集成平台
- [x] 支持阿里云百炼智能体应用
- [x] 支持扣子智能体

## ❓ 常见问题
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


- **...than one time zone. You must configure either...**  
启动报错，错误信息包含 `...than one time zone. You must configure either...`，这表示数据库连接需要设置时区。
将数据库连接 `?` 后面修改成：`?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai`


- **开源版本有新版本需要如何升级？**  
一般情况，只需要下载新版本并解压，将上个版本的配置文件复制到新版本的`conf`目录下，然后运行`start.bat` 或 `start.sh`即可。


- **更多问题**  
可以加QQ群交流：953272742