package cc.xypp.yunmeiui.utils;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient4 {
    public static String doGet(String url,Map<String,String> header) {
        URL urlObj = null;
        StringBuilder result = new StringBuilder("");
        try {
            urlObj = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(30000);
            connection.setRequestProperty("x-requested-with", "XMLHttpRequest");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            header.forEach(connection::setRequestProperty);
            connection.setDoInput(true);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    public static String doPost(String url, Map<String, String> paramMap,Map<String,String> header) {
        URL urlObj = null;
        StringBuilder result = new StringBuilder("");
        try {
            urlObj = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(30000);
            connection.setRequestProperty("x-requested-with", "XMLHttpRequest");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            header.forEach(connection::setRequestProperty);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            StringBuilder param = new StringBuilder();
            paramMap.forEach((k,v)->{
                param.append(k).append("=").append(v).append("&");
            });
            out.print(param.toString());
            out.flush();
            out.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}