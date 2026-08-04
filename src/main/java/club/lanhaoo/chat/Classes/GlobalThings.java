/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:4:59 PM_3/1/2020
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat.Classes;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalThings {
    public String serverIP;
    //等待服务器确认
    public static Set<String> confirmIds =
            ConcurrentHashMap.newKeySet();


    //已经显示过的消息
    public static Set<String> receivedIds =
            ConcurrentHashMap.newKeySet();
}
