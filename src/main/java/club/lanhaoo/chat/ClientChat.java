package club.lanhaoo.chat; /**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:8:49 PM_9/29/2019
 * @Magic_Power_Of_Code!
 */



import club.lanhaoo.chat.HttpFileShare.App;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

public class ClientChat {
    private JTextArea textArea1_chat;
    private JPanel panel1;
    private JTextField textField1_message;
    private JButton sendButton;
    private JList list1;
    private JScrollPane scrollPane1;
    private JToolBar Jtoolbar;
    private JButton nicknameButton;
    private JButton hideIPButton;
    private JTextArea textArea_message;
    private JScrollPane jscrollpane_message;
    private JButton serverIPButton;
    private JButton fileShareButton;

    public static void main(String[] args) throws Exception{


        final UserSettings userSettings=new UserSettings();

        JFrame frame = new JFrame("ClientChat");

        ClientChat clientChat=new ClientChat();

        JPanel jPanel=clientChat.panel1;
        final JTextArea jTextArea_chat=clientChat.textArea1_chat;
        final JTextArea jTextArea_message=clientChat.textArea_message;

        final JList jList_iplist=clientChat.list1;
        JButton jButton_send=clientChat.sendButton;

        JScrollPane jScrollPane=clientChat.scrollPane1;

        JToolBar jToolBar=clientChat.Jtoolbar;
        jToolBar.setFloatable(false);
//        阻止移动

        frame.setContentPane(jPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       //设置居中
        Point point = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        frame.setBounds(point.x - 600 / 2, point.y - 400 / 2, 600, 400);

        frame.pack();
        frame.setVisible(true);
        jTextArea_message.grabFocus();
        //获取焦点

        JButton jButton_severIP=clientChat.serverIPButton;
        jButton_severIP.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
                userSettings.setServerIp(JOptionPane.showInputDialog("重设服务器IP:"));
            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });



        //回车监听
        JButton jButton_HideIp=clientChat.hideIPButton;
        jButton_HideIp.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
                userSettings.setHidemyIp(!userSettings.getHidemyIp());
                if (userSettings.getHidemyIp()){
                    jTextArea_chat.append("已设置隐藏ip, 若已设置昵称 需要重新设置昵称\n");
                    userSettings.setUserName(null);
                }else{
                    jTextArea_chat.append("已显示ip\n");
                }
            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });

        JButton jButton_Nickname=clientChat.nicknameButton;
        jButton_Nickname.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
                userSettings.setUserName(JOptionPane.showInputDialog("输入自定义昵称"));
                jTextArea_chat.append("已设置昵称"+userSettings.getUserName()+"\n");
            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });
        final JButton jButton_fileShare=clientChat.fileShareButton;

        jButton_fileShare.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                try {

                    App app= new App();

                    app.setWebpassword(JOptionPane.showInputDialog("设置密码?"));

                    app.start(NanoHTTPD.SOCKET_READ_TIMEOUT,false);

                    jTextArea_chat.append("开始分享文件,ip地址: "+ InetAddress.getLocalHost().getHostAddress()+":8089");
                    jButton_fileShare.setText("停止分享文件");


                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });


        jTextArea_chat.append("开始客户端，将使用端口2113\n");
        final String serverIP=JOptionPane.showInputDialog("服务器地址");

        userSettings.setServerIp(serverIP);
        jTextArea_chat.append("将使用 " +serverIP +" 作为服务器地址\n");


        //        发送按钮监听
        jButton_send.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
                String pure_message= jTextArea_message.getText();
//
//                if (setname){
//                    raw_Data="[with_name]"+username+"@"+raw_Data;
//
//                }

                long mtime= new Date().getTime();
                Message message=new Message("text","",pure_message,String.valueOf(mtime));

                if (message.send(userSettings)){
                    jTextArea_message.setText(null);
                }else {
                    jTextArea_chat.append("发送失败\n");
                }


            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });


        jTextArea_message.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {

            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()== KeyEvent.VK_ENTER){

                    String pure_message= jTextArea_message.getText();

                    long mtime= new Date().getTime();
                    Message message=new Message("text","",pure_message,String.valueOf(mtime));
                    message.setFromIp(Server.getIpAddress());

                    if (message.send(userSettings)){
                        jTextArea_message.setText(null);
                    }else {
                        jTextArea_chat.append("发送失败\n");
                    }


                }
            }

            public void keyReleased(KeyEvent e) {

            }
        });



//        接收服务器数据
        try{
            jTextArea_chat.append("开始接收服务器数据\n=============\n");
            DatagramSocket datagramSocket=new DatagramSocket(12251);
            byte[] bytes_from_server=new byte[1024];
            DatagramPacket datagramPacket=new DatagramPacket(bytes_from_server,bytes_from_server.length);


            ArrayList arr_ip=new ArrayList();
            final ListModel listModel_ip=new DefaultListModel();
            jList_iplist.setModel(listModel_ip);
            //设置list模型
            jList_iplist.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if (e.getClickCount()==2){
                        int list_index=jList_iplist.locationToIndex(e.getPoint());
                        System.out.println("点击的是"+ list_index);
                        SingleTalk singleTalk=new SingleTalk();
                        System.out.println(((DefaultListModel) listModel_ip).get(list_index));
                        singleTalk.openWindow(((DefaultListModel) listModel_ip).get(list_index).toString(),userSettings);
                    }
                }
            });

            JScrollBar jScrollBar_chat=jScrollPane.getVerticalScrollBar();

            while (true){
                datagramSocket.receive(datagramPacket);
                //todo 接受到的消息都是来自服务端的。。。。

                String message_pure=new String(datagramPacket.getData(),0,datagramPacket.getLength(),"UTF-8");

                Gson gson=new Gson();
                Message message=gson.fromJson(message_pure,Message.class);

                String command=message.getCommand();
                if (!arr_ip.contains(message.getFromIp())) {
                    arr_ip.add(message.getFromIp());
                    ((DefaultListModel) listModel_ip).addElement(message.getFromIp());
                }


                if(command.contains("UserCommand")){

                        jTextArea_chat.append("来自 "+message.getSender()+"\n");
                        jTextArea_chat.append(message.getContent()+"\n");
                        continue;

                }


                jTextArea_chat.append("来自 "+message.getFromIp()+"\n");
                jTextArea_chat.append(message.getContent()+"\n\n");

                jScrollBar_chat.validate();
                jScrollBar_chat.setValue(jScrollBar_chat.getMaximum());

            }

        }catch (Exception e){
            e.printStackTrace();
        }

//接收服务器数据



    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

}

