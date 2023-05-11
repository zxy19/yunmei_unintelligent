package cc.xypp.yunmeiui.service;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.xypp.yunmeiui.eneity.Lock;
import cc.xypp.yunmeiui.eneity.User;
import cc.xypp.yunmeiui.utils.UserUtils;
import cc.xypp.yunmeiui.utils.YunmeiAPI;

public class CodeService {
    public static abstract class Callback {

        public void setpss(int pss, String tip) {
            setpss(pss, tip, false);
        }

        public abstract void setpss(int pss, String tip, boolean toast);
        public abstract void start();
        public abstract void end(String code);


    }
    private final Callback callback;
    Activity context;
    UserUtils userUtils;
    Lock currentLock;
    private SharedPreferences sp;


    public CodeService(Activity activity, Lock currentLock,Callback callback){
        context = activity;
        userUtils = new UserUtils(activity);
        this.currentLock = currentLock;
        sp = context.getSharedPreferences("storage", MODE_PRIVATE);
        this.callback = callback;
    }
    public void getCode(){

        User user = userUtils.getByNameMD5(currentLock.username);
        if(user==null){
            if(sp.getBoolean("alwaysCode",false)){
                List<User> userList = userUtils.getAll();
                if(userList.size()>0){
                    user = userList.get(0);
                }
            }
        }
        if(user == null){
            callback.setpss(0,"无法获取开门密码：无保存的账号",true);
            callback.end("");
        }else{
            try {
                callback.setpss(0,"登录",true);
                YunmeiAPI api = new YunmeiAPI(user.username, user.passwordMD5,true);
                for (int i = 0; i < api.schools.size(); i++) {
                    if(api.schools.get(i).schoolNo.equals(currentLock.schoolNo)){
                        api.setSchool(i);
                    }
                }
                callback.setpss(1,"获取",true);
                Map<String, String> data = new HashMap<>();
                data.put("lockNo",currentLock.lockNo);
                JSONObject jsonObject = new JSONObject(api.sess.post("/lockpassword/getlockpwdbylockno",data));
                callback.setpss(100,"获取成功",true);
                callback.end(jsonObject.getString("lockPwd"));
            }catch (RuntimeException e){
                callback.setpss(100,e.getMessage(),true);
                callback.end("");
            }catch (Exception e){
                callback.setpss(100,"获取开锁密码失败",true);
                callback.end("");
            }
        }
    }
}
