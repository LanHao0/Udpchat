package club.lanhaoo.chat;

import club.lanhaoo.chat.Classes.Message;
import com.google.gson.Gson;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:10:26 PM_6/17/2019
 * @Magic_Power_Of_Code!
 */

public class Server {
    public static void main(String[] args) throws Exception {
        //可能抛出异常 加上抛出异常

        //服务端其实就是所有消息的接收端！接收到消息打印到聊天室房间

        System.out.println("开始服务端");
        InetAddress localHost = InetAddress.getLocalHost();

        //自动生成广播地址
        String[] temp_arr;

        temp_arr = localHost.getHostAddress().split("\\.");
        temp_arr[3] = "255";
        String broadcast_ip = temp_arr[0] + "." + temp_arr[1] + "." + temp_arr[2] + "." + temp_arr[3];

        System.out.println("广播地址: " + broadcast_ip);

        byte[] bytes = new byte[1024];

        DatagramSocket datagramSocket = new DatagramSocket(2112);
        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
        System.out.println("在 " + localHost + " : 2112" + "上运行服务端 ");
        boolean ServerOn = true;

        ArrayList Iplist = new ArrayList();
        ArrayList BanIp = new ArrayList();

        String serverPassword="mima111";

        while (ServerOn) {

            datagramSocket.receive(datagramPacket);
            //收到消息

            String pure_message = new String(datagramPacket.getData(), 0, datagramPacket.getLength(), StandardCharsets.UTF_8);
            System.out.println(pure_message);
            String fromIP = datagramPacket.getAddress().getHostAddress();

            Gson gson = new Gson();
            Message message = gson.fromJson(pure_message, Message.class);

            String message_content=message.getContent();
            String command = message.getCommand();

            Iplist.add(fromIP);
            //todo 超级命令登陆ip

            if (BanIp.contains(fromIP)) {
                //如果来自被封禁IP，则不进行操作
                String bannedtips = "你已被管理员封禁，无法发送群消息&[系统消息]";
                long mtime = new Date().getTime();
                Message message_back = new Message("text", "", bannedtips);
                if (!message_back.send(fromIP)) {
                    System.out.println("发送失败\n");
                }
                System.out.println("来自封禁Ip:" + fromIP + "内容:" + pure_message);
                continue;

            } else {

                //config
                String endCommand = "ENDSERVER";
                String banipCommand = "BANIP";
                String unbanipCommand = "UNBAN";
                String ServerOfftips = localHost + "服务器下线&[系统消息]";

                String UserCommand_Nonamesend = "NoNameSend";
                //config


                // 广播前检测
                if (command.contains("SYSTEM_COMMAND")) {
                    if (message_content.contains(serverPassword)){
                        String mcontent=null;

                        if (command.contains(banipCommand)){
                            BanIp.add(message_content.split("#")[2]);
                            System.out.println("Banned ip:" + message_content.split("#")[2]);
                            mcontent="已封禁IP: "+fromIP;

                        }

                        if (command.contains(unbanipCommand)){
                            BanIp.remove(message_content.split("#")[2]);
                            System.out.println("Unbanned ip:" + message_content.split("#")[2]);
                            mcontent="解封IP: "+fromIP;

                        }

                        if (command.contains(endCommand)){
                            System.out.println(ServerOfftips);

                            mcontent=ServerOfftips;

                            // todo stop Broadcasting message instead of break the loop
                            break;
                        }

                        long mtime = new Date().getTime();
                        Message message_go=new Message(
                                "system",
                                "",
                                mcontent);
                        message_go.serverSend(broadcast_ip);
                        continue;
                    }

                }

                if (command.contains("UserCommand")) {
                    if (command.contains(UserCommand_Nonamesend)) {
                        message.setSender("[匿名消息]");
                        message.serverSend(broadcast_ip);
                    }

                    continue;
                }


                //广播消息
                System.out.println("广播来自 " + message.getFromIp() + " 的消息 " + message.getContent());
                message.serverSend(broadcast_ip);
            }


        }

        datagramSocket.close();

    }

    //这段代码来自https://stackoverflow.com/questions/17252018/getting-my-lan-ip-address-192-168-xxxx-ipv4，强转了两句的变量类型，适用于这里
    public static String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ipAddress = inetAddress.getHostAddress().toString();
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
