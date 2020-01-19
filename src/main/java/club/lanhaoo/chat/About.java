/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:8:49 PM_1/1/2020
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat;

import club.lanhaoo.chat.HttpFileShare.MyFile;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class About {
    private JTextArea textArea1;
    private JButton checkUpdateButton;
    private JButton openSourceLicensesCreditButton;
    private JPanel Jpanel;
    private JButton softwareInfoButton;

    public void openWindow() {
        JFrame frame = new JFrame("About");
        final About about=new About();
        frame.setContentPane(about.Jpanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        about.openSourceLicensesCreditButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                String credit=new MyFile().getResString("openSourceLicense.txt");
                about.textArea1.setText(credit);
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

        about.softwareInfoButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                String infos="UDPChat 局域网 \n" +
                        "\n开发者列表:"+
                        "\n兰兰想"+
                        "\n\n官网:\nhttps://www.lanhaoo.club/myApps/#/app/UDPChat%20%E5%B1%80%E5%9F%9F%E7%BD%91";
                about.textArea1.setText(infos);
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
    }
}
