package club.lanhaoo.chat;

import club.lanhaoo.chat.Classes.GlobalThings;
import club.lanhaoo.chat.Classes.Message;
import club.lanhaoo.chat.Classes.UserSettings;
import com.google.gson.Gson;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;


/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:11:57 PM_6/17/2019
 * @Magic_Power_Of_Code!
 */


public class clientReceiveThread implements Runnable {


    private String serverIP = "127.0.0.1";
    //改成你的服务器IP

    @Override
    public void run(){
        UserSettings userSettings= new UserSettings();

        userSettings.setServerIp(serverIP);
        try{

            System.out.println("开始接受服务器数据");


            DatagramSocket datagramSocket =
                    new DatagramSocket(12251);


            byte[] bytes_from_server =
                    new byte[1024];


            DatagramPacket datagramPacket =
                    new DatagramPacket(
                            bytes_from_server,
                            bytes_from_server.length
                    );



            while(true){


                datagramSocket.receive(datagramPacket);



                String message_pure =
                        new String(
                                datagramPacket.getData(),
                                0,
                                datagramPacket.getLength(),
                                StandardCharsets.UTF_8
                        );



                Gson gson = new Gson();


                Message message =
                        gson.fromJson(
                                message_pure,
                                Message.class
                        );

                if (message == null) {
                    //丢弃无法解析的报文
                    continue;
                }



                /*
                 *
                 * ACK处理
                 *
                 */

                if("ACK".equals(message.getType())){


                    System.out.println(
                            "收到ACK:"
                                    +message.getContent()
                    );


                    GlobalThings.confirmIds.add(
                            message.getContent()
                    );


                    continue;

                }



                /*
                 *
                 * 回复服务器ACK
                 *
                 */


                Message ack =
                        new Message(
                                "ACK",
                                "",
                                message.getMessageId()
                        );


                ack.send(userSettings);



                /*
                 *
                 * 消息去重
                 *
                 */


                String messageId =
                        message.getMessageId();



                if(messageId!=null){


                    if(GlobalThings.receivedIds
                            .contains(messageId)){


                        System.out.println(
                                "重复消息:"
                                        +messageId
                        );


                        continue;

                    }


                    GlobalThings.receivedIds.add(messageId);

                }



                /*
                 *
                 * 显示聊天
                 *
                 */


                if(message.getSender()!=null){

                    System.out.println(
                            "来自 "
                                    +message.getSender()
                    );

                }else{

                    System.out.println(
                            "来自 "
                                    +message.getFromIp()
                    );

                }


                System.out.println(
                        message.getContent()
                                +'\n'
                );


            }


        }catch(Exception e){

            e.printStackTrace();

        }


    }

}