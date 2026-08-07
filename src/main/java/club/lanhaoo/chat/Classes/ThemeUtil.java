package club.lanhaoo.chat.Classes;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.UIManager;
import java.awt.Color;

/**
 * 主题工具：集中管理 浅色 / 深色 两套配色，并提供应用主题、判断当前是否深色的方法。
 * 颜色在读取时根据当前 LookAndFeel 动态返回，因此切换主题后界面会自动取用新配色。
 */
public class ThemeUtil {

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    /** 当前是否为深色主题（依据已安装的 LookAndFeel 判断） */
    public static boolean isDark() {
        return UIManager.getLookAndFeel() instanceof FlatDarkLaf;
    }

    /** 安装指定主题。优先使用 Mac 风格 L&F，失败则回退到通用 FlatLaf。 */
    public static void applyTheme(String theme) {
        try {
            if (THEME_DARK.equals(theme)) {
                FlatMacDarkLaf.setup();
            } else {
                FlatMacLightLaf.setup();
            }
        } catch (Throwable t) {
            // 某些平台缺少字体/资源时回退到基础 FlatLaf
            if (THEME_DARK.equals(theme)) FlatDarkLaf.setup();
            else FlatLightLaf.setup();
        }
    }

    /** 把颜色转成 #rrggbb 形式的十六进制字符串（供 HTML 渲染使用） */
    public static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // ===== 调色板：根据当前主题返回对应颜色 =====
    public static Color chatBg()      { return isDark() ? new Color(0x2b2b2b) : new Color(0xf5f6f8); }
    public static Color ownBg()       { return isDark() ? new Color(0x234862) : Color.decode("#d6ecff"); }
    public static Color ownBorder()   { return isDark() ? new Color(0x3a5f7d) : Color.decode("#a9d4f5"); }
    public static Color otherBg()     { return isDark() ? new Color(0x1e3b2b) : Color.decode("#dcf5e3"); }
    public static Color otherBorder() { return isDark() ? new Color(0x2f5a40) : Color.decode("#a9dcb8"); }
    public static Color ownSender()   { return isDark() ? new Color(0x6cc4ff) : Color.decode("#0b6cb5"); }
    public static Color otherSender() { return isDark() ? new Color(0x5fd38a) : Color.decode("#1b8a4b"); }
    public static Color timeColor()   { return isDark() ? new Color(0x8a9099) : Color.decode("#9aa0a6"); }
    public static Color bubbleText()  { return isDark() ? new Color(0xe6e6e6) : new Color(0x222222); }
    public static Color linkColor()   { return isDark() ? new Color(0x6cb6ff) : Color.decode("#1565c0"); }
    public static Color waveUnplayed(){ return isDark() ? new Color(0x4a5560) : new Color(0xbcd2e0); }
    public static Color voiceBtnBorder() { return isDark() ? new Color(0x555555) : new Color(0xcccccc); }
    public static Color voiceBtnBg()  { return isDark() ? new Color(0x3a3a3a) : Color.WHITE; }
    public static Color imageBorder() { return isDark() ? new Color(0x444444) : Color.decode("#dddddd"); }
    public static Color hintColor()   { return isDark() ? new Color(0x777777) : Color.decode("#888888"); }
    public static Color inputBorder() { return isDark() ? new Color(0x4a4a4a) : new Color(0xd0d4d9); }
}
