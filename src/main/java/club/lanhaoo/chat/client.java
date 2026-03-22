package club.lanhaoo.chat;

import club.lanhaoo.chat.Classes.Message;
import club.lanhaoo.chat.Classes.UserSettings;
import club.lanhaoo.chat.HttpFileShare.App;

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


        UserSettings userSettings =new UserSettings();
        System.out.println("输入服务器地址");
        Scanner sc=new Scanner(System.in);

        String server_ip=sc.nextLine();
        userSettings.setServerIp(server_ip);

        String end_command="UserCommand.Droplink";//config 结束聊天指令及提示

        App app_fileShare=new App();
        String set_Username="setMyName";
        String secret_Talk="secretTalk";
        String noNameSend="NoNameSend";

        //secretTalk format UserCommand.secretTalk#ip#chatContent;

        new Thread(new clientReceiveThread(),"club.lanhaoo.chat.client Receive message from club.lanhaoo.chat.server").start();

        boolean inCommunication=true;

        while (inCommunication){

            String raw_Data=sc.nextLine();

//            含有系统命令
            if (raw_Data.startsWith("SYSTEM_COMMAND.")){
                String command_sys=null;
                if (raw_Data.contains("ENDSERVER")){
                    command_sys="ENDSERVER";
                }

                if (raw_Data.contains("BANIP")){
                    command_sys="BANIP";
                }
                if (raw_Data.contains("UNBAN")){
                    command_sys="UNBAN";
                }
                if (raw_Data.contains("WHOIS")){
                    command_sys="WHOIS";
                }
                if (command_sys == null) {
                    System.out.println("未知系统命令");
                    continue;
                }

                Message message = new Message("text", "SYSTEM_COMMAND."+command_sys, raw_Data);
                if (!message.send(userSettings)) {
                    System.out.println("发送失败\n");
                }

                continue;
            }

//            含有用户命令
            if (raw_Data.startsWith("UserCommand.")){

                if (raw_Data.contains(secret_Talk)){
                    String[] commandParts = raw_Data.split("#", 3);
                    if (commandParts.length < 3) {
                        System.out.println("secretTalk 格式错误，应为 UserCommand.secretTalk#ip#content");
                    } else {
                        String toip = commandParts[1];
                        String string = commandParts[2];
                        Message message = new Message("text", "", string);

                        if (!message.send(toip)) {
                            System.out.println("发送失败\n");
                        }
                    }
                }

                if (raw_Data.contains(end_command)){
                    inCommunication=false;
                    System.out.println("您已下线");
                    break;
                }

                if (raw_Data.contains(set_Username)){
                    String[] strings=raw_Data.split("#");
                    userSettings.setUserName(strings[1]);
                    System.out.println("已设置姓名："+strings[1]);
                }

                if (raw_Data.contains(noNameSend)){

                    userSettings.setHidemyIp(!userSettings.getHidemyIp());
                    if(userSettings.getHidemyIp()){
                        System.out.println("已设置隐藏IP");
                    }else {
                        System.out.println("已关闭隐藏IP");
                    }
                }

                if (raw_Data.contains("fileShare")){
                    if (!userSettings.isOnFileSharing()){
                        System.out.println("请设置文件分享密码");
                        app_fileShare.setWebpassword(sc.nextLine());
                        System.out.println("开始文件分享,端口8089,密码 "+app_fileShare.getWebpassword());
                        app_fileShare.start();
                        userSettings.setOnFileSharing(true);
                    }else {
                        app_fileShare.stop();
                        userSettings.setOnFileSharing(false);
                        System.out.println("已关闭文件共享");
                    }
                }

                continue;
            }

            Message message = new Message("text", "", raw_Data);
            message.setFromIp(Server.getIpAddress());

            if (!message.send(userSettings)) {
                System.out.println("发送失败\n");
            }



        }

        //结束
    }
}
