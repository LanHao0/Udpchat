package club.lanhaoo.chat.HttpFileShare;

import java.io.*;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


import fi.iki.elonen.NanoHTTPD;

// NOTE: If you're using NanoHTTPD >= 3.0.0 the namespace is different,
//       instead of the above import use the following:
// import org.nanohttpd.NanoHTTPD;

public class App extends NanoHTTPD {

    private String webpassword;

    public String getWebpassword() {
        return webpassword;
    }

    public void setWebpassword(String webpassword) {
        this.webpassword = webpassword;


    }

    public App() throws IOException {
        super(8089);
        //start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

    }

//    DEBUG
//    public static void main(String[] args) {
//        try {
//            App app=new App();
//            app.start(NanoHTTPD.SOCKET_READ_TIMEOUT,false);
//        } catch (IOException ioe) {
//            System.err.println("Couldn't start server:\n" + ioe);
//        }
//    }

    @Override
    public Response serve(IHTTPSession session){
        Map<String, String> parms = session.getParms();
        String msg = "";
        if (parms.get("getAssets")!=null){
            String asset=parms.get("getAssets");
            msg=new MyFile().getResString(asset);
            return newFixedLengthResponse(Response.Status.OK, mimeFor(asset), msg);
        }

        if (parms.get("checkpass")!=null) {
            if (!parms.get("checkpass").equals(webpassword)) {
                return newFixedLengthResponse("Wrong password");
            }
        }else {

            return newFixedLengthResponse(new MyFile().getResString("needpassword.html"));
        }
//       #======= get css & js & json_data

        if (parms.get("getjson")!=null){

            MyFile myFile=new MyFile();
            if (parms.get("filepath") == null) {
                msg=myFile.getRoots();
            } else {

                File file=new File(parms.get("filepath"));
                if (file.isFile()){
                    String MIME_TYPE="";
                    FileInputStream fis = null;
                    Response response= null;
                    try {
                        fis = new FileInputStream(file);
                        MIME_TYPE= URLConnection.guessContentTypeFromName(file.getName());
                        if (MIME_TYPE==null) MIME_TYPE = mimeFor(file.getName());
                        response = newFixedLengthResponse(Response.Status.OK,MIME_TYPE,fis,fis.getChannel().size());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 预览模式：浏览器内联显示（图片/视频），不强制下载
                    if (parms.get("preview")!=null){
                        response.addHeader("Content-Disposition","inline; filename=\""+  URLEncoder.encode(file.getName())+"\"");
                    }else{
                        response.addHeader("Content-Disposition","attachment; filename=\""+  URLEncoder.encode(file.getName())+"\"");
                    }

                    return response;
                }
                boolean getOnlyFolderJSON=false;
                if (parms.get("getjson_onlyfolder")!=null){
                    getOnlyFolderJSON=true;
                }

                msg=myFile.listFilesForFolder(new File(parms.get("filepath")),getOnlyFolderJSON);

            }
            return newFixedLengthResponse(Response.Status.OK,"text/json",msg);
        }
//      #===========


        msg=new MyFile().getResString("index.html");
        msg=msg.replace("replace_pass",webpassword);
        return newFixedLengthResponse(msg);
    }

    private String mimeFor(String name){
        int i = name.lastIndexOf('.');
        String ext = (i >= 0) ? name.substring(i+1).toLowerCase() : "";
        switch (ext){
            case "css": return "text/css";
            case "js": return "application/javascript";
            case "html": case "htm": return "text/html";
            case "json": return "application/json";
            case "png": return "image/png";
            case "jpg": case "jpeg": return "image/jpeg";
            case "gif": return "image/gif";
            case "webp": return "image/webp";
            case "svg": return "image/svg+xml";
            case "mp4": return "video/mp4";
            case "webm": return "video/webm";
            case "ogg": return "video/ogg";
            case "mp3": return "audio/mpeg";
            case "wav": return "audio/wav";
            default: return "application/octet-stream";
        }
    }
}