/**
 * 无 GUI 模式的接收线程：复用最新桌面端收消息逻辑（ACK 确认、JOIN 过滤、
 * messageId 去重、在线列表维护），但输出到控制台而非 Swing 模型。
 *
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 */

package club.lanhaoo.chat;

import club.lanhaoo.chat.Classes.GlobalThings;
import club.lanhaoo.chat.Classes.Message;
import club.lanhaoo.chat.Classes.UserSettings;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ClientChatReceiveThreadCli implements Runnable {
    private final UserSettings userSettings;
    private final Set<String> onlineIps = new HashSet<>();
    // 收到新消息时回调（控制台打印 / 外部处理），可为 null
    private final java.util.function.Consumer<Message> onMessage;

    public ClientChatReceiveThreadCli(UserSettings userSettings,
                                      java.util.function.Consumer<Message> onMessage) {
        this.userSettings = userSettings;
        this.onMessage = onMessage;
    }

    private static String fmtTime(long ts) {
        return new SimpleDateFormat("HH:mm").format(new Date(ts));
    }

    private static String describe(Message m) {
        String who = (m.getSender() != null && !m.getSender().isEmpty())
                ? m.getSender() : (m.getFromIp() != null ? m.getFromIp() : "?");
        String head = "[" + fmtTime(m.getTimestamp()) + "] " + who;
        String media = m.getMediaType();
        if (media != null) {
            if ("voice".equals(media)) return head + " 发来语音: " + m.getContent();
            if ("image".equals(media)) return head + " 发来图片: " + m.getContent();
            if ("file".equals(media)) return head + " 发来文件: " + m.getContent();
        }
        return head + ": " + m.getContent();
    }

    @Override
    public void run() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket(12251);
            byte[] bytes_from_server = new byte[8192];
            DatagramPacket datagramPacket = new DatagramPacket(bytes_from_server, bytes_from_server.length);

            while (true) {
                datagramSocket.receive(datagramPacket);
                String message_pure = new String(datagramPacket.getData(), 0, datagramPacket.getLength(), StandardCharsets.UTF_8);

                Gson gson = new Gson();
                Message message;
                try {
                    message = gson.fromJson(message_pure, Message.class);
                } catch (Exception e) {
                    message = null;
                }
                if (message == null) {
                    continue; // 丢弃无法解析的报文
                }

                // ACK 确认：仅登记，不显示
                if ("ACK".equals(message.getType())) {
                    if (message.getContent() != null) {
                        GlobalThings.confirmIds.add(message.getContent());
                    }
                    continue;
                }

                // JOIN 等控制消息不显示
                if ("JOIN".equals(message.getType())) {
                    continue;
                }

                // 对每条聊天消息回复 ACK，让服务器停止重传
                Message ack = new Message("ACK", "", message.getMessageId());
                ack.send(userSettings);

                // 去重：自己刚发出的消息回显会被丢弃（发送时已登记 messageId）
                if (message.getMessageId() != null) {
                    if (GlobalThings.receivedIds.contains(message.getMessageId())) {
                        continue; // 重复消息
                    }
                    GlobalThings.receivedIds.add(message.getMessageId());
                }

                // 维护在线列表（仅在控制台提示新上线的对端）
                if (message.getFromIp() != null && !message.getFromIp().isEmpty()) {
                    if (onlineIps.add(message.getFromIp()) && onlineIps.size() > 1) {
                        System.out.println("[系统] 新在线对端: " + message.getFromIp());
                    }
                }

                // 只显示有内容的聊天消息
                if (message.getContent() != null && !message.getContent().trim().isEmpty()) {
                    System.out.println(describe(message));
                    if (onMessage != null) {
                        onMessage.accept(message);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("[系统] 接收线程异常退出: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
