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

public class CellRender_Message implements ListCellRenderer {
    private TitledBorder titledBorder;
    private Message message;

    protected ListCellRenderer listCellRenderer=new DefaultListCellRenderer();

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        message=(Message) list.getModel().getElementAt(index);

        if (message.getSender() != null ) {
            titledBorder=new TitledBorder(LineBorder.createBlackLineBorder(),message.getSender());
        }else {
            titledBorder=new TitledBorder(LineBorder.createBlackLineBorder(),message.getFromIp());
        }
        JLabel jLabel= (JLabel) listCellRenderer.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);

        jLabel.setBorder(titledBorder);
        jLabel.setText(message.getContent());

        if (isSelected){
            StringSelection stringSelection = new StringSelection(message.getContent());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection,stringSelection);
        }

        return jLabel;
    }
}

//http://www.java2s.com/Tutorial/Java/0240__Swing/AddyourownListCellRenderer.htm