/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:2:42 PM_3/1/2020
 * @Magic_Power_Of_Code!
 */


package club.lanhaoo.chat.Classes;


import com.google.gson.Gson;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;



public class DatagramSend {


    private int port;

    private Message raw_message;

    private String IP;



    //统一UDP Socket
    private static DatagramSocket socket;



    static {

        try {

            socket = new DatagramSocket();


        } catch (Exception e) {

            e.printStackTrace();

        }

    }




    public DatagramSend(
            int port,
            Message raw_message,
            String IP
    ){

        this.port = port;

        this.raw_message = raw_message;

        this.IP = IP;

    }





    /**
     *
     * 可靠发送
     *
     * 普通消息:
     *      发送
     *      等ACK
     *      重试
     *
     *
     * ACK消息:
     *      直接发送
     *
     */


    public void send() throws Exception {



        sendRaw();



        //ACK消息不用等待ACK
        if(raw_message.getType().equals("ACK")){

            return;

        }



        int maxRetry = 3;



        for(int i=0;i<maxRetry;i++){



            //第一次发送已经完成
            if(i>0){


                System.out.println(
                        "重新发送:"
                                +raw_message.getMessageId()
                );


                sendRaw();

            }



            //等待ACK

            Thread.sleep(1000);



            if(GlobalThings.confirmIds
                    .contains(
                            raw_message.getMessageId()
                    )){


                System.out.println(
                        "发送成功:"
                                +raw_message.getMessageId()
                );


                return;

            }


        }



        System.out.println(
                "发送失败:"
                        +raw_message.getMessageId()
        );


    }






    /**
     *
     * 无ACK发送
     *
     * 用于:
     *      ACK
     *      Server广播
     *
     */


    public void sendRaw() throws Exception {



        Gson gson =
                new Gson();



        byte[] bytes =
                gson.toJson(raw_message)
                        .getBytes(StandardCharsets.UTF_8);




        if(bytes.length>1024){


            System.out.println(
                    "消息超过UDP限制，需要分包"
            );


            return;

        }





        DatagramPacket packet =
                new DatagramPacket(
                        bytes,
                        bytes.length,
                        InetAddress.getByName(IP),
                        port
                );



        socket.send(packet);



    }



}