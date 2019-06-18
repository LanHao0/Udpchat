package club.lanhaoo.chat;

import org.omg.PortableInterceptor.INACTIVE;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:10:26 PM_6/17/2019
 * @Magic_Power_Of_Code!
 */

public class server {
    public static void main(String[] args) throws Exception{
        //可能抛出异常 加上抛出异常

        //服务端其实就是所有消息的接收端！接收到消息打印到聊天室房间
        //todo 服务器发送消息到房间
        System.out.println("开始服务端");
        InetAddress localHost=InetAddress.getLocalHost();

        //自动生成广播地址
        String[] temp_arr;

        temp_arr=localHost.getHostAddress().split("\\.");
        temp_arr[3]="255";
        String broadcast_ip=temp_arr[0]+"."+temp_arr[1]+"."+temp_arr[2]+"."+temp_arr[3];

        System.out.println("广播地址: "+ broadcast_ip);

        byte[] bytes=new byte[1024];
        DatagramSocket datagramSocket=new DatagramSocket(2112);
        DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length);
        System.out.println("在 "+localHost+ " : 2112"+"上运行服务端 ");
        boolean ServerOn=true;
        String username;
        ArrayList Iplist=new ArrayList();
        ArrayList BanIp=new ArrayList();

        while (ServerOn){

            datagramSocket.receive(datagramPacket);
            //收到消息

            String received_message_merge=new String(datagramPacket.getData(),0,datagramPacket.getLength())+" 来自 "+datagramPacket.getAddress().getHostAddress();
            String pure_message=new String(datagramPacket.getData(),0,datagramPacket.getLength());
            String fromIP=datagramPacket.getAddress().getHostAddress();



            Iplist.add(fromIP);
            //todo 超级命令登陆ip

            if(BanIp.contains(fromIP)){
                //如果来自被封禁IP，则不进行操作
                String bannedtips="你已被管理员封禁，无法发送群消息&[系统消息]";
                byte[] bytes1=new byte[1024];
                bytes1=bannedtips.getBytes();
                datagramSocket.send(new DatagramPacket(bytes1,bytes1.length,InetAddress.getByName(fromIP),12251));
                System.out.println("来自封禁Ip:"+fromIP+"内容:"+pure_message);
                continue;
            }else {

                    //config
                    String SuperendendCommand="SYSTEM_COMMAND.ENDSERVER";
                    String SuperbanipCommand="BANIP";
                    String SuperunbanipCommand="UNBAN";
                    String ServerOfftips=localHost+"服务器下线&[系统消息]";

                    System.out.println(received_message_merge);
                    //System.out.println(pure_message);

                    String UserCommand_Nonamesend="NoNameSend";
                    //config


                    // 广播前检测
                    if (pure_message.contains("UserCommand")){
                        if (pure_message.contains(UserCommand_Nonamesend)){
                            String string="[匿名消息]"+pure_message.split("#")[1]+"&匿名用户";
                            datagramSocket.send(new DatagramPacket(string.getBytes(),string.getBytes().length,InetAddress.getByName(broadcast_ip),12251));
                        }
                        continue;
                    }


                    if(pure_message.contains("SYSTEM_COMMAND")){
                        try{
                            if(pure_message.split("#")[1].equals("mima111")){

                                if (pure_message.contains(SuperbanipCommand)){
                                    //SYSTEM_COMMAND.BANIP#mima111#127.0.0.1

                                    BanIp.add(pure_message.split("#")[2]);
                                    System.out.println("Banned ip:"+pure_message.split("#")[2]);

                                    String temp="["+fromIP+"已被管理员封禁]&[系统消息]";
                                    datagramSocket.send(new DatagramPacket(temp.getBytes(),temp.getBytes().length,InetAddress.getByName(broadcast_ip),12251));
                                    continue;

                                }
                                if (pure_message.contains(SuperunbanipCommand)){
                                    //SYSTEM_COMMAND.BANIP#mima111#127.0.0.1

                                    BanIp.remove(pure_message.split("#")[2]);
                                    System.out.println("unban ip:"+pure_message.split("#")[2]);

                                    String temp="["+fromIP+"解除封禁]&[系统消息]";
                                    datagramSocket.send(new DatagramPacket(temp.getBytes(),temp.getBytes().length,InetAddress.getByName(broadcast_ip),12251));
                                    continue;

                                }
                                if(pure_message.contains(SuperendendCommand)){
                                    //todo pure_message 判断来源用户

                                        System.out.println(ServerOfftips);

                                        byte[] temp_byte=ServerOfftips.getBytes();

                                        datagramSocket.send(new DatagramPacket(temp_byte,temp_byte.length,InetAddress.getByName(broadcast_ip),12251));
                                        break;
                                    }

                            }

                        }catch (Exception e){
                            continue;
                        }
                    }
                //广播消息
                System.out.println("广播来自 "+datagramPacket.getAddress().getHostAddress()+" 的消息 "+ pure_message);
                byte[] bytes1=new byte[1024];
                pure_message=pure_message+"&"+fromIP;
                bytes1=pure_message.getBytes();
                datagramSocket.send(new DatagramPacket(bytes1,bytes1.length,InetAddress.getByName(broadcast_ip),12251));


            }

        }
            datagramSocket.close();


    }
}
