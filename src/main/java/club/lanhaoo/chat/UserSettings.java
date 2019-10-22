/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:9:09 PM_10/20/2019
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat;

public class UserSettings {
    private String UserName;
    private boolean HidemyIp;

    public void setServerIp(String serverIp) {
        ServerIp = serverIp;
    }

    public String getServerIp() {
        return ServerIp;
    }

    private String ServerIp;

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

}
