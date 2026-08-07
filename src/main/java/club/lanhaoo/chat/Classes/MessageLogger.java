package club.lanhaoo.chat.Classes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 消息历史记录：运行时把每一条“收发”消息（含图片/语音/文件等多媒体）以 JSON 形式
 * 追加写入 logs/ 目录下按日期命名的文本文件（每行一条 JSON），便于回溯聊天历史。
 */
public class MessageLogger {

    private static final File LOGS_DIR = new File(System.getProperty("user.home"), "udpchat/logs");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 记录一条消息。direction 为 "sent"（自己发出）或 "received"（收到） */
    public static synchronized void log(Message m, String direction) {
        if (m == null) return;
        try {
            LOGS_DIR.mkdirs();
            String date = LocalDate_now();
            File file = new File(LOGS_DIR, date + ".txt");
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("time", formatTime(m.getTimestamp()));
            obj.put("direction", direction == null ? "unknown" : direction);
            obj.put("type", m.getType());
            obj.put("mediaType", m.getMediaType());
            obj.put("sender", m.getSender());
            obj.put("fromIp", m.getFromIp());
            obj.put("content", m.getContent());
            String line = GSON.toJson(obj);
            try (BufferedWriter w = new BufferedWriter(new FileWriter(file, true))) {
                w.write(line);
                w.write("\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String LocalDate_now() {
        return java.time.LocalDate.now().format(DATE_FMT);
    }

    private static String formatTime(long ts) {
        if (ts <= 0) return "";
        LocalDateTime t = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(ts), ZoneId.systemDefault());
        return t.format(TIME_FMT);
    }
}
