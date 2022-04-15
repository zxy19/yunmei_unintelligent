package cc.xypp.yunmei.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class http extends HttpClient4{
    String baseURL="";
    String token;
    String userId;
    public http(String baseURL){
        this.baseURL=baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public void setToken(String token,String userId) {
        this.token = token;
        this.userId=userId;
    }
    public String get(String URL){
        return doGet(durl(URL),getUHeader());
    }
    public String post(String URL, Map<String,String> param){
        return doPost(durl(URL),param,getUHeader());
    }
    private Map<String,String> getUHeader(){
        Map<String,String> headers = new HashMap<>();
        if(token!=null)headers.put("token_data",token);
        if(userId!=null){
            headers.put("token_userId",userId);
            headers.put("tokenUserId",userId);
        }
        return headers;
    }
    private String durl(String url){
        if(!baseURL.endsWith("/"))baseURL=baseURL+"/";
        if(url.startsWith("/")){
            return baseURL+url.substring(1);
        }else return baseURL+url;
    }
}
