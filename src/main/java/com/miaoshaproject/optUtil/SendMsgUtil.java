package com.miaoshaproject.optUtil;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SendMsgUtil {

    public synchronized static String SendMsg( String telPhone,String content) throws IOException {
        StringBuffer sb = new StringBuffer("http://http.yunsms.cn/tx/?");
        sb.append("uid=150241");
        sb.append("&pwd=f93a733bc9ad035cde8c051ea56cdd92");
        sb.append("&mobile=" + telPhone);
        sb.append("&content=" + URLEncoder.encode("【测试】您的验证码为："+content,"gbk"));
       System.out.println(sb.toString());
        URL url = new URL(sb.toString());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        String inputline = in.readLine();

        System.out.println("发送成功:" + inputline);

        return inputline;
    }

}
