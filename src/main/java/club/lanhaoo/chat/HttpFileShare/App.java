package club.lanhaoo.chat.HttpFileShare;

import java.io.*;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.sun.jndi.toolkit.url.UrlUtil;
import fi.iki.elonen.NanoHTTPD;
// NOTE: If you're using NanoHTTPD >= 3.0.0 the namespace is different,
//       instead of the above import use the following:
// import org.nanohttpd.NanoHTTPD;

public class App extends NanoHTTPD {

    public App() throws IOException {
        super(8089);
        //start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

    }

    public static void main(String[] args) {
        try {
            App app=new App();
            app.start(NanoHTTPD.SOCKET_READ_TIMEOUT,false);
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    @Override
    public Response serve(IHTTPSession session){
        Map<String, String> parms = session.getParms();
        String msg = "";

//       #======= get css & js & json_data
        if (parms.get("getcss")!=null){
            msg=new MyFile().getResString("bootstrap.min.css");
            return newFixedLengthResponse(Response.Status.OK,"text/css",msg);
        }
        if (parms.get("getjq")!=null){
            msg=new MyFile().getResString("jquary34.js");
            return newFixedLengthResponse(Response.Status.OK,"text/javascript",msg);
        }
        if (parms.get("getcssjs")!=null){
            msg=new MyFile().getResString("bootstrap.min.js");
            return newFixedLengthResponse(Response.Status.OK,"text/javascript",msg);
        }

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
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }catch (IOException e){ }
                    response.addHeader("Content-Disposition:","attachment; filename=\""+  URLEncoder.encode(file.getName())+"\"");

                    return response;
                }
                msg=myFile.listFilesForFolder(new File(parms.get("filepath")));

            }
            return newFixedLengthResponse(Response.Status.OK,"text/json",msg);
        }
//      #===========


        msg=new MyFile().getResString("index.html");
        return newFixedLengthResponse(msg);
    }

}