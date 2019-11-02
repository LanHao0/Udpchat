/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:9:15 PM_8/18/2019
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat;

import com.google.gson.Gson;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Message {
    private String type;
    private String sender;
    private String command;
    private String content;
    private String timestamp;
    private String fromIp;

    public Message(String mtype,String mcommand, String mcontent, String mtimestamp){
        type=mtype;
        command=mcommand;
        content=mcontent;
        timestamp=mtimestamp;
    }

    public String getType(){
        return type;
    }

    public String getFromIp() {
        return fromIp;
    }

    public void setFromIp(String fromIp) {
        this.fromIp = fromIp;
    }

    public String getCommand() {
        return command;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public boolean send(String toIp){
        try {
            DatagramSocket datagramSocket2=new DatagramSocket(2113);
            String pure_message="[私聊消息]"+content+"&"+InetAddress.getLocalHost().getHostAddress();
            byte[] bytes=pure_message.getBytes();

            DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length, InetAddress.getByName(toIp),12251);

            datagramSocket2.send(datagramPacket);

            datagramSocket2.close();
            return true;
        }catch (Exception e1){
            System.out.println(e1);
            return false;
        }
    }

    public boolean send(UserSettings userSettings){
        try {
            DatagramSocket datagramSocket2=new DatagramSocket(2113);

            if (userSettings.getUserName()!=null){
                this.sender=userSettings.getUserName();
            }
            if (userSettings.getHidemyIp()){
//                pure_message="UserCommand.NoNameSend#"+content;
                this.command="UserCommand.NoNameSend";
            }
            Gson gson=new Gson();
            String raw_Data = gson.toJson(this);
            byte[] bytes=raw_Data.getBytes();

            DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length, InetAddress.getByName(userSettings.getServerIp()),2112);

            datagramSocket2.send(datagramPacket);

            datagramSocket2.close();
            return true;
        }catch (Exception e1){
            System.out.println(e1);
            return false;
        }
    }

}
