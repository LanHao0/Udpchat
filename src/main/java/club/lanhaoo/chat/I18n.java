package club.lanhaoo.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * 简单的多语言工具：从 classpath 下的 messages_*.properties 读取文案。
 * 使用 UTF-8 编码读取（ResourceBundle 默认按 ISO-8859-1），支持中文。
 */
public class I18n {

    private static Locale locale = Locale.SIMPLIFIED_CHINESE;

    private static ResourceBundle bundle;

    static {
        load();
    }

    public static void setLanguage(String lang) {
        if ("en".equalsIgnoreCase(lang)) {
            locale = Locale.ENGLISH;
        } else {
            locale = Locale.SIMPLIFIED_CHINESE;
        }
        load();
    }

    public static void setLocale(Locale l) {
        locale = (l == null) ? Locale.SIMPLIFIED_CHINESE : l;
        load();
    }

    private static void load() {
        try {
            bundle = ResourceBundle.getBundle("messages", locale, new UTF8Control());
        } catch (Exception e) {
            try {
                bundle = ResourceBundle.getBundle("messages", Locale.SIMPLIFIED_CHINESE, new UTF8Control());
            } catch (Exception ignored) {
                bundle = null;
            }
        }
    }

    public static String getLanguage() {
        return locale.getLanguage().equalsIgnoreCase("en") ? "en" : "zh";
    }

    public static Locale getLocale() {
        return locale;
    }

    /** 获取文案；支持 String.format 占位符（%s/%d ...） */
    public static String get(String key, Object... args) {
        if (bundle == null) return key;
        try {
            String s = bundle.getString(key);
            if (args != null && args.length > 0) {
                s = String.format(s, args);
            }
            return s;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /** 以 UTF-8 读取 .properties 的 ResourceBundle.Control */
    static class UTF8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (InputStream stream = loader.getResourceAsStream(resourceName)) {
                if (stream == null) return null;
                try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    return new PropertyResourceBundle(reader);
                }
            }
        }
    }
}
