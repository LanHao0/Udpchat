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
            arrayList.add(new MyEveryFileProperties(file.getPath(),file.getPath(),false,"","folder",0));
        }
        Gson gson=new Gson();
        return gson.toJson(arrayList);
    }


    public String listFilesForFolder(final File folder,Boolean onlyFolder) {

        ArrayList<MyEveryFileProperties> arrayList = new ArrayList<MyEveryFileProperties>();

        for (final File fileEntry : folder.listFiles()) {
            String name = fileEntry.getName();
            String ext = getExt(name);
            String type = fileEntry.isFile() ? classify(name) : "folder";
            long size = fileEntry.isFile() ? fileEntry.length() : 0;
            if (onlyFolder){
                if (!fileEntry.isFile()){
                    arrayList.add(new MyEveryFileProperties(fileEntry.getAbsolutePath(), name, fileEntry.isFile(), ext, type, size));
                }
            }else{
                arrayList.add(new MyEveryFileProperties(fileEntry.getAbsolutePath(), name, fileEntry.isFile(), ext, type, size));
            }

        }
        Gson gson = new Gson();
        return gson.toJson(arrayList);
    }

    private String getExt(String name){
        int i = name.lastIndexOf('.');
        if (i < 0 || i >= name.length()-1) return "";
        return name.substring(i+1).toLowerCase();
    }

    private String classify(String name){
        String ext = getExt(name);
        switch (ext){
            case "jpg": case "jpeg": case "png": case "gif": case "bmp": case "webp": case "svg":
                return "image";
            case "mp4": case "webm": case "mov": case "mkv": case "avi": case "m4v": case "3gp":
                return "video";
            case "mp3": case "wav": case "flac": case "aac": case "m4a": case "ogg":
                return "audio";
            default:
                return "other";
        }
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
