# Udpchat

This is a Java project.
Chat in local network by using udp

## How to Use?
(Need install java first) 
Download lasest zip from **release**
### For Windows user:
double click "server.cmd" and "client.cmd",
enter server ip in client.cmd, then enter any message you want.
### For Linux user:
use following command to start server and client.

    bash ./client.sh
    bash ./server.sh

 
enter server ip in client.sh, then enter any message you want.
### For Android user:
Download apk from release of this project:
[Udpchat_android](https://github.com/LanHao0/Udpchat_android/releases)

Here're some commands you may need.

|Description|Command  |
|--|--|
|Set Nickname|UserCommand.setMyName#yourname  |
|User Private Talk|UserCommand.secretTalk#other_user_ip#message |
|Send anonymous group message|UserCommand.NoNameSend#message|
|Exit from group|UserCommand.Droplink|
|Admin ban&unban ip|SYSTEM_COMMAND.BANIP#mima111#user_ip<br>SYSTEM_COMMAND.UNBAN#mima111#user_ip|
|Admin turn off server|SYSTEM_COMMAND.ENDSERVER#mima111|

# Udp聊天
这是一个Java项目。
在本地局域网聊天，使用UDP

## 如何使用
（需要下载 java）
从 **release**中下载最新的压缩包
### Windows 用户:
双击 "server.cmd" 和 "client.cmd",
在client.cmd中输入服务器Ip地址, 然后发送你想要发送的消息
### Linux 用户:
使用命令

    bash ./client.sh
    bash ./server.sh

 
在client.sh中输入服务器Ip地址, 然后发送你想要发送的消息
### 安卓 手机用户:
从项目的 release 中下载apk安装包:
[Udpchat_android](https://github.com/LanHao0/Udpchat_android/releases)

下面是你可能需要的一些命令：


|Description|Command  |
|--|--|
|设置昵称|UserCommand.setMyName#yourname  |
|私聊|UserCommand.secretTalk#other_user_ip#message |
|发送匿名群消息|UserCommand.NoNameSend#message|
|退出群聊|UserCommand.Droplink|
|管理封禁&解封IP|SYSTEM_COMMAND.BANIP#mima111#user_ip<br>SYSTEM_COMMAND.UNBAN#mima111#user_ip|
|管理关闭服务器|SYSTEM_COMMAND.ENDSERVER#mima111|
