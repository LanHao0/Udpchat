package club.lanhaoo.chat;

import club.lanhaoo.chat.Classes.Message;
import com.google.gson.Gson;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:11:57 PM_6/17/2019
 * @Magic_Power_Of_Code!
 */

public class clientReceiveThread implements Runnable {

    public void run(){
        try{
            System.out.println("开始接受服务器数据");
            DatagramSocket datagramSocket=new DatagramSocket(12251);
            byte[] bytes_from_server=new byte[1024];
            DatagramPacket datagramPacket=new DatagramPacket(bytes_from_server,bytes_from_server.length);


            while (true){
                datagramSocket.receive(datagramPacket);
                //todo 接受到的消息都是来自服务端的。。。。

                String message_pure=new String(datagramPacket.getData(),0,datagramPacket.getLength(), StandardCharsets.UTF_8);
                Gson gson=new Gson();
                Message message=gson.fromJson(message_pure,Message.class);

                if(message.getSender()!=null){
                    System.out.println("来自 "+message.getSender());
                    System.out.println(message.getContent()+'\n');
                    continue;
                }

                System.out.println("来自 "+message.getFromIp());
                System.out.println(message.getContent()+'\n');
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
