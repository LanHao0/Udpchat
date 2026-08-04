/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:9:15 PM_8/18/2019
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat.Classes;


import java.util.Date;
import java.util.UUID;


/**
 * 消息协议
 *
 * messageId:
 *      每条消息唯一编号，用于UDP ACK确认
 *
 * type:
 *      CHAT     普通聊天
 *      ACK      确认消息
 *      SYSTEM   系统消息
 *
 */


public class Message {


    //唯一消息ID
    private String messageId;


    //消息类型
    private String type;


    //发送者
    private String sender;


    //命令
    private String command;


    //内容
    private String content;


    //时间戳
    private long timestamp;


    //来源IP
    private String fromIp;


    //图片
    private String imgBase64;



    public Message(
            String mtype,
            String mcommand,
            String mcontent
    ){


        this.messageId =
                UUID.randomUUID().toString();


        this.type = mtype;


        this.command = mcommand;


        this.content = mcontent;


        this.timestamp =
                System.currentTimeMillis();



        if(mtype.equals("local")){

            this.fromIp="127.0.0.1";

            this.sender="本地";

        }

    }





    /*
     *
     * messageId
     *
     */


    public String getMessageId(){

        return messageId;

    }


    public void setMessageId(String messageId){

        this.messageId=messageId;

    }





    /*
     *
     * type
     *
     */


    public String getType(){

        return type;

    }


    public void setType(String type){

        this.type=type;

    }





    /*
     *
     * sender
     *
     */


    public String getSender(){

        return sender;

    }


    public void setSender(String sender){

        this.sender=sender;

    }





    /*
     *
     * command
     *
     */


    public String getCommand(){

        return command;

    }





    /*
     *
     * content
     *
     */


    public String getContent(){

        return content;

    }


    public void setContent(String content){

        this.content=content;

    }





    /*
     *
     * timestamp
     *
     */


    public long getTimestamp(){

        return timestamp;

    }





    /*
     *
     * fromIp
     *
     */


    public String getFromIp(){

        return fromIp;

    }


    public void setFromIp(String fromIp){

        this.fromIp=fromIp;

    }





    /*
     *
     * 图片
     *
     */


    public String getImgBase64(){

        return imgBase64;

    }


    public void setImgBase64(String imgBase64){

        this.imgBase64=imgBase64;

    }






    /**
     *
     * 客户端发送到服务器
     *
     */

    public boolean send(UserSettings userSettings){



        if(userSettings.getUserName()!=null){

            this.sender =
                    userSettings.getUserName();

        }



        if(userSettings.getHidemyIp()){

            this.sender="[匿名消息]";

        }



        try{


            new DatagramSend(
                    2112,
                    this,
                    userSettings.getServerIp()
            ).send();


            return true;


        }catch(Exception e){

            e.printStackTrace();

        }


        return false;

    }






    /**
     *
     * 服务器发送给客户端
     *
     */

    public boolean sendToClient(String ip){


        try{


            new DatagramSend(
                    12251,
                    this,
                    ip
            ).sendRaw();


            return true;


        }catch(Exception e){

            e.printStackTrace();

        }


        return false;


    }







    /**
     *
     * 服务器广播
     *
     */

    public boolean serverSend(String broadcastIp){


        try{


            new DatagramSend(
                    12251,
                    this,
                    broadcastIp
            ).sendRaw();


            return true;


        }catch(Exception e){

            e.printStackTrace();

        }


        return false;


    }




}