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
            while (true){
                datagramSocket.receive(datagramPacket);

                String message_pure=new String(datagramPacket.getData(),0,datagramPacket.getLength());
                System.out.println("来自 "+datagramPacket.getAddress().getHostAddress());
                System.out.println(message_pure);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
