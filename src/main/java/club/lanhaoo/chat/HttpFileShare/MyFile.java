package club.lanhaoo.chat.HttpFileShare;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MyFile {
    public String getRoots(){
        File[] roots=File.listRoots();
        String string_return="";
        ArrayList<MyEveryFileProperties> arrayList=new ArrayList<MyEveryFileProperties>();

        for (File file : roots) {
            arrayList.add(new MyEveryFileProperties(file.getPath(),file.getPath()));
        }
        Gson gson=new Gson();


        return gson.toJson(arrayList);
    }


    public String listFilesForFolder(final File folder) {

        String return_String = "";
        ArrayList<MyEveryFileProperties> arrayList = new ArrayList<MyEveryFileProperties>();

//        if (folder.getParent() != null) {
//            arrayList.add(new MyEveryFileProperties(folder.getParent(),"./"));
//        } else {
//            arrayList.add(new MyEveryFileProperties("./","./"));
//
//        }

        for (final File fileEntry : folder.listFiles()) {
            arrayList.add(new MyEveryFileProperties(fileEntry.getAbsolutePath(), fileEntry.getName()));
        }
        Gson gson = new Gson();

        return gson.toJson(arrayList);


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
