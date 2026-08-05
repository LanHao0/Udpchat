package club.lanhaoo.chat.HttpFileShare;

import java.io.*;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

//       #======= 上传文件（POST，body 为原始文件字节）=======
        if (parms.get("upload") != null) {
            try {
                String dir = parms.get("filepath");
                String fname = parms.get("filename");
                if (dir == null || fname == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "missing param");
                }
                // 规范化文件名，防止路径穿越（如 "../../x.txt"）
                String safeName = new File(fname).getName();
                if (safeName.isEmpty()) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "bad filename");
                }
                File outFile = new File(dir, safeName);
                File parent = outFile.getParentFile();
                if (parent == null || !parent.isDirectory()) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "bad path");
                }
                // 按 Content-Length 精确读取，避免读到连接 EOF 一直阻塞导致 SocketTimeout
                long len = -1;
                try {
                    String cl = session.getHeaders().get("content-length");
                    if (cl != null) len = Long.parseLong(cl);
                } catch (Exception ignore) { }
                InputStream is = session.getInputStream();
                try (OutputStream os = new FileOutputStream(outFile)) {
                    byte[] buf = new byte[8192];
                    long total = 0;
                    int n;
                    while (len < 0 || total < len) {
                        int toRead = (len < 0) ? buf.length : (int) Math.min(buf.length, len - total);
                        n = is.read(buf, 0, toRead);
                        if (n < 0) break;
                        os.write(buf, 0, n);
                        total += n;
                    }
                }
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "OK");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
            }
        }

//       #======= 删除文件/文件夹 =======
        if (parms.get("delete") != null) {
            try {
                String p = parms.get("filepath");
                if (p == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "missing param");
                }
                File f = new File(p);
                if (!f.exists()) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found");
                }
                deleteRecursive(f);
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "OK");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
            }
        }

//       #======= 复制文件/文件夹到目标目录（粘贴）=======
        if (parms.get("copy") != null) {
            try {
                String from = parms.get("from");
                String to = parms.get("to");
                if (from == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "missing param");
                }
                File src = new File(from);
                if (!src.exists()) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "源文件不存在");
                }
                // 未指定目标（根目录）时给出明确提示，而不是静默失败
                if (to == null || to.isEmpty()) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "请先进入一个文件夹再粘贴");
                }
                File toFile = new File(to);
                File dstDir;
                if (toFile.isDirectory()) dstDir = toFile;          // 目标是文件夹
                else if (toFile.isFile()) dstDir = toFile.getParentFile(); // 目标是文件，则粘贴到其所在目录
                else return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "目标无效");
                if (dstDir == null || !dstDir.isDirectory()) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "目标文件夹无效");
                }
                // 目标已存在同名文件/文件夹时，自动加“(副本)”后缀，避免覆盖源文件
                File dst = new File(dstDir, src.getName());
                if (dst.exists()) {
                    String name = src.getName();
                    int dot = name.lastIndexOf('.');
                    String base = (dot > 0) ? name.substring(0, dot) : name;
                    String ext = (dot > 0) ? name.substring(dot) : "";
                    int i = 1;
                    do { dst = new File(dstDir, base + " (副本" + (i++) + ")" + ext); } while (dst.exists());
                }
                if (src.isDirectory()) copyRecursive(src, dst);
                else Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "OK");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
            }
        }

//       #======= 移动文件/文件夹到目标目录 =======
        if (parms.get("move") != null) {
            try {
                String from = parms.get("from");
                String to = parms.get("to");
                if (from == null || to == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "missing param");
                }
                File src = new File(from);
                File dstDir = new File(to);
                if (!src.exists()) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found");
                }
                if (!dstDir.isDirectory()) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "bad target");
                }
                File dst = new File(dstDir, new File(from).getName());
                if (src.isDirectory()) {
                    copyRecursive(src, dst);
                    deleteRecursive(src);
                } else {
                    Files.move(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "OK");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
            }
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
                        // 关键：关闭 keep-alive，避免浏览器复用同一个 socket 导致下载到错误的响应（如之前的目录 JSON）
                        response.addHeader("Connection", "close");
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
            Response jsonResp = newFixedLengthResponse(Response.Status.OK,"text/json",msg);
            // 关闭 keep-alive，避免该连接被用于后续的下载请求
            jsonResp.addHeader("Connection", "close");
            return jsonResp;
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

    // 递归删除文件/文件夹
    private void deleteRecursive(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) deleteRecursive(c);
            }
        }
        f.delete();
    }

    // 递归复制文件/文件夹
    private void copyRecursive(File s, File d) throws IOException {
        if (s.isDirectory()) {
            if (!d.exists()) d.mkdirs();
            File[] children = s.listFiles();
            if (children != null) {
                for (File c : children) copyRecursive(c, new File(d, c.getName()));
            }
        } else {
            if (d.getParentFile() != null) d.getParentFile().mkdirs();
            Files.copy(s.toPath(), d.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }
}