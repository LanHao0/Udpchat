/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:9:57 PM_10/22/2019
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

public class SingleTalk {
    private JTextArea textArea_chat;
    private JPanel panel1;
    private JTextArea textArea_message;
    private JButton sendButton;
    private JLabel labelIp;

    public void openWindow(String IP, final UserSettings userSettings) {
        JFrame frame = new JFrame("SingleTalk");
        SingleTalk singleTalk=new SingleTalk();
        frame.setContentPane(singleTalk.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        final JTextArea jTextArea_chat=singleTalk.textArea_chat;
        jTextArea_chat.append("现在聊天对象: " +IP);

        final JLabel jLabel_other=singleTalk.labelIp;
        jLabel_other.setText(IP);

        final JTextArea jTextArea_message=singleTalk.textArea_message;

        JButton jButton_send=singleTalk.sendButton;

        jButton_send.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
                String pure_message=jTextArea_message.getText();

                long mtime= new Date().getTime();
                Message message=new Message("text","",pure_message,String.valueOf(mtime));
                if (message.send(jLabel_other.getText())){
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
    }
}
