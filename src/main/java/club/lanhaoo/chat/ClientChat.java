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
import club.lanhaoo.chat.Classes.VoiceRecorder;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.SwingWorker;


public class ClientChat {
    private JTextArea textArea1_chat;
    private JTextField textField1_message;

    // 用于语言切换时重建动态工具栏文案
    private static ServerAnnouncement currentServer;
    private static String startedNickname;

    public ClientChat() {
        FlatMacLightLaf.setup();
        initComponents();
    }

    public static void main(String[] args) throws IOException {
        final ListModel listModel_message = new DefaultListModel();


        final UserSettings userSettings = new UserSettings();
        userSettings.load();
        I18n.setLanguage(userSettings.getLanguage());

        // 界面文案刷新回调（在组件创建完成后赋值，详见下方 applyI18nRef[0] = ...）
        final Runnable[] applyI18nRef = {null};
        // 发送消息后，强制把消息列表滚到最底部（无视用户是否曾上滑看历史）
        final boolean[] forceScrollOnNext = {false};

        final JFrame frame = new JFrame(I18n.get("app.title"));

        ClientChat clientChat = new ClientChat();

        JPanel jPanel = clientChat.panel1;

        final JTextArea jTextArea_message = clientChat.textArea_message;
        // 输入框：浅色圆角边框 + 内边距 + 统一字体，整体更整洁
        jTextArea_message.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        jTextArea_message.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xd0d4d9), 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));


        JButton jButton_send = clientChat.sendButton;

        JScrollPane jScrollPane = clientChat.scrollPane1;

        JToolBar jToolBar = clientChat.Jtoolbar;
        jToolBar.setFloatable(false);
//        阻止移动
        // 工具栏增加内边距，按钮之间更舒展
        jToolBar.setMargin(new Insets(6, 12, 6, 12));
        jToolBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        // 昵称 / 隐藏IP 已迁移到“设置”页（见 Settings.openWindow），
        // 工具栏静态项只保留 服务器IP / 文件共享 / 关于，其余按钮在代码中动态添加
        cleanupToolbarSeparators(jToolBar);

        // 设置按钮（打开设置页，含昵称 / 隐藏IP / 语言）
        final JButton settingsButton = new JButton(I18n.get("toolbar.settings"));
        jToolBar.addSeparator();
        jToolBar.add(settingsButton);
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings.openWindow(userSettings, listModel_message,
                        () -> SwingUtilities.invokeLater(() -> {
                            if (applyI18nRef[0] != null) applyI18nRef[0].run();
                        }));
            }
        });

        // 开启服务器按钮：在当前客户端进程内启动一个服务器（后台线程），并广播自身以便被发现
        JButton jButton_startServer = new JButton("Start Server");
        jToolBar.addSeparator();
        jToolBar.add(jButton_startServer);

        // 启动服务器动作：抽成 Runnable，工具栏按钮与扫描框“启动服务器”按钮共用
        final Runnable startServerAction = () -> {
            String nick = JOptionPane.showInputDialog(frame, "服务器昵称(可留空):", Server.defaultNickname());
            final String nickname = (nick == null || nick.trim().isEmpty()) ? Server.defaultNickname() : nick.trim();
            new Thread(() -> {
                try {
                    Server.startServer(nickname);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
            ((DefaultListModel) listModel_message).addElement(new Message("local", "", I18n.get("msg.serverStarted", nickname)));
            startedNickname = nickname;
            jButton_startServer.setText(I18n.get("toolbar.startServer") + ": " + nickname);
        };
        jButton_startServer.addActionListener(e -> startServerAction.run());

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
                discoverServer(frame, picked -> onServerPicked(picked, jButton_severIP, userSettings), startServerAction);
            }
        });

        // 昵称 / 隐藏IP 已迁移到“设置”页（见 Settings.openWindow），此处不再单独处理

        final JButton jButton_fileShare = clientChat.fileShareButton;
        final App app = new App();

        // 文件共享是否在运行（手动或发送媒体时自动开启）
        final boolean[] fileShareRunning = {false};
        // 发送语音/图片前确保文件共享已开启：媒体文件通过 :8089 免密端点提供给对方
        final Runnable ensureFileShare = () -> {
            if (!fileShareRunning[0]) {
                try {
                    app.setWebpassword("");
                    app.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                    fileShareRunning[0] = true;
                    SwingUtilities.invokeLater(() -> ((DefaultListModel) listModel_message).addElement(
                            new Message("local", "", "已自动开启文件共享，用于收发语音/图片")));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };


        jButton_fileShare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userSettings.isOnFileSharing()) {
                    app.stop();
                    userSettings.setOnFileSharing(false);
                    fileShareRunning[0] = false;
                    jButton_fileShare.setText(I18n.get("toolbar.fileShare"));
                    ((DefaultListModel) listModel_message).addElement(new Message("local", "", I18n.get("msg.fileshareStop")));

                } else {
                    app.setWebpassword(JOptionPane.showInputDialog(I18n.get("fileshare.passwordPrompt")));
                    try {
                        app.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                        fileShareRunning[0] = true;

                        ((DefaultListModel) listModel_message).addElement(new Message("local", "", I18n.get("msg.fileshareStart", InetAddress.getLocalHost().getHostAddress() + ":8089")));
                        jButton_fileShare.setText(I18n.get("toolbar.fileShare.stop"));

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


        discoverServer(frame, picked -> onServerPicked(picked, jButton_severIP, userSettings), startServerAction);


        //        发送按钮监听
        jButton_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                forceScrollOnNext[0] = true;
                SendMessage(jTextArea_message, userSettings, listModel_message);
            }
        });

        // 发送媒体（图片/语音）通用逻辑：把文件放进媒体目录，发一条带 http 链接的消息。
        // 不在此本地添加到消息列表，避免与服务器回显重复（与文本消息一致）。
        Consumer<File> sendMediaFile = (File f) -> {
            try {
                ensureFileShare.run();
                String ip = Server.getIpAddress();
                String link = "http://" + ip + ":8089/?chatfile=" + URLEncoder.encode(f.getName(), "UTF-8");
                Message message = new Message("text", "", link);
                message.setMediaType(f.getName().toLowerCase().endsWith(".wav") ? "voice" : "image");
                message.setFromIp(ip);
                new Thread(() -> message.send(userSettings)).start();
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> ((DefaultListModel) listModel_message).addElement(
                        new Message("local", "", "发送失败: " + ex.getMessage())));
            }
        };

        JButton send_pic = clientChat.sendPicturesButton;
        send_pic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new ImageFilter());
                if (jFileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File f = jFileChooser.getSelectedFile();
                    if (f == null) return;
                    try {
                        File dir = App.getMediaDir();
                        String nm = f.getName();
                        int dot = nm.lastIndexOf('.');
                        String ext = (dot > 0) ? nm.substring(dot) : "";
                        File dest = new File(dir, "img_" + UUID.randomUUID().toString() + ext);
                        Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        sendMediaFile.accept(dest);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "发送图片失败: " + ex.getMessage());
                    }
                }
            }
        });

        // ====== 发送语音：按下开始录音，再次按下停止并发送 ======
        final VoiceRecorder[] recorder = {null};
        clientChat.voiceButton.addActionListener(e -> {
            if (recorder[0] == null) {
                try {
                    recorder[0] = new VoiceRecorder();
                    recorder[0].start();
                    clientChat.voiceButton.setText("停止并发送");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "无法开始录音: " + ex.getMessage());
                }
            } else {
                try {
                    File wav = recorder[0].stop();
                    recorder[0] = null;
                    clientChat.voiceButton.setText("语音");
                    sendMediaFile.accept(wav);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    recorder[0] = null;
                    clientChat.voiceButton.setText("语音");
                }
            }
        });

        // ====== 发送文件：选任意文件拷贝进媒体目录，发一条带 http 链接的“file”媒体消息 ======
        final JButton sendFileButton = clientChat.sendFileButton;
        Consumer<File> sendFileMedia = (File f) -> {
            try {
                ensureFileShare.run();
                String ip = Server.getIpAddress();
                String link = "http://" + ip + ":8089/?chatfile=" + URLEncoder.encode(f.getName(), "UTF-8");
                Message message = new Message("text", "", link);
                message.setMediaType("file");
                message.setFromIp(ip);
                new Thread(() -> message.send(userSettings)).start();
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> ((DefaultListModel) listModel_message).addElement(
                        new Message("local", "", "发送失败: " + ex.getMessage())));
            }
        };
        sendFileButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (f == null) return;
                try {
                    File dir = App.getMediaDir();
                    // 保留原始文件名，便于接收方看到真实文件名（前缀避免重名覆盖）
                    String nm = f.getName();
                    File dest = new File(dir, "file_" + UUID.randomUUID().toString() + "_" + nm);
                    Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    sendFileMedia.accept(dest);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "发送文件失败: " + ex.getMessage());
                }
            }
        });

        jTextArea_message.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isAltDown()) {
                    forceScrollOnNext[0] = true;
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

        ((DefaultListModel) listModel_message).addElement(new Message("local", "", I18n.get("msg.startReceive")));
        CellRender_Message listCellRenderer = new CellRender_Message();
        jList_Message.setCellRenderer(listCellRenderer);
        // 图片下载生成缩略图后，请求列表重绘以把图片内联显示出来（并重新计算行高）
        CellRender_Message.setRepaintCallback(() -> {
            SwingUtilities.invokeLater(() -> {
                // 仅 revalidate/repaint 不足以让 JList 刷新缓存的“可变单元格高度”，
                // 必须触发一次 model 事件才会重新测量对应行（新消息到达时之所以能正常展开，正是因为它触发了 model 事件）。
                // DefaultListModel.set(index, element) 内部会 fireContentsChanged，从而强制 JList 重新测量该图片行的高。
                DefaultListModel model = (DefaultListModel) jList_Message.getModel();
                for (int i = model.getSize() - 1; i >= 0; i--) {
                    Object o = model.getElementAt(i);
                    if (o instanceof Message && "image".equals(((Message) o).getMediaType())) {
                        model.set(i, (Message) o);
                    }
                }
            });
        });
        // 语音播放进度刷新：仅做轻量重绘（高度不变，无需重排行高）
        CellRender_Message.setProgressRepaint(() -> jList_Message.repaint());
        // 聊天区使用柔和背景，配合气泡更清爽
        jList_Message.setBackground(new Color(0xf5f6f8));
        jList_Message.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        // ====== 消息列表：右键复制消息 / 放大预览 ======
        final JPopupMenu msgPopup = new JPopupMenu();
        JMenuItem copyMsgItem = new JMenuItem("复制消息");
        copyMsgItem.addActionListener(e -> {
            int idx = jList_Message.getSelectedIndex();
            if (idx >= 0) {
                Object o = jList_Message.getModel().getElementAt(idx);
                if (o instanceof Message) {
                    String c = ((Message) o).getContent();
                    if (c != null && !c.isEmpty()) {
                        StringSelection ss = new StringSelection(c);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
                    }
                }
            }
        });
        msgPopup.add(copyMsgItem);
        // 仅当选中图片消息时显示“放大预览”
        JMenuItem previewMsgItem = new JMenuItem("放大预览");
        previewMsgItem.addActionListener(e -> {
            int idx = jList_Message.getSelectedIndex();
            if (idx >= 0) {
                Object o = jList_Message.getModel().getElementAt(idx);
                if (o instanceof Message && "image".equals(((Message) o).getMediaType())) {
                    openImagePreview(((Message) o).getContent());
                }
            }
        });
        msgPopup.add(previewMsgItem);

        // ====== 消息列表：左键点击链接打开 ======
        jList_Message.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showMsgPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showMsgPopup(e);
            }
            private void showMsgPopup(MouseEvent e) {
                int idx = jList_Message.locationToIndex(e.getPoint());
                if (idx >= 0) {
                    jList_Message.setSelectedIndex(idx);
                    Object o = jList_Message.getModel().getElementAt(idx);
                    previewMsgItem.setVisible(o instanceof Message
                            && "image".equals(((Message) o).getMediaType()));
                    msgPopup.show(jList_Message, e.getX(), e.getY());
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1 || e.isPopupTrigger()) return;
                int idx = jList_Message.locationToIndex(e.getPoint());
                if (idx < 0) return;
                Rectangle cell = jList_Message.getCellBounds(idx, idx);
                if (cell == null || !cell.contains(e.getPoint())) return;
                Message m = (Message) jList_Message.getModel().getElementAt(idx);
                String url = m.getContent();
                if ("voice".equals(m.getMediaType())) {
                    CellRender_Message.togglePlay(url);
                } else if ("image".equals(m.getMediaType())) {
                    openImagePreview(url);
                } else if ("file".equals(m.getMediaType())) {
                    downloadAndOpen(url);
                } else {
                    String link = findUrlAt(m, jList_Message, e.getPoint(), cell);
                    if (link != null) openInBrowser(link);
                }
            }
        });

        // ====== 输入框：右键菜单（剪切/复制/粘贴/全选）======
        final JPopupMenu textPopup = new JPopupMenu();
        JMenuItem cutItem = new JMenuItem("剪切");
        cutItem.addActionListener(e -> jTextArea_message.cut());
        JMenuItem copyItem = new JMenuItem("复制");
        copyItem.addActionListener(e -> jTextArea_message.copy());
        JMenuItem pasteItem = new JMenuItem("粘贴");
        pasteItem.addActionListener(e -> jTextArea_message.paste());
        JMenuItem selectAllItem = new JMenuItem("全选");
        selectAllItem.addActionListener(e -> jTextArea_message.selectAll());
        textPopup.add(cutItem);
        textPopup.add(copyItem);
        textPopup.add(pasteItem);
        textPopup.addSeparator();
        textPopup.add(selectAllItem);
        jTextArea_message.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) textPopup.show(jTextArea_message, e.getX(), e.getY());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) textPopup.show(jTextArea_message, e.getX(), e.getY());
            }
        });

        final JList jList_iplist = clientChat.list1;
        final ListModel listModel_ip = new DefaultListModel();
        jList_iplist.setModel(listModel_ip);
        jList_iplist.setBackground(new Color(0xf5f6f8));
        jList_iplist.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        jList_iplist.setFixedCellHeight(26);
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


        // ====== 自动滚动 + “N条新消息”按钮 ======
        // userScrolledUp: 用户是否把滚动条拉离了底部（在看历史消息）
        // programmaticScroll: 标记程序触发的滚动，避免被误判为用户操作
        final boolean[] userScrolledUp = {false};
        final int[] newMsgCount = {0};
        final boolean[] programmaticScroll = {false};

        final JButton newMsgBtn = new JButton("0 条新消息");
        newMsgBtn.setVisible(false);
        frame.getLayeredPane().add(newMsgBtn, JLayeredPane.POPUP_LAYER);

        Runnable positionNewMsgBtn = () -> {
            int w = newMsgBtn.getPreferredSize().width;
            int h = newMsgBtn.getPreferredSize().height;
            if (w <= 0 || h <= 0) return;
            newMsgBtn.setBounds(frame.getWidth() - w - 24, frame.getHeight() - h - 24, w, h);
        };
        Runnable scrollToBottom = () -> {
            programmaticScroll[0] = true;
            int last = ((DefaultListModel) listModel_message).getSize() - 1;
            if (last >= 0) jList_Message.ensureIndexIsVisible(last);
            jScrollBar_chat.setValue(jScrollBar_chat.getMaximum());
            programmaticScroll[0] = false;
            userScrolledUp[0] = false;
            newMsgCount[0] = 0;
            newMsgBtn.setVisible(false);
        };
        Runnable showNewMsgBtn = () -> {
            newMsgCount[0]++;
            newMsgBtn.setText(I18n.get("msg.newMessages", newMsgCount[0]));
            positionNewMsgBtn.run();
            newMsgBtn.setVisible(true);
        };

        newMsgBtn.addActionListener(e -> scrollToBottom.run());

        jScrollBar_chat.addAdjustmentListener(e -> {
            if (programmaticScroll[0]) return; // 忽略程序触发的滚动
            int max = jScrollBar_chat.getMaximum() - jScrollBar_chat.getVisibleAmount();
            boolean atBottom = jScrollBar_chat.getValue() >= max - 4;
            if (atBottom) {
                userScrolledUp[0] = false;
                newMsgCount[0] = 0;
                newMsgBtn.setVisible(false);
            } else {
                userScrolledUp[0] = true;
            }
        });

        ((DefaultListModel) listModel_message).addListDataListener(new javax.swing.event.ListDataListener() {
            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                // 记录每条“收发”消息（文本 + 图片/语音/文件等多媒体）到 logs/ 按日期的 JSON 文本
                for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                    Object o = listModel_message.getElementAt(i);
                    if (o instanceof Message) {
                        Message mm = (Message) o;
                        boolean isChat = "CHAT".equals(mm.getType()) || mm.getMediaType() != null;
                        if (isChat) {
                            String dir = (mm.getFromIp() != null && mm.getFromIp().equals(Server.getIpAddress()))
                                    ? "sent" : "received";
                            club.lanhaoo.chat.Classes.MessageLogger.log(mm, dir);
                        }
                    }
                }
                onNewMessage();
            }
            public void intervalRemoved(javax.swing.event.ListDataEvent e) { }
            public void contentsChanged(javax.swing.event.ListDataEvent e) { }
            private void onNewMessage() {
                if (userScrolledUp[0] && !forceScrollOnNext[0]) {
                    SwingUtilities.invokeLater(showNewMsgBtn);
                } else {
                    SwingUtilities.invokeLater(scrollToBottom);
                }
                forceScrollOnNext[0] = false;
            }
        });

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) { positionNewMsgBtn.run(); }
        });
        SwingUtilities.invokeLater(positionNewMsgBtn);

        // 初始化界面文案（多语言）；语言切换时由 Settings 回调重新执行
        applyI18nRef[0] = () -> {
            clientChat.sendButton.setText(I18n.get("button.send"));
            clientChat.sendPicturesButton.setText("图片");
            settingsButton.setText(I18n.get("toolbar.settings"));
            jButton_startServer.setText(startedNickname == null ? I18n.get("toolbar.startServer")
                    : I18n.get("toolbar.startServer") + ": " + startedNickname);
            clientChat.serverIPButton.setText(fmtServerButton(currentServer));
            clientChat.fileShareButton.setText(userSettings.isOnFileSharing()
                    ? I18n.get("toolbar.fileShare.stop") : I18n.get("toolbar.fileShare"));
            clientChat.aboutButton.setText(I18n.get("toolbar.about"));
            copyMsgItem.setText(I18n.get("menu.copyMessage"));
            cutItem.setText(I18n.get("text.cut"));
            copyItem.setText(I18n.get("text.copy"));
            pasteItem.setText(I18n.get("text.paste"));
            selectAllItem.setText(I18n.get("text.selectAll"));
        };
        SwingUtilities.invokeLater(applyI18nRef[0]);


    }

    /**
     * 选定服务器后：记录服务器地址、更新按钮，并向服务器发送 JOIN 报文，
     * 让服务器登记本机IP，从而无需先发聊天消息也能收到其他人的消息。
     */
    private static void onServerPicked(ServerAnnouncement picked, JButton button, UserSettings userSettings) {
        if (picked == null) {
            return;
        }
        userSettings.setServerIp(picked.getIp());
        currentServer = picked;
        if (button != null) {
            button.setText(I18n.get("toolbar.serverIP") + ": " + picked.toString());
        }
        // 注册到服务器（即使UDP丢包，客户端稍后发消息也会再次登记）
        Message join = new Message("JOIN", "", "");
        join.sendJoinRaw(userSettings);
    }

    private static String fmtServerButton(ServerAnnouncement p) {
        return (p == null) ? I18n.get("toolbar.serverIP") : I18n.get("toolbar.serverIP") + ": " + p.toString();
    }

    /** 清理工具栏中多余的分隔符（开头/结尾/连续） */
    private static void cleanupToolbarSeparators(JToolBar bar) {
        java.util.List<Integer> toRemove = new java.util.ArrayList<>();
        int n = bar.getComponentCount();
        for (int i = 0; i < n; i++) {
            if (!(bar.getComponent(i) instanceof JSeparator)) continue;
            boolean prevSep = (i - 1 >= 0) && (bar.getComponent(i - 1) instanceof JSeparator);
            boolean nextSep = (i + 1 < n) && (bar.getComponent(i + 1) instanceof JSeparator);
            boolean atEdge = (i == 0) || (i == n - 1);
            if (prevSep || nextSep || atEdge) toRemove.add(i);
        }
        for (int i = toRemove.size() - 1; i >= 0; i--) bar.remove((int) toRemove.get(i));
        bar.revalidate();
    }

    private static void SendMessage(JTextArea jTextArea_message, UserSettings userSettings, ListModel listModel_message) {
        String pure_message = jTextArea_message.getText();
        if (pure_message == null || pure_message.trim().isEmpty()) {
            return;
        }

        Message message = new Message("text", "", pure_message);
        message.setFromIp(Server.getIpAddress());

        // 点击发送后立即清空输入框（在 EDT 上），发送放到后台线程，
        // 避免 message.send 等待 ACK 时阻塞界面
        jTextArea_message.setText("");

        new Thread(() -> {
            boolean ok = message.send(userSettings);
            if (!ok) {
                SwingUtilities.invokeLater(() -> {
                    ((DefaultListModel) listModel_message).addElement(new Message("local", "", "发送失败"));
                });
            }
        }, "SendMessage").start();

    }

    /**
     * 在消息 HTML 渲染结果中，检测鼠标点击点是否落在某个链接上。
     * 用一个与渲染完全相同的临时 JEditorPane 进行坐标→模型偏移映射，
     * 再通过文档的元素属性判断该偏移是否位于 <a> 标签内。
     */
    private static String findUrlAt(Message m, JList<?> list, Point click, Rectangle cell) {
        JEditorPane ep = new JEditorPane();
        ep.setContentType("text/html");
        ep.setEditable(false);
        ep.setText(CellRender_Message.toHtml(m, false));
        // 必须与渲染时相同的宽度，换行才一致，坐标映射才准确
        ep.setSize(cell.width, Math.max(cell.height, 10));
        ep.doLayout();
        try {
            ep.getUI().getRootView(ep); // 触发视图布局
        } catch (Exception ignore) { }

        Point p = new Point(click.x - cell.x, click.y - cell.y);
        int pos = ep.viewToModel(p);
        if (pos < 0) return null;

        javax.swing.text.Document doc = ep.getDocument();
        if (!(doc instanceof HTMLDocument)) return null;
        HTMLDocument htmlDoc = (HTMLDocument) doc;
        Element elem = htmlDoc.getCharacterElement(pos);
        if (elem == null) return null;
        AttributeSet attrs = elem.getAttributes();
        Object link = attrs.getAttribute(HTML.Tag.A);
        if (link instanceof AttributeSet) {
            Object href = ((AttributeSet) link).getAttribute(HTML.Attribute.HREF);
            if (href instanceof String && !((String) href).isEmpty()) {
                return (String) href;
            }
        }
        return null;
    }

    /** 用系统默认浏览器打开链接（自动补全 http:// 前缀） */
    private static void openInBrowser(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "无法打开链接: " + url, "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 图片放大预览：下载原图并缩放至适配屏幕后，在可关闭对话框中展示 */
    private static void openImagePreview(String url) {
        final JDialog dlg = new JDialog((Frame) null, "图片预览", false);
        dlg.setLayout(new BorderLayout());
        final JLabel label = new JLabel("加载中…", SwingConstants.CENTER);
        dlg.add(new JScrollPane(label), BorderLayout.CENTER);
        dlg.setSize(420, 320);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                File f = CellRender_Message.getRawSync(url);
                BufferedImage img = ImageIO.read(f);
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                int maxW = (int) (screen.width * 0.9);
                int maxH = (int) (screen.height * 0.9);
                int w = img.getWidth(), h = img.getHeight();
                double s = Math.min(Math.min((double) maxW / w, (double) maxH / h), 1.0);
                Image scaled = img.getScaledInstance((int) (w * s), (int) (h * s), Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
            @Override
            protected void done() {
                try {
                    ImageIcon ic = get();
                    label.setIcon(ic);
                    label.setText(null);
                    dlg.pack();
                    dlg.setLocationRelativeTo(null);
                } catch (Exception ex) {
                    label.setText("加载失败: " + ex.getMessage());
                }
            }
        }.execute();
        // 点击预览图任意处关闭
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dlg.dispose(); }
        });
    }

    /** 下载文件消息到本地 received 目录，并用系统默认程序打开 */
    private static void downloadAndOpen(String url) {
        final File dir = new File(System.getProperty("user.home"), "udpchat_media/received");
        dir.mkdirs();
        final String name = CellRender_Message.chatFileName(url);
        new SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                File f = new File(dir, name);
                try (InputStream in = new URI(url).toURL().openStream();
                     FileOutputStream out = new FileOutputStream(f)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                }
                return f;
            }
            @Override
            protected void done() {
                try {
                    File f = get();
                    Desktop.getDesktop().open(f);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "打开文件失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * 扫描+选择合并窗口：持续扫描，发现的服务器实时进入列表，
     * 双击或点“连接”即加入；另提供“启动服务器”“手动输入”与“取消”。取消即停止扫描。
     */
    private static void discoverServer(Component parent, Consumer<ServerAnnouncement> onResult, Runnable onStartServer) {
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
        JButton btnStartServer = new JButton(I18n.get("toolbar.startServer"));
        JButton btnManual = new JButton("手动输入...");
        JButton btnCancel = new JButton("取消");
        btns.add(btnConnect);
        btns.add(btnStartServer);
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

        btnStartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 启动本机服务器：停止扫描并关闭窗口，复用主界面的启动逻辑
                scanning[0] = false;
                dlg.dispose();
                if (onStartServer != null) onStartServer.run();
                // 启动完成后自动连接刚启动的本机服务器
                String ip = Server.getIpAddress();
                if (ip != null && onResult != null) {
                    String nick = (startedNickname != null) ? startedNickname : ip;
                    onResult.accept(new ServerAnnouncement(ip, "", nick));
                }
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
            label1.setText(I18n.get("label.chat"));
            panel1.add(label1, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

            //---- label2 ----
            label2.setText(I18n.get("label.online"));
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

                //---- serverIPButton ----
                serverIPButton.setText("serverIP");
                Jtoolbar.add(serverIPButton);
                Jtoolbar.addSeparator();

                //---- fileShareButton ----
                fileShareButton.setText("FileShare");
                Jtoolbar.add(fileShareButton);
                Jtoolbar.addSeparator();

                //---- aboutButton ----
                aboutButton.setText(I18n.get("toolbar.about"));
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

            // 媒体发送行：图片 / 语音 / 文件，置于输入框上方
            {
                sendPicturesButton.setText("图片");
                voiceButton = new JButton("语音");
                sendFileButton = new JButton("文件");
                JPanel mediaPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 2));
                mediaPanel.setOpaque(false);
                mediaPanel.add(sendPicturesButton);
                mediaPanel.add(voiceButton);
                mediaPanel.add(sendFileButton);
                panel1.add(mediaPanel, new GridConstraints(4, 0, 1, 2,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
            }
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
    private JButton serverIPButton;
    private JButton fileShareButton;
    private JButton aboutButton;
    private JScrollPane jscrollpane_message;
    private JTextArea textArea_message;
    private JButton sendPicturesButton;
    private JButton voiceButton;
    private JButton sendFileButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}

