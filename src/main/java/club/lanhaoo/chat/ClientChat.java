package club.lanhaoo.chat;

/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:8:49 PM_9/29/2019
 * @Magic_Power_Of_Code!
 */


import club.lanhaoo.chat.Classes.Message;
import club.lanhaoo.chat.Classes.ServerAnnouncement;
import club.lanhaoo.chat.Classes.UI.CellRender_Message;
import club.lanhaoo.chat.Classes.UI.ImageFilter;
import club.lanhaoo.chat.Classes.UserSettings;
import club.lanhaoo.chat.HttpFileShare.App;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;


public class ClientChat {
    private JTextArea textArea1_chat;
    private JTextField textField1_message;

    public ClientChat() {
        FlatMacLightLaf.setup();
        initComponents();
    }

    public static void main(String[] args) throws IOException {
        final ListModel listModel_message = new DefaultListModel();


        final UserSettings userSettings = new UserSettings();

        final JFrame frame = new JFrame("ClientChat");

        ClientChat clientChat = new ClientChat();

        JPanel jPanel = clientChat.panel1;

        final JTextArea jTextArea_message = clientChat.textArea_message;


        JButton jButton_send = clientChat.sendButton;

        JScrollPane jScrollPane = clientChat.scrollPane1;

        JToolBar jToolBar = clientChat.Jtoolbar;
        jToolBar.setFloatable(false);
//        阻止移动

        //开启服务器按钮：在当前客户端进程内启动一个服务器（后台线程），并广播自身以便被发现
        JButton jButton_startServer = new JButton("Start Server");
        jToolBar.addSeparator();
        jToolBar.add(jButton_startServer);
        jButton_startServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nick = JOptionPane.showInputDialog(frame, "服务器昵称(可留空):", Server.defaultNickname());
                final String nickname = (nick == null || nick.trim().isEmpty()) ? Server.defaultNickname() : nick.trim();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Server.startServer(nickname);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
                ((DefaultListModel) listModel_message).addElement(new Message("local", "", "已启动服务器，昵称: " + nickname));
                jButton_startServer.setText("Server: " + nickname);
            }
        });

        frame.setContentPane(jPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //设置居中
        Point point = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        frame.setBounds(point.x - 600 / 2, point.y - 400 / 2, 600, 400);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.pack();
        frame.setVisible(true);
        jTextArea_message.grabFocus();
        //获取焦点



        JButton jButton_severIP = clientChat.serverIPButton;
        jButton_severIP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                discoverServer(frame, picked -> {
                    if (picked != null) {
                        userSettings.setServerIp(picked.getIp());
                        jButton_severIP.setText("服务器: " + picked.toString());
                    }
                });
            }
        });

        //重新扫描服务器按钮：随时可点，弹出“扫描中”并重新发现局域网服务器
        JButton jButton_scanServer = new JButton("Scan");
        jToolBar.addSeparator();
        jToolBar.add(jButton_scanServer);
        jButton_scanServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                discoverServer(frame, picked -> {
                    if (picked != null) {
                        userSettings.setServerIp(picked.getIp());
                        jButton_severIP.setText("服务器: " + picked.toString());
                    }
                });
            }
        });


        JButton jButton_HideIp = clientChat.hideIPButton;
        jButton_HideIp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userSettings.setHidemyIp(!userSettings.getHidemyIp());
                if (userSettings.getHidemyIp()) {
                    ((DefaultListModel) listModel_message).addElement(new Message("local",
                            "", "已设置隐藏ip, 若已设置昵称 需要重新设置昵称"));
                    userSettings.setUserName(null);
                } else {
                    ((DefaultListModel) listModel_message).addElement(new Message("local",
                            "", "已显示IP"));
                }
            }
        });

        JButton jButton_Nickname = clientChat.nicknameButton;
        jButton_Nickname.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = JOptionPane.showInputDialog("输入自定义昵称");
                if (username != null && !username.equals("")) {
                    userSettings.setUserName(username);
                    ((DefaultListModel) listModel_message).addElement(new Message("local", "", "已设置昵称" + userSettings.getUserName()));
                } else {
                    userSettings.setUserName(null);
                }
            }
        });

        final JButton jButton_fileShare = clientChat.fileShareButton;
        final App app = new App();

        jButton_fileShare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userSettings.isOnFileSharing()) {
                    app.stop();
                    userSettings.setOnFileSharing(false);
                    jButton_fileShare.setText("FileShare");
                    ((DefaultListModel) listModel_message).addElement(new Message("local", "", "已停止分享文件"));

                } else {
                    app.setWebpassword(JOptionPane.showInputDialog("设置密码?"));
                    try {
                        app.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

                        ((DefaultListModel) listModel_message).addElement(new Message("local", "", "开始分享文件,ip地址: " + InetAddress.getLocalHost().getHostAddress() + ":8089"));
                        jButton_fileShare.setText("停止分享文件");

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    userSettings.setOnFileSharing(true);
                }

            }
        });

        JButton jButton_about = clientChat.aboutButton;
        jButton_about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                About about = new About();
                about.openWindow();
            }
        });


        discoverServer(frame, picked -> {
            if (picked != null) {
                userSettings.setServerIp(picked.getIp());
                jButton_severIP.setText("服务器: " + picked.toString());
            }
        });


        //        发送按钮监听
        jButton_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SendMessage(jTextArea_message, userSettings, listModel_message);
            }
        });

        JButton send_pic = clientChat.sendPicturesButton;
        send_pic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                ImageFilter imageFilter = new ImageFilter();
                jFileChooser.setFileFilter(imageFilter);
                if (jFileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File f = jFileChooser.getSelectedFile();

                    // read  and/or display the file somehow. ....
                }

            }
        });

        jTextArea_message.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isAltDown()) {
                    SendMessage(jTextArea_message, userSettings, listModel_message);
                }

            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isAltDown()) {
                    jTextArea_message.setText("");
                }
            }
        });



        JList jList_Message = clientChat.messageJList;

        jList_Message.setModel(listModel_message);

        ((DefaultListModel) listModel_message).addElement(new Message("local", "", "开始接收服务器数据"));
        CellRender_Message listCellRenderer = new CellRender_Message();
        jList_Message.setCellRenderer(listCellRenderer);

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
                jScrollBar_chat, listModel_ip, listModel_message,userSettings);
        new Thread(cCRT).start();


    }

    private static void SendMessage(JTextArea jTextArea_message, UserSettings userSettings, ListModel listModel_message) {
        String pure_message = jTextArea_message.getText();

        Message message = new Message("text", "", pure_message);
        message.setFromIp(Server.getIpAddress());

        SwingUtilities.invokeLater(() -> {

            if (message.send(userSettings)) {
                jTextArea_message.grabFocus();
                jTextArea_message.setText("");
            } else {
                ((DefaultListModel) listModel_message).addElement(new Message("local", "", "发送失败"));
            }

        });



    }

    /**
     * 扫描+选择合并窗口：持续扫描，发现的服务器实时进入列表，
     * 双击或点“连接”即加入；另提供“手动输入”与“取消”。取消即停止扫描。
     */
    private static void discoverServer(Component parent, Consumer<ServerAnnouncement> onResult) {
        final List<ServerAnnouncement> found = new ArrayList<>();
        final Set<String> seen = new HashSet<>();
        final DefaultListModel<String> model = new DefaultListModel<>();
        final JList<String> list = new JList<>(model);

        final JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(parent), "扫描服务器", Dialog.ModalityType.MODELESS);
        dlg.setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("持续扫描中，双击或选“连接”加入："));
        dlg.add(top, BorderLayout.NORTH);
        dlg.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton btnConnect = new JButton("连接");
        JButton btnManual = new JButton("手动输入...");
        JButton btnCancel = new JButton("取消");
        btns.add(btnConnect);
        btns.add(btnManual);
        btns.add(btnCancel);
        dlg.add(btns, BorderLayout.SOUTH);

        final boolean[] scanning = { true };

        final Runnable pickSelected = new Runnable() {
            @Override
            public void run() {
                int idx = list.getSelectedIndex();
                if (idx >= 0 && idx < found.size()) {
                    scanning[0] = false;
                    dlg.dispose();
                    if (onResult != null) onResult.accept(found.get(idx));
                }
            }
        };

        SwingWorker<Void, ServerAnnouncement> worker = new SwingWorker<Void, ServerAnnouncement>() {
            @Override
            protected Void doInBackground() {
                while (scanning[0]) {
                    for (ServerAnnouncement a : scanServers(2000)) {
                        publish(a);
                    }
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void process(List<ServerAnnouncement> chunks) {
                String myIp = Server.getIpAddress();
                for (ServerAnnouncement a : chunks) {
                    if (a == null || a.getIp() == null || "127.0.0.1".equals(a.getIp())) continue;
                    String label = a.toString();
                    if (myIp != null && myIp.equals(a.getIp())) label = label + "  (本机)";
                    if (seen.add(label)) {
                        found.add(a);
                        model.addElement(label);
                    }
                }
            }
        };

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    pickSelected.run();
                }
            }
        });

        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pickSelected.run();
            }
        });

        btnManual.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scanning[0] = false;
                dlg.dispose();
                String manual = JOptionPane.showInputDialog(parent, "请输入服务器地址:");
                if (manual != null && !manual.trim().isEmpty()) {
                    if (onResult != null) onResult.accept(new ServerAnnouncement(manual.trim(), "", manual.trim()));
                }
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scanning[0] = false;
                dlg.dispose();
                if (onResult != null) onResult.accept(null);
            }
        });

        dlg.setSize(340, 320);
        dlg.setLocationRelativeTo(parent);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                scanning[0] = false;
            }
        });
        dlg.setVisible(true);

        worker.execute();
    }

    /**
     * 在发现端口上监听一段时间，收集局域网内广播的服务器通告（按 ip 去重）。
     */
    private static List<ServerAnnouncement> scanServers(int timeoutMs) {
        List<ServerAnnouncement> list = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(ServerAnnouncement.DISCOVERY_PORT);
            socket.setReuseAddress(true);
            socket.setSoTimeout(800);
            byte[] buf = new byte[1024];
            long end = System.currentTimeMillis() + timeoutMs;
            while (System.currentTimeMillis() < end) {
                try {
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    socket.receive(p);
                    String s = new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8);
                    ServerAnnouncement a = ServerAnnouncement.fromJson(s);
                    //忽略本机回环通告（127.0.0.1），避免列表里出现自己
                    if (a != null && a.getIp() != null && !"127.0.0.1".equals(a.getIp()) && seen.add(a.getIp())) {
                        list.add(a);
                    }
                } catch (SocketTimeoutException e) {
                    //单次接收超时，继续直到总超时
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) socket.close();
        }
        return list;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        panel1 = new JPanel();
        sendButton = new JButton();
        JLabel label1 = new JLabel();
        JLabel label2 = new JLabel();
        scrollPane1 = new JScrollPane();
        messageJList = new JList();
        list1 = new JList();
        Jtoolbar = new JToolBar();
        nicknameButton = new JButton();
        hideIPButton = new JButton();
        serverIPButton = new JButton();
        fileShareButton = new JButton();
        aboutButton = new JButton();
        jscrollpane_message = new JScrollPane();
        textArea_message = new JTextArea();
        sendPicturesButton = new JButton();

        //======== panel1 ========
        {
            panel1.setPreferredSize(new Dimension(600, 400));
            panel1.setRequestFocusEnabled(false);
            panel1.setLayout(new GridLayoutManager(7, 2, new Insets(5, 5, 5, 5), -1, -1));

            //---- sendButton ----
            sendButton.setText("Send");
            panel1.add(sendButton, new GridConstraints(6, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

            //---- label1 ----
            label1.setText("\u804a\u5929\u6846");
            panel1.add(label1, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

            //---- label2 ----
            label2.setText("\u5728\u7ebf\u5217\u8868");
            panel1.add(label2, new GridConstraints(1, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

            //======== scrollPane1 ========
            {
                scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                //---- messageJList ----
                messageJList.setFixedCellHeight(-1);
                messageJList.setFixedCellWidth(-1);
                scrollPane1.setViewportView(messageJList);
            }
            panel1.add(scrollPane1, new GridConstraints(2, 0, 2, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));
            panel1.add(list1, new GridConstraints(2, 1, 2, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(150, 50), null));

            //======== Jtoolbar ========
            {

                //---- nicknameButton ----
                nicknameButton.setText("Nickname");
                Jtoolbar.add(nicknameButton);
                Jtoolbar.addSeparator();

                //---- hideIPButton ----
                hideIPButton.setText("HideIP");
                Jtoolbar.add(hideIPButton);
                Jtoolbar.addSeparator();

                //---- serverIPButton ----
                serverIPButton.setText("serverIP");
                Jtoolbar.add(serverIPButton);
                Jtoolbar.addSeparator();

                //---- fileShareButton ----
                fileShareButton.setText("FileShare");
                Jtoolbar.add(fileShareButton);
                Jtoolbar.addSeparator();

                //---- aboutButton ----
                aboutButton.setText("About");
                Jtoolbar.add(aboutButton);
            }
            panel1.add(Jtoolbar, new GridConstraints(0, 0, 1, 2,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

            //======== jscrollpane_message ========
            {
                jscrollpane_message.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                //---- textArea_message ----
                textArea_message.setLineWrap(true);
                textArea_message.setMargin(new Insets(2, 2, 2, 2));
                textArea_message.setWrapStyleWord(true);
                jscrollpane_message.setViewportView(textArea_message);
            }
            panel1.add(jscrollpane_message, new GridConstraints(5, 0, 2, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));

            //---- sendPicturesButton ----
            sendPicturesButton.setText("Send Pictures");
            panel1.add(sendPicturesButton, new GridConstraints(4, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel panel1;
    private JButton sendButton;
    private JScrollPane scrollPane1;
    private JList messageJList;
    private JList list1;
    private JToolBar Jtoolbar;
    private JButton nicknameButton;
    private JButton hideIPButton;
    private JButton serverIPButton;
    private JButton fileShareButton;
    private JButton aboutButton;
    private JScrollPane jscrollpane_message;
    private JTextArea textArea_message;
    private JButton sendPicturesButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}

