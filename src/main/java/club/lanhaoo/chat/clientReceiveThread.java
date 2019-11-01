package club.lanhaoo.chat;

import com.google.gson.Gson;

import javax.swing.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Scanner;

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

            String username;

            while (true){
                datagramSocket.receive(datagramPacket);
                //todo 接受到的消息都是来自服务端的。。。。

                String message_pure=new String(datagramPacket.getData(),0,datagramPacket.getLength(),"UTF-8");
                Gson gson=new Gson();
                Message message=gson.fromJson(message_pure,Message.class);

                if(message_pure.startsWith("[with_name]")){
                    if(message_pure.contains("@")){
                        message_pure=message_pure.replace("[with_name]","");
                        username=message_pure.split("@")[0];
                        System.out.println("来自 "+username);
                        System.out.println(message_pure.split("@")[1]);
                        continue;
                    }
                }

                System.out.println("来自 "+message.getFromIp());
                System.out.println(message.getContent());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
