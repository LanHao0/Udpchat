package club.lanhaoo.chat.Classes;

import club.lanhaoo.chat.HttpFileShare.App;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * 录音工具：使用 javax.sound 采集麦克风，停止后写出为 WAV 文件，
 * 保存到聊天媒体目录（HttpFileShare 可免密访问）。
 */
public class VoiceRecorder {

    // 8kHz / 16bit / 单声道，体积较小、在局域网内传输合适
    private final AudioFormat format = new AudioFormat(8000f, 16, 1, true, true);
    private TargetDataLine line;
    private ByteArrayOutputStream out;
    private Thread recordThread;
    private volatile boolean recording = false;

    /** 开始录音（非阻塞） */
    public void start() throws Exception {
        line = AudioSystem.getTargetDataLine(format);
        line.open(format);
        line.start();
        out = new ByteArrayOutputStream();
        recording = true;
        recordThread = new Thread(() -> {
            byte[] buf = new byte[4096];
            while (recording) {
                int n = line.read(buf, 0, buf.length);
                if (n > 0) out.write(buf, 0, n);
            }
        }, "VoiceRecorder");
        recordThread.start();
    }

    /** 停止录音并写出 WAV，返回文件（位于媒体目录） */
    public File stop() throws Exception {
        recording = false;
        Thread.sleep(150);
        if (line != null) {
            line.stop();
            line.close();
        }
        byte[] data = out.toByteArray();
        File dir = App.getMediaDir();
        File wav = new File(dir, "voice_" + System.currentTimeMillis() + ".wav");
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        AudioInputStream ais = new AudioInputStream(bais, format, data.length / (long) format.getFrameSize());
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wav);
        return wav;
    }
}
