/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:8:32 PM_1/19/2020
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat;

import club.lanhaoo.chat.Classes.GlobalThings;
import club.lanhaoo.chat.Classes.Message;
import club.lanhaoo.chat.Classes.UserSettings;
import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ClientChatReceiveThread implements Runnable {
    private JScrollBar jScrollBar;
    private ArrayList<String> arrayList;
    private ListModel listModel;
    private ListModel listModel_message;
    private UserSettings userSettings;


    public ClientChatReceiveThread(JScrollBar jScrollBar,
                                   ListModel listModel,
                                   ListModel listModel_message,UserSettings userSettings) {
        this.jScrollBar = jScrollBar;
        this.arrayList = new ArrayList<String>();
        this.listModel = listModel;
        this.listModel_message = listModel_message;
        this.userSettings = userSettings;

    }

    @Override
    public void run() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket(12251);
            byte[] bytes_from_server = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(bytes_from_server, bytes_from_server.length);

            while (true) {
                datagramSocket.receive(datagramPacket);
                String message_pure = new String(datagramPacket.getData(), 0, datagramPacket.getLength(), StandardCharsets.UTF_8);

                Gson gson = new Gson();
                Message message = gson.fromJson(message_pure, Message.class);
                System.out.println(message_pure);
                if (message == null) {
                    //丢弃无法解析的报文
                    continue;
                }
                //ACK确认
                if("ACK".equals(message.getType())){
                    System.out.println(
                            "收到ACK:"
                                    + message.getContent()
                    );
                    GlobalThings.confirmIds.add(
                            message.getContent()
                    );
                    continue;

                }

                //JOIN 等控制消息不显示（避免空白处/在线列表出现 null）
                if("JOIN".equals(message.getType())){
                    continue;
                }

                String messageId = message.getMessageId();

                Message ack =
                        new Message(
                                "ACK",
                                "",
                                message.getMessageId()
                        );

                ack.send(userSettings);
                if(messageId!=null){


                    if(GlobalThings.receivedIds.contains(messageId)){


                        System.out.println(
                                "重复消息:"
                                        + messageId
                        );


                        continue;

                    }


                    GlobalThings.receivedIds.add(messageId);

                }

                SwingUtilities.invokeLater(() -> {

                    if (message.getFromIp() != null
                            && !arrayList.contains(message.getFromIp())) {
                        arrayList.add(message.getFromIp());
                        ((DefaultListModel) listModel).addElement(message.getFromIp());
                    }

                    //只显示有内容的聊天消息（过滤控制/空白报文）
                    if (message.getContent() != null
                            && !message.getContent().isEmpty()) {
                        ((DefaultListModel) listModel_message).addElement(message);
                    }

                });

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
