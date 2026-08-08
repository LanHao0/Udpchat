/**
 * @Author: LanHao
 * @Website:https://lanhaoo.club/
 * @Created_Date:8:49 PM_1/1/2020
 * @Magic_Power_Of_Code!
 */

package club.lanhaoo.chat;

import club.lanhaoo.chat.HttpFileShare.MyFile;
import com.google.gson.*;
import com.intellij.uiDesigner.core.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;


public class About {

    public About() {
        initComponents();
    }

    public void openWindow() {
        JFrame frame = new JFrame("UDPChat");
        final About about=new About();
        frame.setContentPane(about.Jpanel);
        about.softwareInfoButton.setText(I18n.get("about.software"));
        about.openSourceLicensesCreditButton.setText(I18n.get("about.credits"));
        about.checkUpdateButton.setText(I18n.get("about.checkUpdate"));
        about.githubButton.setText("GITHUB 开源");
        about.githubButton.addActionListener(e -> openGithub());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置居中
        Point point = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        frame.setBounds(point.x - 600 / 2, point.y - 400 / 2, 600, 400);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        about.openSourceLicensesCreditButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                String credit=new MyFile().getResString("openSourceLicense.txt");
                about.textArea1.setText(credit);
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        about.softwareInfoButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                String infos="UDPChat 局域网 \n" +
                        "\n开发者列表:"+
                        "\n兰兰想"+
                        "\n\n官网:\nhttps://www.lanhaoo.club/myApps/#/app/UDPChat%20%E5%B1%80%E5%9F%9F%E7%BD%91";
                about.textArea1.setText(infos);
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        about.checkUpdateButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    update(about);
                } catch (URISyntaxException | IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }


    private void openGithub() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/LanHao0/Udpchat"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void update(About about) throws URISyntaxException, IOException {

        HttpGet httpGet;

        URI uri_update_api=new URIBuilder("https://www.lanhaoo.club/lab/drawing/api/pc_udpchat_version.php")
                    .setParameter("now_version","1")
                    .build();
        httpGet = new HttpGet(uri_update_api);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;


        try {

            httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode()==200){
                String content= EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

                Gson gson = new Gson();
                JsonArray jsonArray=gson.fromJson(content,JsonArray.class);
                System.out.println(jsonArray.size());
                if(jsonArray.size()<=1){
                    //No updates
                    about.textArea1.setText("Your software is up to date!");

                }else{
                    //has Updates
                    //循环检查level中是否有大于3的,第一个是当前版本,不用管

                    boolean hasLevel3=false;
                    boolean hasLevel2=false;
                    boolean recoomend_update=false;

                    JsonObject jsonObject =jsonArray.get(jsonArray.size()-1).getAsJsonObject();
                    String update_detail=jsonObject.get("update_content").getAsString();
                    String url=jsonObject.get("url").getAsString();
                    String raw_time=jsonObject.get("time").getAsString();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time=sdf.format(Long.parseLong(raw_time+"000"));

                    for (int i=1;i<jsonArray.size();i++){


                        String Slevel=jsonArray.get(i).getAsJsonObject().get("level").getAsString();
                        int level=Integer.parseInt(Slevel);

                        if (level>=3){
                            hasLevel3=true;
                            recoomend_update=true;
                            break;
                        }
                        if (level==2){
                            hasLevel2=true;
                            recoomend_update=true;
                        }
                    }

                    if (recoomend_update){
                        if (hasLevel2){
                            //level2 updates yellow_recommend
                            about.textArea1.setText("Recommend Update");

                        }
                        if(hasLevel3){
                            //level3 updates red_Have to
                            about.textArea1.setText("Really need to Update!!!");
                        }
                    }else{
                        //level1 updates blue_you can decide
                        about.textArea1.setText("Regular Update, Don't have to");
                    }
                    about.textArea1.append("\n" +
                            "Update detail:\n"+update_detail+"\n" +
                            "Download url:\n"+url+"\n" +
                            "Time: "+time);

                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (httpResponse!=null){
                httpResponse.close();
            }
            httpClient.close();
        }


    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        Jpanel = new JPanel();
        Spacer vSpacer1 = new Spacer();
        checkUpdateButton = new JButton();
        openSourceLicensesCreditButton = new JButton();
        JScrollPane scrollPane1 = new JScrollPane();
        textArea1 = new JTextArea();
        softwareInfoButton = new JButton();
        githubButton = new JButton();

        //======== Jpanel ========
        {
            Jpanel.setPreferredSize(new Dimension(600, 400));
            Jpanel.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
            Jpanel.add(vSpacer1, new GridConstraints(4, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));

            //---- checkUpdateButton ----
            checkUpdateButton.setText("CheckUpdate");
            Jpanel.add(checkUpdateButton, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

            //---- githubButton ----
            githubButton.setText("GITHUB 开源");
            Jpanel.add(githubButton, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

            //---- openSourceLicensesCreditButton ----
            openSourceLicensesCreditButton.setText("Open Source licenses / Credit");
            Jpanel.add(openSourceLicensesCreditButton, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

            //======== scrollPane1 ========
            {
                scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                //---- textArea1 ----
                textArea1.setEditable(false);
                textArea1.setLineWrap(true);
                textArea1.setText("");
                textArea1.setWrapStyleWord(true);
                scrollPane1.setViewportView(textArea1);
            }
            Jpanel.add(scrollPane1, new GridConstraints(3, 0, 2, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));

            //---- softwareInfoButton ----
            softwareInfoButton.setText("Software Info");
            Jpanel.add(softwareInfoButton, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel Jpanel;
    private JButton checkUpdateButton;
    private JButton openSourceLicensesCreditButton;
    private JTextArea textArea1;
    private JButton softwareInfoButton;
    private JButton githubButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
