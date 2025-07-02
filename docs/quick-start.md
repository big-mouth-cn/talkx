## 快速部署

### 1、安装Java运行环境：1.8+
打开官网：https://www.oracle.com/java/technologies/downloads/#java8

> 下载对应操作系统的JDK，然后按提示安装。

### 2、下载安装包并解压：
> 打开下载页面，下载需要的版本。

可选下载地址：[百度网盘](https://pan.baidu.com/s/1YNqN3KUfNe730LnX6WZNAg?pwd=cvt8)、[发行页面](https://github.com/big-mouth-cn/talkx/releases)

### 3、执行数据库脚本：
数据库要求：MySQL 5.6.x+

> 创建数据库并导入初始化脚本：[talkx.sql](..%2Fsrc%2Fmain%2Fresources%2Ftalkx.sql)
### 4、修改配置文件：
> 首先打开目录：conf/

> 然后直接复制一份配置文件 [default.application.yml](..%2Fsrc%2Fmain%2Fconf%2Fdefault.application.yml) 然后重命名为 application.yml，按下图所示修改配置。

![iShot_2025-03-18_19.19.18.png](..%2Fother%2FiShot_2025-03-18_19.19.18.png)
### 5、启动：
> 进入目录：bin/
#### 【Windows】
> 运行：start.bat
#### 【macOS、Linux】
> 运行：start.sh

### 6、登录TalkX：
> 打开 http://127.0.0.1:8086 IP地址需要按实际修改。
> 然后左下角登录，不需要填写邀请码，手机号码的验证码默认是：`123456`
