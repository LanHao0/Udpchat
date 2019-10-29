package club.lanhaoo.chat.HttpFileShare;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyFile {
    public String getRoots(){
        File[] roots=File.listRoots();
        String string_return="";
        for (File file : roots) {
            System.out.println(file.getPath());
            string_return+="<a href='./?filepath="+file.getPath()+"'>"+file.getPath()+"</a><br>";
        }
        return string_return;
    }


    public String listFilesForFolder(final File folder) {

            String return_String = "";
            if (folder.getParent() != null) {
                return_String += "<a href='./?filepath=" + folder.getParent() + "'>./</a><br>";
            } else {
                return_String += "<a href='./'>./</a><br>";
            }

            for (final File fileEntry : folder.listFiles()) {

                    System.out.println(fileEntry.getName());
                    return_String += "<a href='./?filepath=" + fileEntry.getAbsolutePath() + "'>" + fileEntry.getName() + "</a><br>";


            }

            return return_String;


    }

    public String getResString(String path){
        File file=new File(getClass().getClassLoader().getResource(path).getFile());
        String msg ="";
        try {
            msg=ReadHTML(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }
    static String ReadHTML(String path) throws IOException {
        byte[] bytes= Files.readAllBytes(Paths.get(path));
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
