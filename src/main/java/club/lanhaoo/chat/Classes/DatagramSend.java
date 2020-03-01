/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:2:42 PM_3/1/2020
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat.Classes;

import club.lanhaoo.chat.Classes.UI.CellRender_Message;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatagramSend {
    private int port;
    private Message raw_message;
    private String IP;

    public DatagramSend(int port, Message raw_message, String IP) {
        this.port = port;
        this.raw_message = raw_message;
        this.IP = IP;
    }

    public void send() throws IOException {
        DatagramSocket datagramSocket=new DatagramSocket(2113);

        Gson gson =new Gson();
        byte[] bytes=gson.toJson(raw_message).getBytes(StandardCharsets.UTF_8);

        if (bytes.length>1024){
            //todo 解决包大于1024问题 -> 新建一个类, 分包type:part10-1 10-2 10-3这样
        }

        DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length, InetAddress.getByName(IP),port);

        datagramSocket.send(datagramPacket);
        datagramSocket.close();

        //todo 开启一个新的线程? 检察一旦发送成功 -> 设置cellrender背景色换颜色
        boolean success=false;
        if (port!=12251){
            try {
                Thread.sleep(2000);
                if (GlobalThings.confirmMD5.indexOf(raw_message.getMD5())==-1){
//                    System.out.println("没有md5"+raw_message.getMD5());
                    //这里就是没接收到服务器确认信息了
//                    System.out.println(GlobalThings.confirmMD5);
                    System.out.println("服务器未收到消息,重新发送中....");
                    this.send();
                }else{
                    //todo 这里是发送成功, 设置颜色?
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }


}
