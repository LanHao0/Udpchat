/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:11:14 AM_2/12/2020
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat.Classes.UI;

import org.apache.commons.io.FilenameUtils;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FilenameFilter;


public class ImageFilter extends FileFilter {

    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = FilenameUtils.getExtension(f.getAbsolutePath());
        if (extension != null) {
            if (extension.equals("gif") ||
                    extension.equals("jpeg") ||
                    extension.equals("jpg") ||
                    extension.equals("png")) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    //description of filter
    public String getDescription() {
        return "Only Images";
    }
}
