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

import com.sun.jndi.toolkit.url.UrlUtil;
import fi.iki.elonen.NanoHTTPD;
import sun.security.provider.MD5;
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
            msg=new MyFile().getResString(parms.get("getAssets"));
//            String mime= URLConnection.guessContentTypeFromName(new MyFile().getResFile(parms.get("getAssets")).getName());
            //todo mime 有bug先一律返回css
            return newFixedLengthResponse(Response.Status.OK,"text/css",msg);
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
                        response = newFixedLengthResponse(Response.Status.OK,MIME_TYPE,fis,fis.getChannel().size());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    response.addHeader("Content-Disposition:","attachment; filename=\""+  URLEncoder.encode(file.getName())+"\"");

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
}