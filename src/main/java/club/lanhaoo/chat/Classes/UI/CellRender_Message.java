/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:7:02 PM_2/6/2020
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat.Classes.UI;

import club.lanhaoo.chat.Classes.Message;
import club.lanhaoo.chat.Classes.ThemeUtil;
import club.lanhaoo.chat.Server;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
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
                        int maxW = 240, maxH = 320;
                        double scale = Math.min(Math.min((double) maxW / w, (double) maxH / h), 1.0);
                        int tw = Math.max(1, (int) (w * scale));
                        int th = Math.max(1, (int) (h * scale));
                        BufferedImage t = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
                        java.awt.Graphics2D g = t.createGraphics();
                        // 高质量缩放，避免缩略图发虚
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                                java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                        g.drawImage(img, 0, 0, tw, th, null);
                        g.dispose();
                        File tf = new File(IMG_CACHE_DIR, hash + "_" + tw + "x" + th + ".jpg");
                        writeJpeg(t, tf, 0.92f);
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

    // ====== 气泡配色（现代化，随主题动态变化，见 ThemeUtil）======
    private static final int BUBBLE_RADIUS = 12;

    // ====== 语音消息：本地缓存 + 波形/时长解析 + 播放控制 ======
    private static final File VOICE_CACHE_DIR = new File(System.getProperty("user.home"), "udpchat_media/voice");
    private static final int VOICE_BARS = 44;
    private static final Map<String, VoiceData> VOICE_CACHE = new ConcurrentHashMap<>();
    private static final KeySetView<String, Boolean> VLOADING = ConcurrentHashMap.newKeySet();
    private static final Map<String, Long> PLAY_POS = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> PLAYING = new ConcurrentHashMap<>();
    private static volatile Runnable progressRepaint = null;
    private static Clip activeClip = null;
    private static String activeUrl = null;
    private static javax.swing.Timer playTimer = null;
    private static String pendingPlayUrl = null;

    private static class VoiceData {
        File file;
        long durationMs = -1;
        int[] peaks;
    }

    /** ClientChat 注册：播放进度变化时只做轻量重绘（高度不变，无需重排行高） */
    public static void setProgressRepaint(Runnable r) { progressRepaint = r; }

    /** 确保语音文件已下载并解析，返回 VoiceData（未就绪返回 null 并后台预取）；预取完成会触发重绘与待播放 */
    private static VoiceData getVoice(String url) {
        VoiceData vd = VOICE_CACHE.get(url);
        if (vd != null && vd.file != null && vd.file.exists()) return vd;
        if (VLOADING.add(url)) {
            final String u = url;
            new Thread(() -> {
                try {
                    VOICE_CACHE_DIR.mkdirs();
                    String hash = Integer.toHexString(u.hashCode());
                    File f = new File(VOICE_CACHE_DIR, hash + "_" + Math.abs(chatFileName(u).hashCode()) + ".wav");
                    if (!f.exists()) {
                        try (InputStream in = new URI(u).toURL().openStream();
                             FileOutputStream out = new FileOutputStream(f)) {
                            byte[] buf = new byte[8192];
                            int n;
                            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                        }
                    }
                    VoiceData nv = parseVoice(f);
                    nv.file = f;
                    VOICE_CACHE.put(u, nv);
                    Runnable r = (progressRepaint != null) ? progressRepaint : repaintCallback;
                    if (r != null) SwingUtilities.invokeLater(r);
                    if (u.equals(pendingPlayUrl)) {
                        pendingPlayUrl = null;
                        SwingUtilities.invokeLater(() -> togglePlay(u));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    VLOADING.remove(u);
                }
            }, "voice-cache-" + url.hashCode()).start();
        }
        return null;
    }

    /** 解析音频：时长(ms) + 归一化波形峰值（仅支持 javax.sound 可解码格式，主要是应用自身产生的 WAV） */
    private static VoiceData parseVoice(File f) {
        VoiceData vd = new VoiceData();
        vd.peaks = new int[VOICE_BARS];
        for (int i = 0; i < VOICE_BARS; i++) vd.peaks[i] = 140;
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(f);
            AudioFormat fmt = ais.getFormat();
            long frames = ais.getFrameLength();
            if (frames > 0 && fmt.getSampleRate() > 0) {
                vd.durationMs = (long) (frames / fmt.getSampleRate() * 1000.0);
            }
            int sampleSize = fmt.getSampleSizeInBits();
            int channels = fmt.getChannels();
            boolean big = fmt.isBigEndian();
            byte[] data;
            try { data = ais.readAllBytes(); } finally { try { ais.close(); } catch (Exception ignore) {} }
            int bytesPerSample = Math.max(1, (sampleSize + 7) / 8);
            int frameSize = bytesPerSample * channels;
            if (frameSize > 0 && data.length >= frameSize) {
                int totalSamples = data.length / frameSize;
                int per = Math.max(1, totalSamples / VOICE_BARS);
                long maxAbs = 1;
                for (int i = 0; i < totalSamples; i++) {
                    long a = Math.abs(sampleValue(data, i * frameSize, bytesPerSample, big));
                    if (a > maxAbs) maxAbs = a;
                }
                for (int b = 0; b < VOICE_BARS; b++) {
                    long peak = 0;
                    int start = b * per;
                    int end = Math.min(totalSamples, start + per);
                    for (int i = start; i < end; i++) {
                        long a = Math.abs(sampleValue(data, i * frameSize, bytesPerSample, big));
                        if (a > peak) peak = a;
                    }
                    int norm = (int) (1000.0 * peak / maxAbs);
                    vd.peaks[b] = Math.max(50, Math.min(1000, norm));
                }
            }
        } catch (Exception ex) {
            // 解析失败：保留默认波形，时长维持 -1（未知）
        }
        return vd;
    }

    private static long sampleValue(byte[] d, int off, int bytesPerSample, boolean big) {
        if (off + bytesPerSample > d.length) return 0;
        if (bytesPerSample == 1) {
            return (d[off] & 0xff) - 128; // 8bit 无符号
        } else {
            int v = big ? (((d[off] & 0xff) << 8) | (d[off + 1] & 0xff))
                        : (((d[off + 1] & 0xff) << 8) | (d[off] & 0xff));
            if (v > 32767) v -= 65536; // 补码转有符号
            return v;
        }
    }

    /** 切换播放/暂停（点击语音气泡时调用） */
    public static void togglePlay(String url) {
        if (activeClip != null && activeUrl != null && activeUrl.equals(url)) {
            stopPlayback();
            return;
        }
        VoiceData vd = VOICE_CACHE.get(url);
        if (vd == null || vd.file == null || !vd.file.exists()) {
            pendingPlayUrl = url;
            getVoice(url); // 未就绪先预取，加载完自动开始播放
            return;
        }
        if (vd.durationMs < 0) {
            openInBrowserFallback(url); // 无法用 Clip 解码（如非 WAV），回退浏览器
            return;
        }
        try {
            stopPlayback();
            AudioInputStream ais = AudioSystem.getAudioInputStream(vd.file);
            Clip c = AudioSystem.getClip();
            c.open(ais);
            activeClip = c;
            activeUrl = url;
            PLAYING.put(url, true);
            PLAY_POS.put(url, 0L);
            c.start();
            c.addLineListener(ev -> {
                if (ev.getType() == LineEvent.Type.STOP) {
                    SwingUtilities.invokeLater(() -> onPlaybackEnded(url));
                }
            });
            playTimer = new javax.swing.Timer(80, e -> {
                if (activeClip == null) return;
                long pos = activeClip.getMicrosecondPosition() / 1000;
                PLAY_POS.put(url, pos);
                if (progressRepaint != null) progressRepaint.run();
            });
            playTimer.start();
        } catch (Exception ex) {
            openInBrowserFallback(url);
        }
    }

    private static void onPlaybackEnded(String url) {
        if (activeUrl != null && activeUrl.equals(url)) stopPlayback();
        else { PLAYING.remove(url); PLAY_POS.remove(url); if (progressRepaint != null) progressRepaint.run(); }
    }

    private static void stopPlayback() {
        if (playTimer != null) { playTimer.stop(); playTimer = null; }
        if (activeClip != null) {
            try { activeClip.stop(); activeClip.close(); } catch (Exception ignore) {}
            String u = activeUrl;
            activeClip = null;
            activeUrl = null;
            if (u != null) { PLAYING.remove(u); PLAY_POS.remove(u); }
            if (progressRepaint != null) progressRepaint.run();
        }
    }

    private static void openInBrowserFallback(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                String u = url;
                if (!u.startsWith("http://") && !u.startsWith("https://")) u = "http://" + u;
                java.awt.Desktop.getDesktop().browse(new java.net.URI(u));
            }
        } catch (Exception ignore) {}
    }

    private static String fmtVoiceTime(long dur, long pos, boolean playing) {
        if (dur <= 0) return "--:--";
        if (playing) return "剩余 " + mmss(Math.max(0, dur - pos));
        return mmss(dur);
    }
    private static String mmss(long ms) {
        long s = ms / 1000;
        return (s / 60) + ":" + (s % 60 < 10 ? "0" : "") + (s % 60);
    }

    /** 高质量写出 JPEG（指定压缩质量，避免缩略图发虚） */
    private static void writeJpeg(BufferedImage img, File f, float quality) throws Exception {
        javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpg").next();
        javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(f);
        writer.setOutput(ios);
        javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }
        writer.write(null, new javax.imageio.IIOImage(img, null, null), param);
        writer.dispose();
        ios.close();
    }

    // ====== 圆角气泡面板（替代直角边框，现代化外观）======
    private static class BubblePanel extends JPanel {
        private final Color bg, border;
        BubblePanel(Color bg, Color border) {
            this.bg = bg; this.border = border;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUBBLE_RADIUS, BUBBLE_RADIUS);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BUBBLE_RADIUS, BUBBLE_RADIUS);
            g2.dispose();
        }
    }

    private static String fmtTime(long ts) {
        if (ts <= 0) return "";
        java.time.LocalDateTime t = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(ts), java.time.ZoneId.systemDefault());
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    private static JLabel timeLabel(long ts) {
        JLabel l = new JLabel(fmtTime(ts));
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        l.setForeground(ThemeUtil.timeColor());
        l.setAlignmentX(RIGHT_ALIGNMENT);
        return l;
    }

    private static String timeDivHtml(Message m) {
        String t = fmtTime(m.getTimestamp());
        if (t.isEmpty()) return "";
        return "<div style='text-align:right;color:" + ThemeUtil.hex(ThemeUtil.timeColor())
                + ";font-size:10px;margin-top:2px;'>"
                + escapeHtml(t) + "</div>";
    }

    // ====== 语音消息组件（播放按钮 + 波形 + 剩余秒数）======
    private Component buildVoiceComponent(Message m, JList list, boolean selected) {
        boolean own = m.getFromIp() != null && m.getFromIp().equals(Server.getIpAddress());
        Color bg = own ? ThemeUtil.ownBg() : ThemeUtil.otherBg();
        Color border = own ? ThemeUtil.ownBorder() : ThemeUtil.otherBorder();
        Color senderColor = own ? ThemeUtil.ownSender() : ThemeUtil.otherSender();
        String sender = (m.getSender() != null) ? m.getSender() : m.getFromIp();
        if (sender == null) sender = "";

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);
        panel.setBackground(ThemeUtil.chatBg());
        int w = list.getWidth();
        if (w <= 0) w = 200;
        panel.setSize(w, 1);

        BubblePanel bubble = new BubblePanel(bg, border);
        bubble.setAlignmentX(own ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);
        bubble.setMaximumSize(new Dimension(280, Integer.MAX_VALUE));

        JLabel senderLbl = new JLabel(sender);
        senderLbl.setForeground(senderColor);
        senderLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        bubble.add(senderLbl);

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        String url = m.getContent();
        VoiceData vd = getVoice(url);
        boolean playing = PLAYING.getOrDefault(url, false);
        long pos = PLAY_POS.getOrDefault(url, 0L);
        long dur = (vd != null) ? vd.durationMs : -1;

        JLabel playBtn = new JLabel(playing ? "⏸" : "▶");
        playBtn.setFont(new Font("Dialog", Font.PLAIN, 16));
        playBtn.setBorder(BorderFactory.createLineBorder(ThemeUtil.voiceBtnBorder()));
        playBtn.setOpaque(true);
        playBtn.setBackground(ThemeUtil.voiceBtnBg());
        playBtn.setHorizontalAlignment(SwingConstants.CENTER);
        playBtn.setPreferredSize(new Dimension(30, 30));
        playBtn.setMaximumSize(new Dimension(30, 30));
        row.add(playBtn);
        row.add(Box.createHorizontalStrut(8));

        WavePanel wave = new WavePanel(vd, pos, playing, dur, own);
        row.add(wave);
        row.add(Box.createHorizontalStrut(8));

        JLabel timeLbl = new JLabel(fmtVoiceTime(dur, pos, playing));
        timeLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        timeLbl.setForeground(ThemeUtil.timeColor());
        row.add(timeLbl);

        bubble.add(row);
        bubble.add(timeLabel(m.getTimestamp()));
        panel.add(bubble);
        return panel;
    }

    private static class WavePanel extends JPanel {
        private final int[] peaks;
        private final long pos, dur;
        private final boolean playing;
        private final boolean own;
        WavePanel(VoiceData vd, long pos, boolean playing, long dur, boolean own) {
            this.peaks = (vd != null && vd.peaks != null) ? vd.peaks : null;
            this.pos = pos; this.playing = playing; this.dur = dur; this.own = own;
            setPreferredSize(new Dimension(150, 36));
            setMaximumSize(new Dimension(150, 36));
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth(), h = getHeight();
            int n = (peaks != null) ? peaks.length : VOICE_BARS;
            int gap = 2;
            int bw = Math.max(1, (w - gap * (n - 1)) / n);
            long duration = (dur > 0) ? dur : (playing ? Math.max(pos, 1) : 1);
            double progress = (duration > 0 && pos > 0) ? Math.min(1.0, pos / (double) duration) : 0.0;
            int playedX = (int) (progress * w);
            Color playedColor = own ? ThemeUtil.ownSender() : ThemeUtil.otherSender();
            for (int i = 0; i < n; i++) {
                int ph = (peaks != null) ? peaks[i] : 140;
                int bh = (int) (h * ph / 1000.0);
                if (bh < 2) bh = 2;
                int x = i * (bw + gap);
                int y = (h - bh) / 2;
                boolean played = x < playedX;
                g.setColor(played ? playedColor : ThemeUtil.waveUnplayed());
                g.fillRect(x, y, bw, bh);
            }
            if (playing && progress > 0) {
                g.setColor(new Color(0xff5500));
                g.drawLine(playedX, 0, playedX, h);
            }
        }
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        Message m = (Message) value;

        // 图片/语音消息用真正的组件渲染，避免 HTML <img>/异步内容导致单元格高度被截短
        if ("image".equals(m.getMediaType())) {
            return buildImageComponent(m, list, isSelected);
        }
        if ("voice".equals(m.getMediaType())) {
            return buildVoiceComponent(m, list, isSelected);
        }

        setContentType("text/html");
        setEditable(false);
        setOpaque(true);
        setBackground(ThemeUtil.chatBg());
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
        Color bg = own ? ThemeUtil.ownBg() : ThemeUtil.otherBg();
        Color border = own ? ThemeUtil.ownBorder() : ThemeUtil.otherBorder();
        Color senderColor = own ? ThemeUtil.ownSender() : ThemeUtil.otherSender();
        String sender = (m.getSender() != null) ? m.getSender() : m.getFromIp();
        if (sender == null) sender = "";

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);
        panel.setBackground(ThemeUtil.chatBg());
        int w = list.getWidth();
        if (w <= 0) w = 200;
        panel.setSize(w, 1);

        BubblePanel bubble = new BubblePanel(bg, border);
        bubble.setAlignmentX(own ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);
        bubble.setMaximumSize(new Dimension(260, Integer.MAX_VALUE));

        JLabel senderLbl = new JLabel(sender);
        senderLbl.setForeground(senderColor);
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
                imgLbl.setBorder(BorderFactory.createLineBorder(ThemeUtil.imageBorder()));
                bubble.add(imgLbl);
            } catch (Exception ex) {
                bubble.add(new JLabel("图片加载失败"));
            }
        } else {
            bubble.add(new JLabel("🖼 图片 · 加载中…"));
        }
        JLabel hint = new JLabel("点击 / 右键放大");
        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        hint.setForeground(ThemeUtil.hintColor());
        bubble.add(hint);
        bubble.add(timeLabel(m.getTimestamp()));
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
                    + "color:" + ThemeUtil.hex(ThemeUtil.timeColor()) + ";font-size:11px;'>"
                    + linkify(escapeHtml(content)) + "</body></html>";
        }

        // 自己的消息靠右、蓝色调；他人消息靠左、绿色调（现代化）
        boolean own = m.getFromIp() != null && m.getFromIp().equals(Server.getIpAddress());
        String bubbleBg = ThemeUtil.hex(own ? ThemeUtil.ownBg() : ThemeUtil.otherBg());
        String borderColor = ThemeUtil.hex(own ? ThemeUtil.ownBorder() : ThemeUtil.otherBorder());
        String align = own ? "right" : "left";
        String senderColor = ThemeUtil.hex(own ? ThemeUtil.ownSender() : ThemeUtil.otherSender());
        String bubbleText = ThemeUtil.hex(ThemeUtil.bubbleText());
        String linkColor = ThemeUtil.hex(ThemeUtil.linkColor());
        String imgBorder = ThemeUtil.hex(ThemeUtil.imageBorder());
        String hintColor = ThemeUtil.hex(ThemeUtil.hintColor());

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
                    .append("; margin:2px 0; text-align:left; border-radius:12px;")
                    .append(" font-family:Microsoft YaHei,SimSun,sans-serif; font-size:12px; color:").append(bubbleText).append(";'>");
            sbm.append("<div style='color:").append(senderColor)
                    .append("; font-weight:bold; font-size:11px; margin-bottom:2px;'>")
                    .append(escapeHtml(sender)).append("</div>");
            if ("voice".equals(media)) {
                sbm.append("<div><a href=\"").append(href)
                        .append("\" style=\"color:").append(linkColor).append(";text-decoration:underline;\">")
                        .append("🔊 语音消息 · 点击播放</a></div>");
            } else if ("image".equals(media)) {
                File thumb = getThumb(content);
                if (thumb != null && thumb.exists()) {
                    int[] dim = thumbDims(thumb);
                    // 图片直接内联显示；显式给定宽高避免异步加载导致行高错乱；外层 <a> 便于点击命中检测
                    sbm.append("<div style='text-align:center;'><a href=\"").append(href)
                            .append("\"><img src=\"file:///").append(escapeHtml(thumb.getAbsolutePath().replace('\\', '/')))
                            .append("\" width=\"").append(dim[0]).append("\" height=\"").append(dim[1])
                            .append("\" style=\"border:1px solid ").append(imgBorder).append(";border-radius:4px;\"></a></div>");
                    sbm.append("<div style='color:").append(hintColor).append(";font-size:10px;text-align:center;'>点击 / 右键放大</div>");
                } else {
                    sbm.append("<div><a href=\"").append(href)
                            .append("\" style=\"color:").append(linkColor).append(";text-decoration:underline;\">")
                            .append("🖼️ 图片 · 加载中…</a></div>");
                }
            } else {
                // 文件消息：可点击下载并在本地打开
                String fname = escapeHtml(chatFileName(content));
                sbm.append("<div><a href=\"").append(href)
                        .append("\" style=\"color:").append(linkColor).append(";text-decoration:underline;\">")
                        .append("📎 ").append(fname).append(" · 点击下载</a></div>");
            }
            sbm.append(timeDivHtml(m));
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
                .append("; margin:2px 0; text-align:left; border-radius:12px;")
                .append(" font-family:Microsoft YaHei,SimSun,sans-serif; font-size:12px; color:").append(bubbleText).append(";'>");
        sb.append("<div style='color:").append(senderColor)
                .append("; font-weight:bold; font-size:11px; margin-bottom:2px;'>")
                .append(escapeHtml(sender)).append("</div>");
        sb.append("<div>").append(linkify(escapeHtml(content))).append("</div>");
        sb.append(timeDivHtml(m));
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
                    "\" style=\"color:" + ThemeUtil.hex(ThemeUtil.linkColor()) + ";text-decoration:underline;\">" + url + "</a>";
            mt.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        mt.appendTail(out);
        // 换行符转为 <br>
        return out.toString().replace("\n", "<br>");
    }
}
