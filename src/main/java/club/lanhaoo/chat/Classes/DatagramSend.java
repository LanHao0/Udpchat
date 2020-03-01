/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:2:42 PM_3/1/2020
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat.Classes;

import club.lanhaoo.chat.Classes.UI.CellRender_Message;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class DatagramSend {
    private int port;
    private Message raw_Data;
    private String IP;

    public DatagramSend(int port, Message raw_Data, String IP) {
        this.port = port;
        this.raw_Data = raw_Data;
        this.IP = IP;
    }

    public void send() throws IOException {
        DatagramSocket datagramSocket=new DatagramSocket(2113);

        Gson gson =new Gson();
        byte[] bytes=gson.toJson(raw_Data).getBytes(StandardCharsets.UTF_8);

        if (bytes.length>1024){
            //todo 解决包大于1024问题,方法:新建一个类, 分包type:part10-1 10-2 10-3这样
        }

        DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length, InetAddress.getByName(IP),port);

        datagramSocket.send(datagramPacket);
        datagramSocket.close();

        //todo 开启一个新的线程检察一旦发送成功则设置cellrender背景色换颜色



    }
}
