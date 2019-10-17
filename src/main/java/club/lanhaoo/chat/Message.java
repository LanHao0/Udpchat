/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:9:15 PM_8/18/2019
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat;

public class Message {
    private String type;
    private String sender;
    private String command;
    private String content;
    private String timestamp;

    public Message(String mtype,String msender,String mcommand, String mcontent, String mtimestamp){
        type=mtype;
        sender=msender;
        command=mcommand;
        content=mcontent;
        timestamp=mtimestamp;
    }

    public String getType(){
        return type;
    }

    public String getSender() {
        return sender;
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
}
