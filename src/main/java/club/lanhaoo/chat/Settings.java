package club.lanhaoo.chat;

import club.lanhaoo.chat.Classes.Message;
import club.lanhaoo.chat.Classes.ThemeUtil;
import club.lanhaoo.chat.Classes.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 设置窗口（程序化创建，不依赖 JFormDesigner）。
 * 包含：昵称、隐藏IP、界面语言（中文/English）、主题（浅色/深色），保存后写入 UserSettings 并持久化。
 */
public class Settings {

    public static void openWindow(final UserSettings us, final ListModel listModel,
                                  final Runnable onLocaleChanged, final Runnable onThemeChanged) {
        final JFrame frame = new JFrame(I18n.get("settings.title"));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        // 昵称
        JLabel lbNick = new JLabel(I18n.get("settings.nickname"));
        final JTextField tfNick = new JTextField(20);
        tfNick.setText(us.getUserName() == null ? "" : us.getUserName());

        // 隐藏IP
        JLabel lbHide = new JLabel(I18n.get("settings.hideIp"));
        final JCheckBox cbHide = new JCheckBox();
        cbHide.setSelected(us.getHidemyIp());

        // 语言
        JLabel lbLang = new JLabel(I18n.get("settings.language"));
        final JComboBox<String> cbLang = new JComboBox<>(
                new String[]{I18n.get("settings.language.zh"), I18n.get("settings.language.en")});
        cbLang.setSelectedIndex("en".equalsIgnoreCase(us.getLanguage()) ? 1 : 0);

        // 主题（浅色 / 深色）
        JLabel lbTheme = new JLabel(I18n.get("settings.theme"));
        final JComboBox<String> cbTheme = new JComboBox<>(
                new String[]{I18n.get("settings.theme.light"), I18n.get("settings.theme.dark")});
        cbTheme.setSelectedIndex(ThemeUtil.THEME_DARK.equals(us.getTheme()) ? 1 : 0);

        // 按钮
        JButton btnSave = new JButton(I18n.get("settings.save"));
        JButton btnCancel = new JButton(I18n.get("settings.cancel"));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        c.gridx = 0; c.gridy = 0; panel.add(lbNick, c);
        c.gridx = 1; panel.add(tfNick, c);
        c.gridx = 0; c.gridy = 1; panel.add(lbHide, c);
        c.gridx = 1; panel.add(cbHide, c);
        c.gridx = 0; c.gridy = 2; panel.add(lbLang, c);
        c.gridx = 1; panel.add(cbLang, c);
        c.gridx = 0; c.gridy = 3; panel.add(lbTheme, c);
        c.gridx = 1; panel.add(cbTheme, c);
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; panel.add(btnRow, c);
        c.gridwidth = 1;

        // ===== 事件 =====
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nick = tfNick.getText().trim();
                us.setUserName(nick.isEmpty() ? null : nick);
                boolean hide = cbHide.isSelected();
                us.setHidemyIp(hide);
                String lang = (cbLang.getSelectedIndex() == 1) ? "en" : "zh";
                boolean langChanged = !lang.equalsIgnoreCase(us.getLanguage());
                String theme = (cbTheme.getSelectedIndex() == 1) ? ThemeUtil.THEME_DARK : ThemeUtil.THEME_LIGHT;
                boolean themeChanged = !theme.equalsIgnoreCase(us.getTheme());
                us.setLanguage(lang);
                us.setTheme(theme);
                us.save();

                I18n.setLanguage(lang);

                if (!nick.isEmpty()) {
                    ((javax.swing.DefaultListModel) listModel).addElement(
                            new Message("local", "", I18n.get("msg.nicknameSet", nick)));
                }
                if (hide) {
                    // 隐藏IP 时若已设置昵称则需清除（与原工具栏逻辑一致）
                    if (us.getUserName() != null) us.setUserName(null);
                    ((javax.swing.DefaultListModel) listModel).addElement(
                            new Message("local", "", I18n.get("hideIp.on")));
                } else {
                    ((javax.swing.DefaultListModel) listModel).addElement(
                            new Message("local", "", I18n.get("hideIp.off")));
                }

                // 主题切换：安装新 L&F 并刷新已打开窗口（先切换再通知，保证 isDark() 取到新值）
                if (themeChanged) {
                    ThemeUtil.applyTheme(theme);
                    if (onThemeChanged != null) onThemeChanged.run();
                }

                if (langChanged && onLocaleChanged != null) {
                    onLocaleChanged.run();
                }
                frame.dispose();
            }
        });

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
