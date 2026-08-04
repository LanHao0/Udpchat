# Udpchat

![version_0.6](https://img.shields.io/badge/version-v0.6-blue.svg)
![language java](https://img.shields.io/badge/language-java-yellow.svg)
![platform windows,linux,android](https://img.shields.io/badge/platform-windows|linux|android-lightgrey.svg)
![MIT license](https://img.shields.io/badge/license-MIT-000000.svg)
[![codebeat badge](https://codebeat.co/badges/697f6359-4e8b-453f-b5a4-b02da0749bc9)](https://codebeat.co/projects/github-com-lanhao0-udpchat-master)
[![Build Status](https://travis-ci.org/LanHao0/Udpchat.svg?branch=master)](https://travis-ci.org/LanHao0/Udpchat)


This is a Java project.
Chat in local network by using udp

## How to Use?
(Need install java first) 
Download lasest zip from **release**
### For Windows user:
double click "server.cmd" and "client.cmd",
or use GUI ClientChat.jar
enter server ip in client.cmd, then enter any message you want.
### For Linux user:
use following command to start server and client.

    bash ./client.sh
    bash ./server.sh

or use GUI ClientChat.jar
enter server ip in client.sh, then enter any message you want.
### For Android user:
Download apk from release of this project:
[Udpchat_android](https://github.com/LanHao0/Udpchat_android/releases)
**(app can't receive private chat for now)**

Here're some commands you may need.


|Description|Command  |
|--|--|
|Set Nickname|UserCommand.setMyName#yourname  |
|User Private Talk|UserCommand.secretTalk#other_user_ip#message |
|Send anonymous group message|UserCommand.NoNameSend|
|open/close local network file sharing |UserCommand.fileShare|
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
也可以使用ClientChat.jar, GUI界面
在client.cmd中输入服务器Ip地址, 然后发送你想要发送的消息
### Linux 用户:
使用命令

    bash ./client.sh
    bash ./server.sh


也可以使用ClientChat.jar, GUI界面
在client.sh中输入服务器Ip地址, 然后发送你想要发送的消息
### 安卓 手机用户:
从项目的 release 中下载apk安装包:
[Udpchat_android](https://github.com/LanHao0/Udpchat_android/releases)
**(安卓应用目前无法接收私聊消息)**

下面是你可能需要的一些命令：


| 描述 | 命令 |
|--|--|
| 设置昵称 | UserCommand.setMyName#yourname  |
|私聊|UserCommand.secretTalk#other_user_ip#message |
|发送匿名群消息|UserCommand.NoNameSend|
|退出群聊|UserCommand.Droplink|
|开启/关闭局域网共享|UserCommand.fileShare|
|管理封禁&解封IP|SYSTEM_COMMAND.BANIP#mima111#user_ip<br>SYSTEM_COMMAND.UNBAN#mima111#user_ip|
|管理关闭服务器|SYSTEM_COMMAND.ENDSERVER#mima111|


###
消息类型：
confirm， 每条消息发送时，会伴随confirmCode，如果客户端收到，会原样返回，这样就知道这条消息已经收到了
