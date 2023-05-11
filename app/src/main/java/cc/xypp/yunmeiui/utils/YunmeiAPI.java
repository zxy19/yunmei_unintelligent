package cc.xypp.yunmeiui.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.xypp.yunmeiui.eneity.Lock;

public class YunmeiAPI {
    private final String usernameMD5;

    public static class Schools {
        public String schoolNo;
        public String schoolName;
        public String url;
        public String token;
    }
    public http sess;
    public String userId;
    public String schoolNo;
    public List<Schools> schools;
    public List<Lock> loginAndGetLock(){
        JSONArray lockResList;
        Map<String,String> lockForm = new HashMap<>();
        lockForm.put("schoolNo",schoolNo);
        lockForm.put("userId",userId);
        try {
            lockResList = new JSONArray(sess.post("/dormuser/getuserlock", lockForm));
        }catch (Exception e){
            throw new RuntimeException("门锁获取失败");
        }
        JSONObject lockRes;
        ArrayList<Lock> locks = new ArrayList<>();
        for (int i = 0; i < lockResList.length(); i++) {
            Lock currentLock = new Lock();
            try {
                lockRes=lockResList.getJSONObject(i);
                currentLock.label = String.format("%s-%s", lockRes.getString("buildName"), lockRes.getString("dormNo"));
                currentLock.D_SEC = lockRes.getString("lockSecret");
                currentLock.D_CHAR = lockRes.getString("lockCharacterUuid");
                currentLock.D_SERV = lockRes.getString("lockServiceUuid");
                currentLock.D_Mac = lockRes.getString("lockNo");
                currentLock.lockNo = lockRes.getString("lockNo");
                currentLock.schoolNo = schoolNo;
                currentLock.username = usernameMD5;
                locks.add(currentLock);
            }catch (Exception ignored){}
        }
        return locks;
    }

    public YunmeiAPI(String username,String password) throws RuntimeException{
        this(username,password,false);
    }
    public YunmeiAPI(String username,String password,boolean isMd5) throws RuntimeException{
        sess = new http("https://base.yunmeitech.com/");
        try {
            if(!isMd5)password=MD5Utils.stringToMD5(password);


            Map<String, String> loginFormData = new HashMap<>();
            loginFormData.put("userName", username);
            loginFormData.put("userPwd",password);
            JSONObject loginRes = new JSONObject(sess.post("/login", loginFormData));
            if (!loginRes.getBoolean("success")) {
                throw new RuntimeException(loginRes.getString("msg"));
            }

            JSONObject temp = loginRes.getJSONObject("o");
            sess.setToken(temp.getString("token"), (userId = temp.getString("userId")));

            //SCHOOL
            Map<String, String> schoolDat = new HashMap<>();
            schoolDat.put("userId", userId);
            JSONArray schoolList = new JSONArray(sess.post("/userschool/getbyuserid", schoolDat));

            schools=new ArrayList<>();

            for (int j = 0; j < schoolList.length(); j++) {
                JSONObject schoolRes = schoolList.getJSONObject(j);
                Schools school = new Schools();
                school.schoolNo=schoolRes.getString("schoolNo");
                school.schoolName=schoolRes.getJSONObject("school").getString("schoolName");
                school.url=schoolRes.getJSONObject("school").getString("serverUrl");
                school.token=schoolRes.getString("token");
                schools.add(school);
            }
            usernameMD5 = MD5Utils.stringToMD5(username);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("未知错误");
        }
    }
    public void setSchool(int index){
        Schools school = schools.get(index);
        sess.setBaseURL(school.url);
        sess.setToken(school.token,userId);
        schoolNo=school.schoolNo;
    }
}
