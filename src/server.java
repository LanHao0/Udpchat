import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
        byte[] bytes=new byte[1024];
        DatagramSocket datagramSocket=new DatagramSocket(2112);
        DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length);
        System.out.println("在 "+localHost+ " : 2112"+"上运行服务端 ");
        boolean ServerOn=true;
        while (ServerOn){
            datagramSocket.receive(datagramPacket);

            String received_message_merge=new String(datagramPacket.getData(),0,datagramPacket.getLength())+" 来自 "+datagramPacket.getAddress().getHostAddress();
            String pure_message=new String(datagramPacket.getData(),0,datagramPacket.getLength());

            //config
            String SuperendendCommand="SYSTEM_COMMAND.END SERVER";
            String ServerOfftips=localHost+"服务器下线";

            System.out.println(received_message_merge);
            //System.out.println(pure_message);

            //广播消息
            System.out.println("广播来自 "+datagramPacket.getAddress().getHostAddress()+" 的消息 "+ pure_message);
            datagramSocket.send(new DatagramPacket(bytes,bytes.length,InetAddress.getByName("127.0.0.255"),12251));
            if(pure_message.equals(SuperendendCommand)){
                //todo pure_message 判断来源用户

                System.out.println(ServerOfftips);

                byte[] temp_byte=ServerOfftips.getBytes();
                datagramSocket.send(new DatagramPacket(temp_byte,temp_byte.length,InetAddress.getByName("127.0.0.255"),12251));
                break;
            }

        }
        datagramSocket.close();









    }
}
