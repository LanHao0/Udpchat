package club.lanhaoo.chat;

import club.lanhaoo.chat.Classes.Message;
import club.lanhaoo.chat.Classes.ServerAnnouncement;
import club.lanhaoo.chat.Classes.UserSettings;
import com.google.gson.Gson;

import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:10:26 PM_6/17/2019
 * @Magic_Power_Of_Code!
 */


public class Server {
    //服务器通告版本号（随发现通告一起广播）
    public static final String VERSION = "1.0.0";

    static Set<String> receivedMessageIds =
            ConcurrentHashMap.newKeySet();

    //已知客户端IP（每次收到消息都登记），用于单播转发，避免依赖UDP广播
    static Set<String> clientIps =
            ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws Exception {
        String nickname = (args != null && args.length > 0 && args[0] != null && !args[0].trim().isEmpty())
                ? args[0].trim()
                : defaultNickname();
        startServer(nickname);
    }

    /**
     * 启动服务器核心逻辑。独立进程( main )与客户端“开启服务器”按钮（后台线程）共用。
     */
    public static void startServer(String nickname) throws Exception {
        System.out.println("开始服务端");
        System.out.println("服务器昵称: " + nickname + " (发现端口 " + ServerAnnouncement.DISCOVERY_PORT + ")");
        startDiscoveryBroadcast(nickname);

        InetAddress localHost = InetAddress.getLocalHost();
        //保存已经处理过的消息ID

        byte[] bytes = new byte[1024];

        DatagramSocket datagramSocket = new DatagramSocket(2112);
        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
        System.out.println("在 " + localHost + "端口： 2112" + "上运行服务端 ");
        boolean ServerOn = true;

        ArrayList Iplist = new ArrayList();
        ArrayList BanIp = new ArrayList();

        String serverPassword = "mima111";

        while (ServerOn) {

            datagramSocket.receive(datagramPacket);
            //收到消息

            String pure_message = new String(datagramPacket.getData(), 0, datagramPacket.getLength(), StandardCharsets.UTF_8);
            System.out.println(pure_message);
            String fromIP = datagramPacket.getAddress().getHostAddress();

            //登记客户端IP，后续用单播转发，避免广播不可达
            clientIps.add(fromIP);

            Gson gson = new Gson();
            Message message = gson.fromJson(pure_message, Message.class);
            if (message == null) {
                //丢弃无法解析的报文，避免服务端崩溃
                continue;
            }

            String message_content = message.getContent();
            String command = message.getType();
            //客户端把 "SYSTEM_COMMAND.xxx" / "UserCommand.xxx" 放在 command 字段，
            //而不是 type 字段，因此需要单独读取
            String realCommand = message.getCommand();

            if("ACK".equals(command)){
                continue;
            }
            String messageId = message.getMessageId();


//没有ID，说明是旧客户端
            if (messageId == null) {

                messageId = UUID.randomUUID().toString();
                message.setMessageId(messageId);

            }


//重复消息检测
            if(receivedMessageIds.contains(messageId)) {

                System.out.println(
                        "重复消息:" + messageId
                );

                //重复消息也要回复ACK
                Message ack = new Message(
                        "ACK",
                        "",
                        messageId
                );

                //客户端在 12251 监听，ACK 必须发到该端口
                ack.sendToClient(fromIP);


                continue;
            }


//记录
            receivedMessageIds.add(messageId);
            if(receivedMessageIds.size()>10000){

                receivedMessageIds.clear();

            }
            //todo 超级命令登陆ip

            //ACK确认
            Message ack = new Message(
                    "ACK",
                    "",
                    message.getMessageId()
            );


            //客户端在 12251 监听，ACK 必须发到该端口
            ack.sendToClient(fromIP);


            if (BanIp.contains(fromIP)) {
                //如果来自被封禁IP，则不进行操作
                String bannedtips = "你已被管理员封禁，无法发送群消息&[系统消息]";

                Message message_back = new Message("text", "", bannedtips);
                if (!message_back.sendToClient(fromIP)) {
                    System.out.println("发送失败\n");
                }
                System.out.println("来自封禁Ip:" + fromIP + "内容:" + pure_message);
                continue;

            } else {

                //config
                String endCommand = "ENDSERVER";
                String banipCommand = "BANIP";
                String unbanipCommand = "UNBAN";
                String ServerOfftips = localHost + "服务器下线&[系统消息]";

                String UserCommand_Nonamesend = "NoNameSend";
                //config


                // 广播前检测
                if (realCommand != null && realCommand.contains("SYSTEM_COMMAND")) {
                    if (message_content.contains(serverPassword)) {
                        String mcontent = null;

                        if (realCommand.contains(banipCommand)) {
                            BanIp.add(message_content.split("#")[2]);
                            System.out.println("Banned ip:" + message_content.split("#")[2]);
                            mcontent = "已封禁IP: " + fromIP;

                        }

                        if (realCommand.contains(unbanipCommand)) {
                            BanIp.remove(message_content.split("#")[2]);
                            System.out.println("Unbanned ip:" + message_content.split("#")[2]);
                            mcontent = "解封IP: " + fromIP;

                        }

                        if (realCommand.contains(endCommand)) {
                            System.out.println(ServerOfftips);

                            mcontent = ServerOfftips;

                            // todo stop Broadcasting message instead of break the loop
                            break;
                        }

                        long mtime = new Date().getTime();
                        Message message_go = new Message(
                                "system",
                                "",
                                mcontent);
                        broadcastToClients(message_go);
                        continue;
                    }

                }

                if (realCommand != null && realCommand.contains("UserCommand")) {
                    if (realCommand.contains(UserCommand_Nonamesend)) {
                        message.setSender("[匿名消息]");
                        broadcastToClients(message);
                    }

                    continue;
                }


                //广播消息
                System.out.println("广播来自 " + message.getFromIp() + " 的消息 " + message.getContent());
                broadcastToClients(message);
            }


        }

        datagramSocket.close();

    }

    /**
     * 把消息单播转发给所有已知客户端（走 12251 端口，与 ACK 相同路径）。
     * 不依赖UDP广播，避免在回环地址/受限网络下消息无法送达。
     */
    private static void broadcastToClients(Message msg) {
        for (String ip : clientIps) {
            if (!msg.sendToClient(ip)) {
                System.out.println("转发失败 -> " + ip);
            }
        }
    }

    /**
     * 持续广播服务器存在通告，方便客户端自动扫描发现。
     * 遍历本机所有 IPv4 接口，按各自的子网掩码广播到正确广播地址，
     * 从而覆盖 192.168 / 10 / 172 等任意子网，而不局限于首个网卡的 192.168.x.255。
     * 通告格式(JSON): {"ip":"...","version":"...","nickname":"..."}
     */
    private static void startDiscoveryBroadcast(final String nickname) {
        new Thread(() -> {
            try {
                DatagramSocket sock = new DatagramSocket();
                sock.setBroadcast(true);
                while (true) {
                    //在所有本地 IPv4 接口上，按各自的真实广播地址发送
                    try {
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                            NetworkInterface intf = en.nextElement();
                            if (intf.isLoopback() || !intf.isUp()) continue;
                            for (InterfaceAddress ia : intf.getInterfaceAddresses()) {
                                InetAddress addr = ia.getAddress();
                                InetAddress bc = broadcastOf(ia);
                                if (!(addr instanceof Inet4Address) || bc == null) continue;
                                //通告里带上该接口自身的 IP，客户端才能连回正确的子网地址
                                ServerAnnouncement ann = new ServerAnnouncement(addr.getHostAddress(), VERSION, nickname);
                                byte[] data = ann.toJson().getBytes(StandardCharsets.UTF_8);
                                try {
                                    sock.send(new DatagramPacket(data, data.length, bc, ServerAnnouncement.DISCOVERY_PORT));
                                } catch (Exception ignore) {
                                }
                            }
                        }
                    } catch (Exception ignore) {
                    }
                    //回环地址：同机客户端也能被发现
                    try {
                        ServerAnnouncement ann = new ServerAnnouncement("127.0.0.1", VERSION, nickname);
                        byte[] data = ann.toJson().getBytes(StandardCharsets.UTF_8);
                        sock.send(new DatagramPacket(data, data.length,
                                InetAddress.getByName("127.0.0.1"), ServerAnnouncement.DISCOVERY_PORT));
                    } catch (Exception ignore) {
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "ServerDiscovery").start();
    }

    static String defaultNickname() {
        try {
            String h = InetAddress.getLocalHost().getHostName();
            if (h != null && !h.trim().isEmpty()) return h;
        } catch (Exception ignore) {
        }
        return "Server";
    }

    //计算接口的真实广播地址：优先用系统给出的 getBroadcast()，
    //若其为 null（部分 WiFi/网卡配置会返回 null），则按地址+前缀长度推导。
    private static InetAddress broadcastOf(InterfaceAddress ia) {
        InetAddress bc = ia.getBroadcast();
        if (bc != null) return bc;
        if (!(ia.getAddress() instanceof Inet4Address)) return null;
        try {
            byte[] addr = ia.getAddress().getAddress();
            int a = ((addr[0] & 0xFF) << 24) | ((addr[1] & 0xFF) << 16)
                    | ((addr[2] & 0xFF) << 8) | (addr[3] & 0xFF);
            int prefix = ia.getNetworkPrefixLength();
            int mask = (prefix <= 0 || prefix >= 32)
                    ? (prefix == 0 ? 0 : 0xFFFFFFFF)
                    : (0xFFFFFFFF << (32 - prefix));
            int bcast = a | (~mask);
            byte[] b = { (byte) (bcast >>> 24), (byte) (bcast >>> 16), (byte) (bcast >>> 8), (byte) bcast };
            return InetAddress.getByAddress(b);
        } catch (Exception e) {
            return null;
        }
    }

    //这段代码来自https://stackoverflow.com/questions/17252018/getting-my-lan-ip-address-192-168-xxxx-ipv4，强转了两句的变量类型，适用于这里
    public static String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ipAddress = inetAddress.getHostAddress().toString();
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.print("get wrong LAN ip");
        }
        return null;
    }
}
