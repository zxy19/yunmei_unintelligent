package cc.xypp.yunmei;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cc.xypp.yunmei.utils.MD5Utils;
import cc.xypp.yunmei.utils.ToastUtil;
import cc.xypp.yunmei.utils.http;

public class loginActivity extends AppCompatActivity {
    private Context context;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_login);
        sp = getSharedPreferences("storage", MODE_PRIVATE);
        ((EditText) findViewById(R.id.ipt_phone)).setText(sp.getString("loginUsr",""));
        ((EditText) findViewById(R.id.ipt_pasw)).setText(sp.getString("loginPsw",""));
    }
    public void doLoginClick(View view){
        new Thread(login).start();
    }
    public void InfoClickE(View view){
        startActivity(new Intent(this, lockInfoActivity.class));
    }
    private void toast(String tip){
        runOnUiThread(()->{
            ToastUtil.show(this,tip);
        });

    }
    Runnable login = new Runnable() {
        @Override
        public void run() {
            //TODO 登录云梅智能
            http sess = new http("https://base.yunmeitech.com/");
            try {
                String userId;
                Map<String, String> allDat = new HashMap<>();

                Map<String, String> loginDat = new HashMap<>();
                loginDat.put("userName", ((EditText) findViewById(R.id.ipt_phone)).getText().toString());
                loginDat.put("userPwd", MD5Utils.stringToMD5(((EditText) findViewById(R.id.ipt_pasw)).getText().toString()));
                JSONObject loginRes = new JSONObject(sess.post("/login", loginDat));
                if (!loginRes.getBoolean("success")) {
                    toast(loginRes.getString("msg"));
                    return;
                }
                JSONObject temp = loginRes.getJSONObject("o");
                sess.setToken(temp.getString("token"), (userId = temp.getString("userId")));
                allDat.put("userId", userId);
                allDat.put("name", temp.getString("realName"));
                allDat.put("tel", temp.getString("userTel"));

                //SCHOOL
                Map<String, String> schoolDat = new HashMap<>();
                schoolDat.put("userId", userId);
                JSONObject schoolRes = new JSONArray(sess.post("/userschool/getbyuserid", schoolDat)).getJSONObject(0);
                if (schoolRes == null) {
                    toast("学校获取异常");
                    return;
                }
                sess.setBaseURL(schoolRes.getJSONObject("school").getString("serverUrl"));
                sess.setToken(schoolRes.getString("token"),userId);
                String schoolNo = schoolRes.getString("schoolNo");
                //LOCK
                Map<String, String> lockDat = new HashMap<>();
                schoolDat.put("schoolNo", schoolNo);
                JSONObject lockRes = new JSONArray(sess.post("/dormuser/getuserlock", schoolDat)).getJSONObject(0);
                if (lockRes == null) {
                    toast("门锁获取异常");
                    return;
                }
                allDat.put("area", lockRes.getString("areaName"));
                allDat.put("areaNo", lockRes.getString("areaNo"));
                allDat.put("build", lockRes.getString("buildName"));
                allDat.put("buildNo", lockRes.getString("buildNo"));
                allDat.put("school", lockRes.getString("schoolName"));
                allDat.put("schoolNo", lockRes.getString("schoolNo"));
                allDat.put("dorm", lockRes.getString("dormName"));
                allDat.put("dormNo", lockRes.getString("dormNo"));

                allDat.put("lockNo", lockRes.getString("lockNo"));
                allDat.put("lockSec", lockRes.getString("lockSecret"));
                allDat.put("LUUID", lockRes.getString("lockServiceUuid"));
                allDat.put("SUUID", lockRes.getString("lockCharacterUuid"));
                allDat.put("LMAC", lockRes.getString("lockNo"));
                allDat.put("loginUsr",loginDat.get("userName"));
                allDat.put("loginPsw", loginDat.get("userPwd"));


                SharedPreferences.Editor edit = sp.edit();

                allDat.forEach(edit::putString);

                edit.apply();
                toast("门锁添加完成");
                runOnUiThread(()->{
                    startActivity(new Intent(context, lockInfoActivity.class));
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                toast("啊哦，程序出错了。请稍后重试");
            }

        }
    };
}