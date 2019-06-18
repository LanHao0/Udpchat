package club.lanhaoo.chat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:10:41 PM_6/17/2019
 * @Magic_Power_Of_Code!
 */

public class client {
    public static void main(String[] args) throws Exception{
        System.out.println("开始客户端，将使用端口2113");
        DatagramSocket datagramSocket=new DatagramSocket(2113);

        System.out.println("输入服务器地址");
        Scanner sc=new Scanner(System.in);
        String server_ip=sc.nextLine();

        String username="游客";

        String end_command="UserCommand.Droplink";//config 结束聊天指令及提示
        String end_tips="下线";
        String set_Username="UserCommand.setMyName";
        //conig 用户设置自己的名字,setMyName#test
        boolean setname=false;
        String  secret_Talk="secretTalk";
        //secretTalk format UserCommand.secretTalk#ip#chatContent;

        DatagramSocket datagramSocketfromsever=new DatagramSocket(1111);

        new Thread(new clientReceiveThread(),"club.lanhaoo.chat.client Receive message from club.lanhaoo.chat.server").start();

        boolean inCommunication=true;
        while (inCommunication){


            System.out.println("输入消息");
            Scanner scanner=new Scanner(System.in);
            String raw_Data=scanner.nextLine();

            if (setname){
                raw_Data="[with_name]"+username+"@"+raw_Data;

            }




            if (raw_Data.contains("[with_name]"+username+"@")||raw_Data.startsWith("UserCommand.")){

                if (raw_Data.contains(secret_Talk)){
                    String toip=raw_Data.split("#")[1];
                    String string="[私聊消息]"+raw_Data.split("#")[2]+"&"+InetAddress.getLocalHost().getHostAddress();
                    byte[] temp_bytes=string.getBytes();
                    DatagramPacket temp_dataPacket=new DatagramPacket(temp_bytes,temp_bytes.length,InetAddress.getByName(toip),12251);
                    datagramSocket.send(temp_dataPacket);
                    continue;
                }

                if (raw_Data.contains(end_command)){
                    inCommunication=false;
                    byte[] temp_bytes=end_tips.getBytes();
                    DatagramPacket temp_dataPacket=new DatagramPacket(temp_bytes,temp_bytes.length,InetAddress.getByName(server_ip),2112);
                    System.out.println("您已下线");
                    datagramSocket.send(temp_dataPacket);

                    break;
                }

                if (raw_Data.contains(set_Username)){

                    String[] strings=raw_Data.split("#");
                    username=strings[1];
                    String temp_merge_message="[with_name]"+username+"#"+raw_Data;
                    byte[] temp_bytes=temp_merge_message.getBytes();
                    DatagramPacket temp_dataPacket=new DatagramPacket(temp_bytes,temp_bytes.length,InetAddress.getByName(server_ip),2112);

                    System.out.println("已设置姓名："+username);
                    setname=true;

                    datagramSocket.send(temp_dataPacket);
                    continue;
                }

            }


            byte[] bytes=raw_Data.getBytes();
            DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length,InetAddress.getByName(server_ip),2112);

            datagramSocket.send(datagramPacket);

        }

        datagramSocket.close();
    }
}
