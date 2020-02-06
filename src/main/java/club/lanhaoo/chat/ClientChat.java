package club.lanhaoo.chat;

/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:8:49 PM_9/29/2019
 * @Magic_Power_Of_Code!
 */


import club.lanhaoo.chat.Classes.Message;
import club.lanhaoo.chat.Classes.UserSettings;
import club.lanhaoo.chat.HttpFileShare.App;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import fi.iki.elonen.NanoHTTPD;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
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
    private JButton aboutButton;
    private JList messageJList;

    public static void main(String[] args) throws IOException {


        final UserSettings userSettings = new UserSettings();

        JFrame frame = new JFrame("ClientChat");

        ClientChat clientChat = new ClientChat();

        JPanel jPanel = clientChat.panel1;

        final JTextArea jTextArea_message = clientChat.textArea_message;


        JButton jButton_send = clientChat.sendButton;

        JScrollPane jScrollPane = clientChat.scrollPane1;

        JToolBar jToolBar = clientChat.Jtoolbar;
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

        final ListModel listModel_message=new DefaultListModel();



        JButton jButton_severIP = clientChat.serverIPButton;
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
        JButton jButton_HideIp = clientChat.hideIPButton;
        jButton_HideIp.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
                userSettings.setHidemyIp(!userSettings.getHidemyIp());
                if (userSettings.getHidemyIp()) {
                    ((DefaultListModel)listModel_message).addElement("已设置隐藏ip, 若已设置昵称 需要重新设置昵称\n");
                    userSettings.setUserName(null);
                } else {
                    ((DefaultListModel)listModel_message).addElement("已显示ip\n");
                }
            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });

        JButton jButton_Nickname = clientChat.nicknameButton;
        jButton_Nickname.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) { }

            public void mousePressed(MouseEvent e) {
                String username = JOptionPane.showInputDialog("输入自定义昵称");
                if (username != null && !username.equals("")) {
                    userSettings.setUserName(username);
                    ((DefaultListModel)listModel_message).addElement("已设置昵称" + userSettings.getUserName() + "\n");
                } else {
                    userSettings.setUserName(null);
                }

            }

            public void mouseReleased(MouseEvent e) { }
            public void mouseEntered(MouseEvent e) { }
            public void mouseExited(MouseEvent e) { }
        });
        final JButton jButton_fileShare = clientChat.fileShareButton;
        final App app = new App();

        jButton_fileShare.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) { }

            @Override
            public void mousePressed(MouseEvent e) {

                if (userSettings.isOnFileSharing()) {
                    app.stop();
                    userSettings.setOnFileSharing(false);
                    jButton_fileShare.setText("FileShare");
                    ((DefaultListModel)listModel_message).addElement("已停止分享文件\n");

                } else {
                    app.setWebpassword(JOptionPane.showInputDialog("设置密码?"));
                    try {
                        app.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                        ((DefaultListModel)listModel_message).addElement("开始分享文件,ip地址: " + InetAddress.getLocalHost().getHostAddress() + ":8089 \n");
                        jButton_fileShare.setText("停止分享文件");

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    userSettings.setOnFileSharing(true);
                }


            }

            @Override
            public void mouseReleased(MouseEvent e) { }
            @Override
            public void mouseEntered(MouseEvent e) { }
            @Override
            public void mouseExited(MouseEvent e) { }
        });

        JButton jButton_about = clientChat.aboutButton;
        jButton_about.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) { }

            @Override
            public void mousePressed(MouseEvent e) {
                About about = new About();
                about.openWindow();
            }

            @Override
            public void mouseReleased(MouseEvent e) { }
            @Override
            public void mouseEntered(MouseEvent e) { }
            @Override
            public void mouseExited(MouseEvent e) { }
        });


        final String serverIP = JOptionPane.showInputDialog("服务器地址");
        userSettings.setServerIp(serverIP);


        //        发送按钮监听
        jButton_send.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) { }

            public void mousePressed(MouseEvent e) {
                SendMessage(jTextArea_message,userSettings,listModel_message);
            }

            public void mouseReleased(MouseEvent e) { }
            public void mouseEntered(MouseEvent e) { }
            public void mouseExited(MouseEvent e) { }
        });


        jTextArea_message.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) { }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    SendMessage(jTextArea_message,userSettings,listModel_message);
                }
            }

            public void keyReleased(KeyEvent e) { }
        });


//        接收服务器数据


        JList jList_Message =clientChat.messageJList;

        jList_Message.setModel(listModel_message);
        ((DefaultListModel)listModel_message).addElement("开始接收服务器数据<br>=============<br>");


        final JList jList_iplist = clientChat.list1;
        final ListModel listModel_ip = new DefaultListModel();
        jList_iplist.setModel(listModel_ip);
        //设置list模型

        jList_iplist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    int list_index = jList_iplist.locationToIndex(e.getPoint());
                    System.out.println("点击的是" + list_index);
                    SingleTalk singleTalk = new SingleTalk();
                    System.out.println(((DefaultListModel) listModel_ip).get(list_index));
                    singleTalk.openWindow(((DefaultListModel) listModel_ip).get(list_index).toString(), userSettings);
                }
            }
        });




        JScrollBar jScrollBar_chat = jScrollPane.getVerticalScrollBar();
        ClientChatReceiveThread cCRT = new ClientChatReceiveThread(
                jScrollBar_chat, listModel_ip,listModel_message);
        cCRT.run();

//接收服务器数据


    }

    private static void SendMessage(JTextArea jTextArea_message, UserSettings userSettings, ListModel listModel_message){
        String pure_message = jTextArea_message.getText();

        long mtime = new Date().getTime();
        Message message = new Message("text", "", pure_message, String.valueOf(mtime));
        message.setFromIp(Server.getIpAddress());

        if (message.send(userSettings)) {
            jTextArea_message.setText(null);
        } else {
            ((DefaultListModel)listModel_message).addElement("发送失败\n");
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 2, new Insets(5, 5, 5, 5), -1, -1));
        panel1.setPreferredSize(new Dimension(600, 400));
        panel1.setRequestFocusEnabled(false);
        sendButton = new JButton();
        sendButton.setText("Send");
        panel1.add(sendButton, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("聊天框");
        panel1.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("在线列表");
        panel1.add(label2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(31);
        panel1.add(scrollPane1, new GridConstraints(2, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea1_chat = new JTextArea();
        textArea1_chat.setEditable(false);
        textArea1_chat.setLineWrap(true);
        textArea1_chat.setMargin(new Insets(0, 0, 0, 0));
        textArea1_chat.setWrapStyleWord(true);
        scrollPane1.setViewportView(textArea1_chat);
        list1 = new JList();
        panel1.add(list1, new GridConstraints(2, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        Jtoolbar = new JToolBar();
        panel1.add(Jtoolbar, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        nicknameButton = new JButton();
        nicknameButton.setText("Nickname");
        Jtoolbar.add(nicknameButton);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        Jtoolbar.add(toolBar$Separator1);
        hideIPButton = new JButton();
        hideIPButton.setText("HideIP");
        Jtoolbar.add(hideIPButton);
        final JToolBar.Separator toolBar$Separator2 = new JToolBar.Separator();
        Jtoolbar.add(toolBar$Separator2);
        serverIPButton = new JButton();
        serverIPButton.setText("serverIP");
        Jtoolbar.add(serverIPButton);
        final JToolBar.Separator toolBar$Separator3 = new JToolBar.Separator();
        Jtoolbar.add(toolBar$Separator3);
        fileShareButton = new JButton();
        fileShareButton.setText("FileShare");
        Jtoolbar.add(fileShareButton);
        jscrollpane_message = new JScrollPane();
        jscrollpane_message.setHorizontalScrollBarPolicy(31);
        panel1.add(jscrollpane_message, new GridConstraints(4, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(-1, 40), null, 0, false));
        textArea_message = new JTextArea();
        textArea_message.setLineWrap(true);
        textArea_message.setMargin(new Insets(2, 2, 2, 2));
        textArea_message.setWrapStyleWord(true);
        jscrollpane_message.setViewportView(textArea_message);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}

