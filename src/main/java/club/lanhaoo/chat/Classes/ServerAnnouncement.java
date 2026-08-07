/**
 * 服务器发现通告对象。
 * 广播格式(JSON): {"ip":"服务器IP","version":"版本","nickname":"昵称"}
 */

package club.lanhaoo.chat.Classes;

import com.google.gson.Gson;

public class ServerAnnouncement {

    //客户端/服务端统一使用的发现端口
    public static final int DISCOVERY_PORT = 2116;

    private String ip;
    private String version;
    private String nickname;

    public ServerAnnouncement(String ip, String version, String nickname) {
        this.ip = ip;
        this.version = version;
        this.nickname = nickname;
    }

    public String getIp() {
        return ip;
    }

    public String getVersion() {
        return version;
    }

    public String getNickname() {
        return nickname;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static ServerAnnouncement fromJson(String json) {
        try {
            return new Gson().fromJson(json, ServerAnnouncement.class);
        } catch (Exception e) {
            return null;
        }
    }

    //显示格式: nickname/ip （无昵称时仅显示 ip）
    @Override
    public String toString() {
        String n = nickname == null ? "" : nickname.trim();
        String i = ip == null ? "" : ip;
        return n.isEmpty() ? i : (n + "/" + i);
    }
}
