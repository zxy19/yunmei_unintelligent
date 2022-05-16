package cc.xypp.yunmei;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

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
    private SharedPreferences sp,ssp;
    private String stored;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_login);
        sp = getSharedPreferences("storage", MODE_PRIVATE);
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            ssp = EncryptedSharedPreferences.create(
                    "yunmei_secure",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((EditText) findViewById(R.id.ipt_phone)).setText(ssp.getString("loginUsr",""));
        ((EditText) findViewById(R.id.ipt_pasw)).setText(stored=ssp.getString("loginPsw",""));
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
                //Map<String, String> allDat = new HashMap<>();
                Map<String, String> sallDat = new HashMap<>();

                Map<String, String> loginDat = new HashMap<>();
                loginDat.put("userName", ((EditText) findViewById(R.id.ipt_phone)).getText().toString());
                String pwc=((EditText) findViewById(R.id.ipt_pasw)).getText().toString();
                if(!pwc.equals(stored))pwc=MD5Utils.stringToMD5(pwc);
                loginDat.put("userPwd",pwc);
                JSONObject loginRes = new JSONObject(sess.post("/login", loginDat));
                if (!loginRes.getBoolean("success")) {
                    toast(loginRes.getString("msg"));
                    return;
                }
                JSONObject temp = loginRes.getJSONObject("o");
                sess.setToken(temp.getString("token"), (userId = temp.getString("userId")));
                sallDat.put("userId", userId);
                sallDat.put("name", temp.getString("realName"));
                sallDat.put("tel", temp.getString("userTel"));

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
                sallDat.put("area", lockRes.getString("areaName"));
                sallDat.put("areaNo", lockRes.getString("areaNo"));
                sallDat.put("build", lockRes.getString("buildName"));
                sallDat.put("buildNo", lockRes.getString("buildNo"));
                sallDat.put("school", lockRes.getString("schoolName"));
                sallDat.put("schoolNo", lockRes.getString("schoolNo"));
                sallDat.put("dorm", lockRes.getString("dormName"));
                sallDat.put("dormNo", lockRes.getString("dormNo"));

                sallDat.put("lockNo", lockRes.getString("lockNo"));
                sallDat.put("lockSec", lockRes.getString("lockSecret"));
                sallDat.put("LUUID", lockRes.getString("lockServiceUuid"));
                sallDat.put("SUUID", lockRes.getString("lockCharacterUuid"));
                sallDat.put("LMAC", lockRes.getString("lockNo"));

                sallDat.put("loginUsr",loginDat.get("userName"));
                sallDat.put("loginPsw", loginDat.get("userPwd"));



                SharedPreferences.Editor sedit = ssp.edit();

                sallDat.forEach(sedit::putString);

                sedit.apply();
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