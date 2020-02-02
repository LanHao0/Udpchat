package club.lanhaoo.chat.HttpFileShare;

import com.google.gson.Gson;

import java.io.*;
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
            arrayList.add(new MyEveryFileProperties(file.getPath(),file.getPath(),false));
        }
        Gson gson=new Gson();
        return gson.toJson(arrayList);
    }


    public String listFilesForFolder(final File folder,Boolean onlyFolder) {

        ArrayList<MyEveryFileProperties> arrayList = new ArrayList<MyEveryFileProperties>();

        for (final File fileEntry : folder.listFiles()) {
            if (onlyFolder){
                if (!fileEntry.isFile()){
                    arrayList.add(new MyEveryFileProperties(fileEntry.getAbsolutePath(), fileEntry.getName(),fileEntry.isFile()));
                }
            }else{
                arrayList.add(new MyEveryFileProperties(fileEntry.getAbsolutePath(), fileEntry.getName(),fileEntry.isFile()));
            }

        }
        Gson gson = new Gson();
        return gson.toJson(arrayList);
    }

    public String getResString(String path){
        File file=new File(getClass().getClassLoader().getResource(path).getFile());
        String msg ="";
        try {
            msg=ReadHTML(getClass().getClassLoader().getResource(path).openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public File getResFile(String path){
        return new File(getClass().getClassLoader().getResource(path).getFile());
    }

    private String ReadHTML(InputStream stream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8));
        StringBuilder total = new StringBuilder();
        for (String line; (line = r.readLine()) != null; ) {
            total.append(line).append('\n');
        }
        r.close();
        return total.toString();
    }
}
