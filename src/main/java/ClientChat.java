/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:8:49 PM_9/29/2019
 * @Magic_Power_Of_Code!
 */



import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

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

    public static void main(String[] args) throws Exception{




        JFrame frame = new JFrame("ClientChat");

        ClientChat clientChat=new ClientChat();

        JPanel jPanel=clientChat.panel1;
        final JTextArea jTextArea_chat=clientChat.textArea1_chat;
        final JTextArea jTextArea_message=clientChat.textArea_message;

        JList jList_iplist=clientChat.list1;
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

        //回车监听


        JButton jButton_Nickname=clientChat.nicknameButton;
        jButton_Nickname.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
//                username=JOptionPane.showInputDialog("输入自定义昵称");
            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });



        jTextArea_chat.append("开始客户端，将使用端口2113\n");
        final String serverIP=JOptionPane.showInputDialog("服务器地址");
        jTextArea_chat.append("将使用 " +serverIP +" 作为服务器地址\n");

        final DatagramSocket datagramSocket2=new DatagramSocket(2113);
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
                try {
                    byte[] bytes=pure_message.getBytes ("UTF-8");
                    DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length,InetAddress.getByName(serverIP),2112);

                    datagramSocket2.send(datagramPacket);
                    jTextArea_message.setText(null);
                }catch (Exception e1){

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
//
//                if (setname){
//                    raw_Data="[with_name]"+username+"@"+raw_Data;
//
//                }
                    try {
                        byte[] bytes=pure_message.getBytes ("UTF-8");
                        DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length,InetAddress.getByName(serverIP),2112);

                        datagramSocket2.send(datagramPacket);
                        jTextArea_message.setText(null);
                    }catch (Exception e1){

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

            String username;
            ArrayList arr_ip=new ArrayList();
            ListModel listModel_ip=new DefaultListModel();
            jList_iplist.setModel(listModel_ip);
            JScrollBar jScrollBar_chat=jScrollPane.getVerticalScrollBar();

            while (true){
                datagramSocket.receive(datagramPacket);
                //todo 接受到的消息都是来自服务端的。。。。

                String message_pure=new String(datagramPacket.getData(),0,datagramPacket.getLength(),"UTF-8");

                String fromip=message_pure.split("&")[1];
                message_pure=message_pure.split("&")[0];

                if (!arr_ip.contains(fromip)) {
                    arr_ip.add(fromip);
                    ((DefaultListModel) listModel_ip).addElement(fromip);
                }




                if(message_pure.startsWith("[with_name]")){
                    if(message_pure.contains("@")){
                        message_pure=message_pure.replace("[with_name]","");
                        username=message_pure.split("@")[0];
                        jTextArea_chat.append("来自 "+username+"\n");
                        jTextArea_chat.append(message_pure.split("@")[1]+"\n");
                        continue;
                    }
                }


                jTextArea_chat.append("来自 "+fromip+"\n");
                jTextArea_chat.append(message_pure+"\n\n");

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

