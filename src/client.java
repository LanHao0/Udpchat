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

        String end_command="UserCommand.Droplink";//config 结束聊天指令及提示
        String end_tips="下线";

        DatagramSocket datagramSocketfromsever=new DatagramSocket(1111);

        new Thread(new clientReceiveThread(),"client Receive message from server").start();

        boolean inCommunication=true;
        while (inCommunication){


            System.out.println("输入消息");
            Scanner scanner=new Scanner(System.in);
            String raw_Data=scanner.nextLine();

            if (raw_Data.equals(end_command)){
                inCommunication=false;
                byte[] temp_bytes=end_tips.getBytes();
                DatagramPacket temp_dataPacket=new DatagramPacket(temp_bytes,temp_bytes.length,InetAddress.getByName(server_ip),2112);
                System.out.println("您已下线");
                break;
            }

            byte[] bytes=raw_Data.getBytes();
            DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length,InetAddress.getByName(server_ip),2112);

            datagramSocket.send(datagramPacket);

        }
        datagramSocket.close();
    }
}
