/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:9:09 PM_10/20/2019
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat.Classes;

import club.lanhaoo.chat.Classes.ThemeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class UserSettings {
    private String UserName;
    private boolean HidemyIp = false;
    private boolean onFileSharing = false;
    private String ServerIp;
    private String language = "zh"; // "zh" 或 "en"
    private String theme = ThemeUtil.THEME_LIGHT; // "light" 或 "dark"

    private static final String SETTINGS_FILE = "udpchat_user.properties";

    public void setServerIp(String serverIp) {
        ServerIp = serverIp;
    }

    public String getServerIp() {
        return ServerIp;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public boolean getHidemyIp() {
        return HidemyIp;
    }

    public void setHidemyIp(boolean hidemyIp) {
        HidemyIp = hidemyIp;
    }

    public boolean isOnFileSharing() {
        return onFileSharing;
    }

    public void setOnFileSharing(boolean onFileSharing) {
        this.onFileSharing = onFileSharing;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = (language == null) ? "zh" : language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = (theme == null) ? ThemeUtil.THEME_LIGHT : theme;
    }

    /** 从工作目录的配置文件载入用户设置（昵称、隐藏IP、语言、服务器地址） */
    public void load() {
        try {
            File f = new File(SETTINGS_FILE);
            if (!f.exists()) return;
            Properties p = new Properties();
            try (InputStream is = new FileInputStream(f)) {
                p.load(is);
            }
            String u = p.getProperty("username", "");
            UserName = (u == null || u.isEmpty()) ? null : u;
            HidemyIp = Boolean.parseBoolean(p.getProperty("hidemyip", "false"));
            language = p.getProperty("language", "zh");
            theme = p.getProperty("theme", ThemeUtil.THEME_LIGHT);
            ServerIp = p.getProperty("serverip", "");
        } catch (Exception ignore) {
        }
    }

    /** 保存用户设置到工作目录的配置文件 */
    public void save() {
        try {
            Properties p = new Properties();
            p.setProperty("username", UserName == null ? "" : UserName);
            p.setProperty("hidemyip", String.valueOf(HidemyIp));
            p.setProperty("language", language == null ? "zh" : language);
            p.setProperty("theme", theme == null ? ThemeUtil.THEME_LIGHT : theme);
            p.setProperty("serverip", ServerIp == null ? "" : ServerIp);
            try (OutputStream os = new FileOutputStream(SETTINGS_FILE)) {
                p.store(os, "udpchat user settings");
            }
        } catch (Exception ignore) {
        }
    }
}
