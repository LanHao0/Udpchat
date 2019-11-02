package club.lanhaoo.chat;

import com.google.gson.Gson;
import org.omg.PortableInterceptor.INACTIVE;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:10:26 PM_6/17/2019
 * @Magic_Power_Of_Code!
 */

public class Server {
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

            String received_message_merge=new String(datagramPacket.getData(),0,datagramPacket.getLength(),"UTF-8")+" 来自 "+datagramPacket.getAddress().getHostAddress();
            String pure_message=new String(datagramPacket.getData(),0,datagramPacket.getLength(),"UTF-8");
            String fromIP=datagramPacket.getAddress().getHostAddress();

            Gson gson=new Gson();
            Message message=gson.fromJson(pure_message,Message.class);

            String command=message.getCommand();

            Iplist.add(fromIP);
            //todo 超级命令登陆ip

            if(BanIp.contains(fromIP)){
                //如果来自被封禁IP，则不进行操作
                String bannedtips="你已被管理员封禁，无法发送群消息&[系统消息]";
                byte[] bytes1=new byte[1024];
                bytes1=bannedtips.getBytes("UTF-8");
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
                    if (command.contains("UserCommand")){
                        if (command.contains(UserCommand_Nonamesend)){

                            message.setSender("[匿名消息]");
                            pure_message=gson.toJson(message);
                            datagramSocket.send(new DatagramPacket(pure_message.getBytes("UTF-8"),pure_message.getBytes("UTF-8").length,InetAddress.getByName(broadcast_ip),12251));
                        }
                        continue;
                    }


                    if(command.contains("SYSTEM_COMMAND")){
                        try{
                            if(command.split("#")[1].equals("mima111")){

                                if (command.contains(SuperbanipCommand)){
                                    //SYSTEM_COMMAND.BANIP#mima111#127.0.0.1

                                    BanIp.add(command.split("#")[2]);
                                    System.out.println("Banned ip:"+pure_message.split("#")[2]);

                                    String temp="["+fromIP+"已被管理员封禁]&[系统消息]";
                                    datagramSocket.send(new DatagramPacket(temp.getBytes("UTF-8"),temp.getBytes("UTF-8").length,InetAddress.getByName(broadcast_ip),12251));
                                    continue;

                                }
                                if (command.contains(SuperunbanipCommand)){
                                    //SYSTEM_COMMAND.BANIP#mima111#127.0.0.1

                                    BanIp.remove(command.split("#")[2]);
                                    System.out.println("unban ip:"+pure_message.split("#")[2]);

                                    String temp="["+fromIP+"解除封禁]&[系统消息]";
                                    datagramSocket.send(new DatagramPacket(temp.getBytes("UTF-8"),temp.getBytes("UTF-8").length,InetAddress.getByName(broadcast_ip),12251));
                                    continue;

                                }
                                if(command.contains(SuperendendCommand)){
                                    //todo pure_message 判断来源用户

                                        System.out.println(ServerOfftips);

                                        byte[] temp_byte=ServerOfftips.getBytes("UTF-8");

                                        datagramSocket.send(new DatagramPacket(temp_byte,temp_byte.length,InetAddress.getByName(broadcast_ip),12251));
                                        break;
                                    }

                            }

                        }catch (Exception e){
                            continue;
                        }
                    }
                //广播消息
                System.out.println("广播来自 " + message.getFromIp() + " 的消息 " + message.getContent());
                byte[] bytes1;
                bytes1 = pure_message.getBytes(StandardCharsets.UTF_8);
                datagramSocket.send(new DatagramPacket(bytes1, bytes1.length, InetAddress.getByName(broadcast_ip), 12251));


            }

        }
            datagramSocket.close();


    }
    //这段代码来自https://stackoverflow.com/questions/17252018/getting-my-lan-ip-address-192-168-xxxx-ipv4，强转了两句的变量类型，适用于这里
    public static String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        String ipAddress=inetAddress.getHostAddress().toString();
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.print("get wrong LAN ip");
        }
        return null;
    }
}
