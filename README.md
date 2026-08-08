# Udpchat

![version_1.0](https://img.shields.io/badge/version-v1.0-blue.svg)
![language java 9](https://img.shields.io/badge/language-java%209-yellow.svg)
![platform windows,linux,android](https://img.shields.io/badge/platform-windows|linux|android-lightgrey.svg)
![MIT license](https://img.shields.io/badge/license-MIT-000000.svg)
[![Build Status](https://travis-ci.org/LanHao0/Udpchat.svg?branch=master)](https://travis-ci.org/LanHao0/Udpchat)

This is a Java project.
Chat in local network by using UDP.

- **Java version:** built with **Java 9** (`maven-compiler-plugin` `source`/`target` = `9`).
- **Build tool:** Maven.
- **UI:** Java Swing (FlatLaf look-and-feel).
- **Platforms:** Windows / Linux desktop, and the companion [Android app](https://github.com/LanHao0/Udpchat_android).

## Features

- 💬 **LAN chat over UDP** — server + client in one app, works across Windows / Linux / Android on the same local network.
- 🎙️ **Voice messages** — auto-preloaded; shows a play button, a 44-bar **waveform**, and the **remaining time** while playing (mirrors the desktop style).
- 🖼️ **Image messages** — sent as a thumbnail; high-quality scaling, click to preview full size.
- 📎 **File sending** — share any file; recipient downloads via the built-in HTTP file server.
- 📡 **File sharing** — a password-less local HTTP server (NanoHTTPD, port `8089`) serves files from the `chat_media` directory so peers can fetch images / voice / files over the LAN.
- 📜 **Message history** — every sent / received message (text, image, voice, file) is appended as one JSON line per row into `logs/YYYY-MM-DD.txt` at runtime.
- 🎨 **Modern chat UI** — speech bubbles with rounded corners, your messages right-aligned (blue), others left-aligned (green), with sender name and send time.
- 👥 **Group & private chat** — nickname, anonymous broadcast, private talk, admin ban / unban / shutdown.

## How to Use?
(Need install Java 9+ first)
Download latest zip from **release**

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

## 赞助 / Sponsor

如果这个小工具对你有帮助，欢迎请作者喝杯咖啡 ☕

![赞助](src/main/resources/support/support.png)

软件主界面也提供了「赞助」按钮，点击即可同时查看这两张收款码（左支付宝、右微信）。

## Build from source

    mvn clean package

The build produces a runnable `jar-with-dependencies` whose main class is
`club.lanhaoo.chat.ClientChat`.

Here're some commands you may need.

| Description | Command  |
|--|--|
| Set Nickname | UserCommand.setMyName#yourname  |
| User Private Talk | UserCommand.secretTalk#other_user_ip#message |
| Send anonymous group message | UserCommand.NoNameSend |
| open/close local network file sharing | UserCommand.fileShare |
| Exit from group | UserCommand.Droplink |
| Admin ban&unban ip | SYSTEM_COMMAND.BANIP#mima111#user_ip<br>SYSTEM_COMMAND.UNBAN#mima111#user_ip |
| Admin turn off server | SYSTEM_COMMAND.ENDSERVER#mima111 |

# Udp聊天

这是一个 Java 项目（**Java 9**，`maven-compiler-plugin` 的 `source`/`target` 为 `9`）。
在本地局域网使用 UDP 聊天。

## 功能

- 💬 **局域网 UDP 聊天** —— 服务器与客户端合二为一，Windows / Linux / Android 在同一局域网内互相收发。
- 🎙️ **语音消息** —— 自动预加载；显示播放按钮、**44 根波形条**以及播放时的**剩余秒数**（与电脑端一致的样式）。
- 🖼️ **图片消息** —— 以缩略图发送，高质量缩放，点击可预览原图。
- 📎 **文件发送** —— 可发送任意文件，对方通过内置 HTTP 文件服务下载。
- 📡 **文件共享** —— 内置本地 HTTP 服务（NanoHTTPD，端口 `8089`）从 `chat_media` 目录提供文件，供局域网内同伴获取图片 / 语音 / 文件。
- 📜 **消息历史** —— 运行时把每条收发消息（文字 / 图片 / 语音 / 文件）以 JSON 形式按行追加写入 `logs/YYYY-MM-DD.txt`。
- 🎨 **现代化聊天界面** —— 圆角气泡，自己发的靠右（蓝色）、别人发的靠左（绿色），并显示发送者与发送时间。
- 👥 **群聊与私聊** —— 昵称、匿名群发、私聊、管理员封禁 / 解封 / 关服。

## 如何使用

（需要先安装 Java 9 及以上）
从 **release** 中下载最新的压缩包

### Windows 用户:
双击 "server.cmd" 和 "client.cmd",
也可以使用 ClientChat.jar, GUI 界面
在 client.cmd 中输入服务器 IP 地址, 然后发送你想要发送的消息

### Linux 用户:
使用命令

    bash ./client.sh
    bash ./server.sh

也可以使用 ClientChat.jar, GUI 界面
在 client.sh 中输入服务器 IP 地址, 然后发送你想要发送的消息

### 安卓手机用户:
从项目的 release 中下载 apk 安装包:
[Udpchat_android](https://github.com/LanHao0/Udpchat_android/releases)
**(安卓应用目前无法接收私聊消息)**

## 从源码构建

    mvn clean package

构建会生成一个可直接运行的 `jar-with-dependencies`，主类是
`club.lanhaoo.chat.ClientChat`。

无界面（命令行）模式复用同一套最新收发逻辑（服务器 JOIN、文本 / 图片 / 语音 /
文件发送、文件共享、消息去重），适合在无显示环境的服务器或脚本中使用：

    java -cp udpchat-1.0-SNAPSHOT-jar-with-dependencies.jar \
         club.lanhaoo.chat.ClientChatCli [服务器IP] [昵称]

不带参数时交互式输入。运行时可用行首 `/` 命令：

| 命令 | 作用 |
|--|--|
| `/image <路径>` | 发送图片 |
| `/voice <路径>` | 发送语音 |
| `/file <路径>`  | 发送文件 |
| `/name <昵称>`  | 修改昵称 |
| `/server <IP>`  | 切换 / 加入服务器 |
| `/fileshare [on\|off]` | 开关文件共享 |
| `/help` | 显示帮助 |
| `/quit` | 退出 |

下面是你可能需要的一些命令：

| 描述 | 命令 |
|--|--|
| 设置昵称 | UserCommand.setMyName#yourname  |
| 私聊 | UserCommand.secretTalk#other_user_ip#message |
| 发送匿名群消息 | UserCommand.NoNameSend |
| 退出群聊 | UserCommand.Droplink |
| 开启/关闭局域网共享 | UserCommand.fileShare |
| 管理封禁&解封IP | SYSTEM_COMMAND.BANIP#mima111#user_ip<br>SYSTEM_COMMAND.UNBAN#mima111#user_ip |
| 管理关闭服务器 | SYSTEM_COMMAND.ENDSERVER#mima111 |

###
消息类型：
confirm，每条消息发送时，会伴随 confirmCode，如果客户端收到，会原样返回，这样就知道这条消息已经收到了
