/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:7:02 PM_2/6/2020
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat.Classes.UI;

import club.lanhaoo.chat.Classes.Message;
import club.lanhaoo.chat.Server;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.View;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息渲染器：使用 JEditorPane + HTML 渲染，
 * 这样消息体中的 http(s):// 与 www. 链接会被显示为可识别的蓝色下划线超链接，
 * 配合 ClientChat 中的点击命中检测即可“点击打开”。
 */
public class CellRender_Message extends JEditorPane implements ListCellRenderer {

    // 聊天区柔和背景，与气泡形成对比
    private static final Color CHAT_BG = new Color(0xf5f6f8);

    // 用于把链接文本替换成 <a> 标签的正则：
    // 1) http(s):// 或 www. 开头；2) 纯 IPv4（可选端口），如文件分享地址 192.168.1.5:8089
    private static final Pattern URL_PATTERN =
            Pattern.compile("((?:https?://|www\\.)[^\\s<>\"']+|(?:(?:\\d{1,3}\\.){3}\\d{1,3})(?::\\d{1,5})?)",
                    Pattern.CASE_INSENSITIVE);

    // ====== 图片消息：本地缩略图/原图缓存 ======
    // 渲染用缩略图（避免每次重绘都走网络），预览用原图；图片以 http://IP:8089/?chatfile= 形式存在对方/本机文件服务上。
    private static final File IMG_CACHE_DIR = new File(System.getProperty("user.home"), "udpchat_media/cache");
    private static final Map<String, File> THUMB_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, File> RAW_CACHE = new ConcurrentHashMap<>();
    private static final KeySetView<String, Boolean> LOADING = ConcurrentHashMap.newKeySet();
    private static volatile Runnable repaintCallback = null;

    /** ClientChat 注册：图片下载完成后请求聊天列表重绘以显示缩略图 */
    public static void setRepaintCallback(Runnable r) { repaintCallback = r; }

    /** 从 chatfile 参数里拿原始扩展名，作为本地缓存文件的后缀 */
    private static String imgExt(String url) {
        int q = url.indexOf("chatfile=");
        if (q >= 0) {
            String n = url.substring(q + 9);
            int amp = n.indexOf('&');
            if (amp >= 0) n = n.substring(0, amp);
            int d = n.lastIndexOf('.');
            if (d >= 0) return n.substring(d).toLowerCase();
        }
        return ".img";
    }

    /** 从聊天媒体链接中解析出文件名（含中文需 URL decode），用于显示与保存 */
    public static String chatFileName(String url) {
        int q = url.indexOf("chatfile=");
        if (q >= 0) {
            String n = url.substring(q + 9);
            int amp = n.indexOf('&');
            if (amp >= 0) n = n.substring(0, amp);
            try { n = java.net.URLDecoder.decode(n, "UTF-8"); } catch (Exception ignore) { }
            if (!n.isEmpty()) return n;
        }
        int slash = url.lastIndexOf('/');
        return slash >= 0 ? url.substring(slash + 1) : url;
    }

    /** 解析缩略图文件名中内嵌的宽高（命名形如 hash_220x165.jpg），用于 HTML 显式给定尺寸 */
    private static int[] thumbDims(File thumb) {
        String n = thumb.getName();
        int u = n.lastIndexOf('_');
        int d = n.lastIndexOf('.');
        if (u > 0 && d > u) {
            String[] parts = n.substring(u + 1, d).split("x");
            if (parts.length == 2) {
                try { return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])}; }
                catch (NumberFormatException ignore) { }
            }
        }
        return new int[]{220, 165};
    }

    /**
     * 确保原图已下载并在本地生成缩略图，返回缩略图文件（未就绪时返回 null 并后台预取）。
     * 图片消息渲染时调用：立即返回已缓存的缩略图，否则触发后台下载，下载完成后回调重绘。
     */
    private static File getThumb(String url) {
        File f = THUMB_CACHE.get(url);
        if (f != null && f.exists()) return f;
        if (LOADING.add(url)) {
            new Thread(() -> {
                try {
                    IMG_CACHE_DIR.mkdirs();
                    String hash = Integer.toHexString(url.hashCode());
                    File raw = new File(IMG_CACHE_DIR, hash + "_raw" + imgExt(url));
                    if (!raw.exists()) {
                        try (InputStream in = new URI(url).toURL().openStream();
                             FileOutputStream out = new FileOutputStream(raw)) {
                            byte[] buf = new byte[8192];
                            int n;
                            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                        }
                    }
                    RAW_CACHE.put(url, raw);
                    BufferedImage img = ImageIO.read(raw);
                    if (img != null) {
                        int w = img.getWidth(), h = img.getHeight();
                        int maxW = 220, maxH = 300;
                        double scale = Math.min(Math.min((double) maxW / w, (double) maxH / h), 1.0);
                        int tw = Math.max(1, (int) (w * scale));
                        int th = Math.max(1, (int) (h * scale));
                        BufferedImage t = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
                        java.awt.Graphics2D g = t.createGraphics();
                        g.drawImage(img, 0, 0, tw, th, null);
                        g.dispose();
                        File tf = new File(IMG_CACHE_DIR, hash + "_" + tw + "x" + th + ".jpg");
                        ImageIO.write(t, "jpg", tf);
                        THUMB_CACHE.put(url, tf);
                    }
                    if (repaintCallback != null) SwingUtilities.invokeLater(repaintCallback);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    LOADING.remove(url);
                }
            }, "img-cache-" + url.hashCode()).start();
        }
        return null;
    }

    /** 供预览使用：同步获取原图本地文件（已有缓存直接返回，否则下载） */
    public static File getRawSync(String url) throws Exception {
        File f = RAW_CACHE.get(url);
        if (f != null && f.exists()) return f;
        IMG_CACHE_DIR.mkdirs();
        String hash = Integer.toHexString(url.hashCode());
        File raw = new File(IMG_CACHE_DIR, hash + "_raw" + imgExt(url));
        if (!raw.exists()) {
            try (InputStream in = new URI(url).toURL().openStream();
                 FileOutputStream out = new FileOutputStream(raw)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            }
        }
        RAW_CACHE.put(url, raw);
        return raw;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        Message m = (Message) value;

        // 图片消息用真正的组件渲染（ImageIcon 高度同步可知），避免 HTML <img> 异步加载导致单元格高度被截短
        if ("image".equals(m.getMediaType())) {
            return buildImageComponent(m, list, isSelected);
        }

        setContentType("text/html");
        setEditable(false);
        setOpaque(true);
        setBackground(CHAT_BG);
        // 设定宽度，确保按列宽正确换行，从而计算正确的单元格高度（变高）
        int w = list.getWidth();
        if (w <= 0) w = 200;
        setText(toHtml(m, isSelected));
        setSize(w, 1);
        return this;
    }

    /**
     * 用真实 Swing 组件渲染图片消息：图片以 ImageIcon 同步加载，单元格高度立即可知，
     * 因此可变高度列表能完整展示整张缩略图（不再被截断）。点击/右键放大由 ClientChat 处理。
     */
    private Component buildImageComponent(Message m, JList list, boolean selected) {
        boolean own = m.getFromIp() != null && m.getFromIp().equals(Server.getIpAddress());
        String bubbleBg = own ? "#d6ecff" : (selected ? "#d0e8ec" : "#ffffff");
        String borderColor = own ? "#a9d4f5" : "#e3e5e8";
        String senderColor = own ? "#0b6cb5" : "#0c6b58";
        String sender = (m.getSender() != null) ? m.getSender() : m.getFromIp();
        if (sender == null) sender = "";

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);
        panel.setBackground(CHAT_BG);
        int w = list.getWidth();
        if (w <= 0) w = 200;
        panel.setSize(w, 1);

        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(Color.decode(bubbleBg));
        bubble.setBorder(BorderFactory.createLineBorder(Color.decode(borderColor)));
        bubble.setAlignmentX(own ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);
        bubble.setMaximumSize(new Dimension(260, Integer.MAX_VALUE));

        JLabel senderLbl = new JLabel(sender);
        senderLbl.setForeground(Color.decode(senderColor));
        senderLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        bubble.add(senderLbl);

        File thumb = getThumb(m.getContent());
        if (thumb != null && thumb.exists()) {
            try {
                BufferedImage bi = ImageIO.read(thumb);
                int maxW = 240;
                int iw = bi.getWidth(), ih = bi.getHeight();
                int dw = Math.min(iw, maxW);
                int dh = (int) Math.round(ih * (dw / (double) iw));
                JLabel imgLbl = new JLabel(new ImageIcon(bi.getScaledInstance(dw, dh, Image.SCALE_SMOOTH)));
                imgLbl.setBorder(BorderFactory.createLineBorder(new Color(0xdddddd)));
                bubble.add(imgLbl);
            } catch (Exception ex) {
                bubble.add(new JLabel("图片加载失败"));
            }
        } else {
            bubble.add(new JLabel("🖼 图片 · 加载中…"));
        }
        bubble.add(new JLabel("点击 / 右键放大"));
        panel.add(bubble);
        return panel;
    }

    /**
     * 重写首选尺寸：JEditorPane 作为 JList 渲染器时，因未加入可见容器，
     * 默认 getPreferredSize 不会按当前宽度换行，导致变高单元格高度计算错误、消息被截断。
     * 这里手动按当前宽度布局根视图，返回换行后的真实高度。
     */
    @Override
    public Dimension getPreferredSize() {
        int w = getWidth();
        if (w <= 0) {
            Container parent = getParent();
            if (parent != null) w = parent.getWidth();
        }
        if (w <= 0) w = 200;
        View root = getUI().getRootView(this);
        if (root != null) {
            root.setSize(w, Integer.MAX_VALUE);
            int h = (int) Math.ceil(root.getPreferredSpan(View.Y_AXIS));
            Insets ins = getInsets();
            return new Dimension(w, h + ins.top + ins.bottom);
        }
        return super.getPreferredSize();
    }

    /**
     * 生成用于渲染（与点击命中检测）的 HTML 字符串。
     * 注意：渲染与命中检测必须使用完全相同的 HTML，坐标映射才会准确。
     */
    public static String toHtml(Message m, boolean selected) {
        String sender = (m.getSender() != null) ? m.getSender() : m.getFromIp();
        if (sender == null) sender = "";
        String content = (m.getContent() != null) ? m.getContent() : "";
        // 空白/纯空白内容不渲染（避免“加入”等环节出现空白气泡）
        if (content.trim().isEmpty()) {
            return "";
        }

        // 系统/本地提示消息（如“已开启服务器”）：居中、弱化显示，链接可点击
        if ("local".equals(m.getType())) {
            return "<html><body style='margin:0;padding:4px 8px;text-align:center;"
                    + "color:#9aa0a6;font-size:11px;'>"
                    + linkify(escapeHtml(content)) + "</body></html>";
        }

        // 自己的消息靠右、蓝色调；他人消息靠左、浅灰调
        boolean own = m.getFromIp() != null && m.getFromIp().equals(Server.getIpAddress());
        String bubbleBg = own ? "#d6ecff" : (selected ? "#d0e8ec" : "#ffffff");
        String borderColor = own ? "#a9d4f5" : "#e3e5e8";
        String align = own ? "right" : "left";
        String senderColor = own ? "#0b6cb5" : "#0c6b58";

        // 媒体消息（语音/图片/文件）：语音渲染为可点击链接，图片直接内联显示缩略图，文件渲染为下载链接
        String media = m.getMediaType();
        if ("voice".equals(media) || "image".equals(media) || "file".equals(media)) {
            String href = escapeHtml(content);
            StringBuilder sbm = new StringBuilder();
            sbm.append("<html><body style='margin:0;'>");
            sbm.append("<table width='100%' cellpadding='6' cellspacing='0'><tr><td align='")
                    .append(align).append("'>");
            sbm.append("<div style='width:82%; background:").append(bubbleBg)
                    .append("; border:1px solid ").append(borderColor)
                    .append("; margin:2px 0; text-align:left;")
                    .append(" font-family:Microsoft YaHei,SimSun,sans-serif; font-size:12px; color:#222;'>");
            sbm.append("<div style='color:").append(senderColor)
                    .append("; font-weight:bold; font-size:11px; margin-bottom:2px;'>")
                    .append(escapeHtml(sender)).append("</div>");
            if ("voice".equals(media)) {
                sbm.append("<div><a href=\"").append(href)
                        .append("\" style=\"color:#1565c0;text-decoration:underline;\">")
                        .append("🔊 语音消息 · 点击播放</a></div>");
            } else if ("image".equals(media)) {
                File thumb = getThumb(content);
                if (thumb != null && thumb.exists()) {
                    int[] dim = thumbDims(thumb);
                    // 图片直接内联显示；显式给定宽高避免异步加载导致行高错乱；外层 <a> 便于点击命中检测
                    sbm.append("<div style='text-align:center;'><a href=\"").append(href)
                            .append("\"><img src=\"file:///").append(escapeHtml(thumb.getAbsolutePath().replace('\\', '/')))
                            .append("\" width=\"").append(dim[0]).append("\" height=\"").append(dim[1])
                            .append("\" style=\"border:1px solid #ddd;border-radius:4px;\"></a></div>");
                    sbm.append("<div style='color:#888;font-size:10px;text-align:center;'>点击 / 右键放大</div>");
                } else {
                    sbm.append("<div><a href=\"").append(href)
                            .append("\" style=\"color:#1565c0;text-decoration:underline;\">")
                            .append("🖼️ 图片 · 加载中…</a></div>");
                }
            } else {
                // 文件消息：可点击下载并在本地打开
                String fname = escapeHtml(chatFileName(content));
                sbm.append("<div><a href=\"").append(href)
                        .append("\" style=\"color:#1565c0;text-decoration:underline;\">")
                        .append("📎 ").append(fname).append(" · 点击下载</a></div>");
            }
            sbm.append("</div></td></tr></table>");
            sbm.append("</body></html>");
            return sbm.toString();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='margin:0;'>");
        // 用表格单元格的 align 实现整块左右对齐（Swing HTML 可靠支持）
        sb.append("<table width='100%' cellpadding='6' cellspacing='0'><tr><td align='")
                .append(align).append("'>");
        sb.append("<div style='width:82%; background:").append(bubbleBg)
                .append("; border:1px solid ").append(borderColor)
                .append("; margin:2px 0; text-align:left;")
                .append(" font-family:Microsoft YaHei,SimSun,sans-serif; font-size:12px; color:#222;'>");
        sb.append("<div style='color:").append(senderColor)
                .append("; font-weight:bold; font-size:11px; margin-bottom:2px;'>")
                .append(escapeHtml(sender)).append("</div>");
        sb.append("<div>").append(linkify(escapeHtml(content))).append("</div>");
        sb.append("</div></td></tr></table>");
        sb.append("</body></html>");
        return sb.toString();
    }

    /** 转义 HTML 特殊字符，避免消息内容破坏页面结构 */
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /** 把文本中的链接替换成带样式的 <a> 标签（不二次扫描已替换的内容） */
    private static String linkify(String text) {
        if (text == null || text.isEmpty()) return "";
        Matcher mt = URL_PATTERN.matcher(text);
        StringBuffer out = new StringBuffer();
        while (mt.find()) {
            String url = mt.group(1);
            String href;
            if (url.startsWith("www.")) {
                href = "http://" + url;
            } else if (url.startsWith("http://") || url.startsWith("https://")) {
                href = url;
            } else {
                // 纯 IPv4（可选端口）也按 http 链接处理，如文件分享地址
                href = "http://" + url;
            }
            // $ 在替换串里有特殊含义，用 Matcher.quoteReplacement 包裹
            String replacement = "<a href=\"" + href +
                    "\" style=\"color:#1565c0;text-decoration:underline;\">" + url + "</a>";
            mt.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        mt.appendTail(out);
        // 换行符转为 <br>
        return out.toString().replace("\n", "<br>");
    }
}
