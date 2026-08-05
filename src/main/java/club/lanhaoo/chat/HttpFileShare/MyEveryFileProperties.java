/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:9:42 AM_10/30/2019
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat.HttpFileShare;

public class MyEveryFileProperties {
    private String filepath;
    private String filename;
    private Boolean isFile;
    private String ext;
    private String type;
    private long size;

    public MyEveryFileProperties(String filepath, String filename, Boolean isFile, String ext, String type, long size) {
        this.filepath = filepath;
        this.filename = filename;
        this.isFile = isFile;
        this.ext = ext;
        this.type = type;
        this.size = size;
    }

}
