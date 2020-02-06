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

public class CellRender_Message implements ListCellRenderer {
    private TitledBorder titledBorder;
    private Message message;


    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        message=(Message) list.getModel().getElementAt(index);

        DefaultListCellRenderer defaultListCellRenderer =new DefaultListCellRenderer();
        if (message.getSender() != null ) {
            titledBorder=new TitledBorder(LineBorder.createBlackLineBorder(),message.getSender());
        }else {
            titledBorder=new TitledBorder(LineBorder.createBlackLineBorder(),message.getFromIp());
        }

        JLabel jLabel=(JLabel) defaultListCellRenderer.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        jLabel.setBorder(titledBorder);
        jLabel.setText(message.getContent());
        return jLabel;
    }
}
