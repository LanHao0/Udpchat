/**
 * ClientChat 的无 GUI（命令行）模式。
 *
 * 复用最新桌面端 ClientChat 的核心逻辑：
 *   - 服务器发现/加入（JOIN 报文）
 *   - 文本 / 图片 / 语音 / 文件发送（媒体文件经 :8089 免密端点共享）
 *   - 文件共享（HttpFileShare.App）
 *   - 消息收发与去重（ClientChatReceiveThreadCli）
 *
 * 用法：
 *   java -cp ... club.lanhaoo.chat.ClientChatCli [服务器IP] [昵称]
 * 不带参数时交互式输入。
 *
 * 控制台命令（行首以 / 开头）：
 *   /image <文件路径>   发送图片
 *   /voice <文件路径>   发送语音
 *   /file  <文件路径>   发送文件
 *   /name  <昵称>       修改昵称
 *   /server <IP>        切换服务器（重新 JOIN）
 *   /fileshare [on|off] 开关文件共享
 *   /help               显示帮助
 *   /quit               退出
 *
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 */

package club.lanhaoo.chat;

import club.lanhaoo.chat.Classes.GlobalThings;
import club.lanhaoo.chat.Classes.Message;
import club.lanhaoo.chat.Classes.UserSettings;
import club.lanhaoo.chat.HttpFileShare.App;
import fi.iki.elonen.NanoHTTPD;

import java.io.File;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.UUID;

public class ClientChatCli {

    private final UserSettings userSettings = new UserSettings();
    private App app;
    private boolean fileShareRunning = false;
    private String serverIp = null;

    public static void main(String[] args) {
        new ClientChatCli().run(args);
    }

    private void run(String[] args) {
        userSettings.load();
        I18n.setLanguage(userSettings.getLanguage());

        try {
            app = new App();
        } catch (Exception ex) {
            System.out.println("[系统] 文件共享组件初始化失败（媒体收发将不可用）: " + ex.getMessage());
        }

        Scanner sc = new Scanner(System.in, "UTF-8");

        // 服务器地址
        if (args.length >= 1 && !args[0].isEmpty()) {
            serverIp = args[0];
        } else {
            System.out.print("输入服务器地址（回车可留空，稍后用 /server 连接）: ");
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) serverIp = s;
        }

        // 昵称
        if (args.length >= 2 && !args[1].isEmpty()) {
            userSettings.setUserName(args[1]);
        } else {
            String cur = (userSettings.getUserName() != null) ? userSettings.getUserName() : "";
            System.out.print("输入昵称（回车沿用设置中的: " + (cur.isEmpty() ? "无" : cur) + "）: ");
            String n = sc.nextLine().trim();
            if (!n.isEmpty()) userSettings.setUserName(n);
        }

        System.out.println("==== UDP 局域网聊天（无界面模式） ====");
        System.out.println("本机IP: " + Server.getIpAddress());
        System.out.println("昵称:   " + (userSettings.getUserName() == null ? "(未设置)" : userSettings.getUserName()));

        // 启动接收线程
        new Thread(new ClientChatReceiveThreadCli(userSettings, null), "ClientChatCli-Receive").start();

        if (serverIp != null && !serverIp.isEmpty()) {
            joinServer(serverIp);
        } else {
            System.out.println("尚未连接服务器，输入 /server <IP> 加入，或 /help 查看帮助。");
        }

        printHelp();

        // 主循环：读取标准输入
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("/")) {
                if (!handleCommand(line, sc)) break; // 返回 false 表示退出
                continue;
            }

            sendText(line);
        }

        cleanup();
    }

    // ====== 命令处理 ======
    private boolean handleCommand(String line, Scanner sc) {
        String[] parts = line.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1].trim() : "";

        switch (cmd) {
            case "/quit":
            case "/exit":
                return false;

            case "/help":
                printHelp();
                break;

            case "/server":
                if (arg.isEmpty()) {
                    System.out.print("输入服务器地址: ");
                    arg = sc.nextLine().trim();
                }
                if (!arg.isEmpty()) joinServer(arg);
                break;

            case "/name":
                if (arg.isEmpty()) {
                    System.out.print("输入昵称: ");
                    arg = sc.nextLine().trim();
                }
                if (!arg.isEmpty()) {
                    userSettings.setUserName(arg);
                    System.out.println("已设置昵称: " + arg);
                }
                break;

            case "/image":
                sendMedia(arg, "image");
                break;

            case "/voice":
                sendMedia(arg, "voice");
                break;

            case "/file":
                sendMedia(arg, "file");
                break;

            case "/fileshare":
                if ("off".equalsIgnoreCase(arg)) {
                    stopFileShare();
                } else {
                    ensureFileShare();
                }
                break;

            default:
                System.out.println("未知命令: " + cmd + " （输入 /help 查看帮助）");
        }
        return true;
    }

    private void printHelp() {
        System.out.println("命令:");
        System.out.println("  /image <文件路径>  发送图片");
        System.out.println("  /voice <文件路径>  发送语音");
        System.out.println("  /file  <文件路径>  发送文件");
        System.out.println("  /name  <昵称>      修改昵称");
        System.out.println("  /server <IP>       切换/加入服务器");
        System.out.println("  /fileshare [on|off] 开关文件共享");
        System.out.println("  /help             显示本帮助");
        System.out.println("  /quit             退出");
        System.out.println("其他输入均作为普通聊天消息发送。");
    }

    // ====== 服务器加入 ======
    private void joinServer(String ip) {
        serverIp = ip;
        userSettings.setServerIp(ip);
        Message join = new Message("JOIN", "", "");
        join.sendJoinRaw(userSettings);
        System.out.println("已向服务器发送 JOIN: " + ip);
    }

    // ====== 文本发送（含本地回显 + 去重登记）======
    private void sendText(String text) {
        Message message = new Message("text", "", text);
        message.setFromIp(Server.getIpAddress());
        // 发送前登记 messageId，抑制服务器回显造成的重复显示
        if (message.getMessageId() != null) {
            GlobalThings.receivedIds.add(message.getMessageId());
        }
        boolean ok = message.send(userSettings);
        if (ok) {
            String me = (userSettings.getUserName() != null) ? userSettings.getUserName() : "我";
            System.out.println("[我] " + me + ": " + text);
        } else {
            System.out.println("[系统] 发送失败");
        }
    }

    // ====== 媒体（图片/语音/文件）发送：复用 ClientChat 最新逻辑 ======
    private void sendMedia(String path, String mediaType) {
        if (path.isEmpty()) {
            System.out.println("请提供文件路径，例如: /" + mediaType + " C:\\1.jpg");
            return;
        }
        File src = new File(path);
        if (!src.exists() || !src.isFile()) {
            System.out.println("[系统] 文件不存在: " + path);
            return;
        }
        ensureFileShare();
        try {
            File dir = App.getMediaDir();
            String nm = src.getName();
            String prefix = "image".equals(mediaType) ? "img_"
                    : "voice".equals(mediaType) ? "voice_"
                    : "file_";
            File dest = new File(dir, prefix + UUID.randomUUID().toString() + "_" + nm);
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String ip = Server.getIpAddress();
            String link = "http://" + ip + ":8089/?chatfile=" + URLEncoder.encode(dest.getName(), "UTF-8");
            Message message = new Message("text", "", link);
            message.setMediaType(mediaType);
            message.setFromIp(ip);
            if (message.getMessageId() != null) {
                GlobalThings.receivedIds.add(message.getMessageId());
            }
            new Thread(() -> {
                boolean ok = message.send(userSettings);
                String me = (userSettings.getUserName() != null) ? userSettings.getUserName() : "我";
                System.out.println(ok ? ("[我] " + me + " 已发送" + mediaLabel(mediaType) + ": " + nm)
                        : "[系统] 发送失败");
            }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("[系统] 发送失败: " + ex.getMessage());
        }
    }

    private static String mediaLabel(String mediaType) {
        if ("image".equals(mediaType)) return "图片";
        if ("voice".equals(mediaType)) return "语音";
        return "文件";
    }

    // ====== 文件共享：发送媒体前自动开启，复用 ClientChat 的 ensureFileShare 逻辑 ======
    private void ensureFileShare() {
        if (fileShareRunning) return;
        try {
            app.setWebpassword("");
            app.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            fileShareRunning = true;
            System.out.println("已自动开启文件共享（媒体收发用），端口 8089");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("[系统] 开启文件共享失败: " + ex.getMessage());
        }
    }

    private void stopFileShare() {
        if (!fileShareRunning) return;
        try {
            app.stop();
        } catch (Exception ignore) {
        }
        fileShareRunning = false;
        userSettings.setOnFileSharing(false);
        System.out.println("已关闭文件共享");
    }

    private void cleanup() {
        stopFileShare();
        System.out.println("已退出。");
    }
}
