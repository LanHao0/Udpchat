/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:9:15 PM_8/18/2019
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

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

    public void setType(String type) {
        this.type = type;
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

    public void setContent(String content) {
        this.content = content;
    }

    public boolean send(String toIp){
        try {
            DatagramSocket datagramSocket2=new DatagramSocket(2113);
            this.setContent("[私聊消息]"+content);
            Gson gson=new Gson();
            String raw_data=gson.toJson(this);

            byte[] bytes=raw_data.getBytes(StandardCharsets.UTF_8);

            DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length, InetAddress.getByName(toIp),12251);

            datagramSocket2.send(datagramPacket);

            datagramSocket2.close();
            return true;
        }catch (Exception e1){
            System.out.println(e1);
            return false;
        }
    }

    public boolean serverSend(String broadcast_ip){
        try {
            DatagramSocket datagramSocket2=new DatagramSocket(2113);
            Gson gson=new Gson();
            String raw_data=gson.toJson(this);
            System.out.println(raw_data);

            datagramSocket2.send(new DatagramPacket(raw_data.getBytes(StandardCharsets.UTF_8), raw_data.getBytes(StandardCharsets.UTF_8).length, InetAddress.getByName(broadcast_ip), 12251));
            datagramSocket2.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean send(UserSettings userSettings){
        try {
            DatagramSocket datagramSocket2=new DatagramSocket(2113);

            if (userSettings.getUserName()!=null){
                this.sender=userSettings.getUserName();
            }
            if (userSettings.getHidemyIp()){
                this.setSender("[匿名消息]");
            }
            Gson gson=new Gson();
            String raw_Data = gson.toJson(this);
            byte[] bytes=raw_Data.getBytes(StandardCharsets.UTF_8);

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
