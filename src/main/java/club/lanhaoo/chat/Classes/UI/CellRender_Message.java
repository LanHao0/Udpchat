/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:7:02 PM_2/6/2020
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat.Classes.UI;

import club.lanhaoo.chat.Classes.Message;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class CellRender_Message extends JTextArea implements ListCellRenderer {
    private TitledBorder titledBorder;
    private Message message;


    @Override

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        message=(Message) list.getModel().getElementAt(index);

        if (message.getSender() != null ) {
            titledBorder=new TitledBorder(LineBorder.createBlackLineBorder(),message.getSender());
        }else {
            titledBorder=new TitledBorder(LineBorder.createBlackLineBorder(),message.getFromIp());
        }

        int width=list.getWidth();
//        https://stackoverflow.com/questions/58270489/how-to-select-row-in-jlist-where-each-cell-contains-jpanel-that-contains-jtextar

        JTextArea jTextArea=new JTextArea();
        jTextArea.setText(message.getContent());
        jTextArea.setBorder(titledBorder);
        jTextArea.setSize(width,Short.MAX_VALUE);

        //todo 限制大小,不然只能横向很怪
        if (isSelected){
            StringSelection stringSelection = new StringSelection(message.getContent());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection,stringSelection);
            jTextArea.setBackground(Color.lightGray);
        }else {
            jTextArea.setBackground(Color.white);
        }

        return jTextArea;
    }
}

//http://www.java2s.com/Tutorial/Java/0240__Swing/AddyourownListCellRenderer.htm