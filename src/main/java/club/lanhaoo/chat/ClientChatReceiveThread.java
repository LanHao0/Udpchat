/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:8:32 PM_1/19/2020
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat;

import club.lanhaoo.chat.Classes.Message;
import com.google.gson.Gson;

import javax.swing.*;
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


    public ClientChatReceiveThread(JScrollBar jScrollBar,
                                   ListModel listModel,
                                   ListModel listModel_message) {
        this.jScrollBar = jScrollBar;
        this.arrayList = new ArrayList<String>();
        this.listModel = listModel;
        this.listModel_message = listModel_message;
    }

    @Override
    public void run() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket(12251);
            byte[] bytes_from_server = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(bytes_from_server, bytes_from_server.length);

            while (true) {
                datagramSocket.receive(datagramPacket);
                //todo 接受到的消息都是来自服务端的。。。。

                String message_pure = new String(datagramPacket.getData(), 0, datagramPacket.getLength(), StandardCharsets.UTF_8);

                Gson gson = new Gson();
                Message message = gson.fromJson(message_pure, Message.class);

                if (!arrayList.contains(message.getFromIp())) {
                    arrayList.add(message.getFromIp());
                }
                ((DefaultListModel)listModel_message).addElement(message);
                //自动下滚
                jScrollBar.validate();
                jScrollBar.setValue(jScrollBar.getMaximum());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
