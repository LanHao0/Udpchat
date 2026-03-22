/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:9:15 PM_8/18/2019
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat.Classes;

import com.google.gson.Gson;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Message {
    private String type;
    private String sender;
    private String command;
    private String content;
    private String timestamp;
    private String fromIp;
    private String imgBase64;


    public String getImgBase64() {
        return imgBase64;
    }

    public void setImgBase64(String imgBase64) {
        this.imgBase64 = imgBase64;
    }

    public Message(String mtype, String mcommand, String mcontent) {
        type = mtype;
        command = mcommand;
        content = mcontent;
        timestamp = getTime();

        if (mtype.equals("local")) {
            fromIp = "127.0.0.1";
            sender = "本地";
        }
    }


    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
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

    public boolean send(String toIp) {
        try {
//            this.setContent("[私聊消息]" + content);
            new DatagramSend(12251, this, toIp).send();
            return true;
        } catch (Exception e1) {
            System.out.println(e1);
        }
        return false;
    }

    public boolean serverSend(String broadcast_ip) {
        try {
            new DatagramSend(12251, this, broadcast_ip).send();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean send(UserSettings userSettings) {
        if (userSettings.getUserName() != null) {
            this.sender = userSettings.getUserName();
        }
        if (userSettings.getHidemyIp()) {
            this.setSender("[匿名消息]");
        }

        try {
            new DatagramSend(2112, this, userSettings.getServerIp()).send();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getTime() {
        long mtime = new Date().getTime();
        return String.valueOf(mtime);
    }


    public String getMD5(){
        Gson gson=new Gson();
        byte[] bytesOfMessage =gson.toJson(this).getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] thedigest = md5.digest(bytesOfMessage);
            BigInteger bigInt = new BigInteger(1,thedigest);
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm is not available", e);
        }
    }
}
