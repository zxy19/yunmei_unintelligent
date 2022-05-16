package cc.xypp.yunmei;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import cc.xypp.yunmei.eneity.Lock;

public class addLockActivity extends AppCompatActivity {

    private SharedPreferences ssp;
    Set<String> locks;
    Lock toAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lock);
        Intent intent = getIntent();
        System.out.println(intent.getDataString());
        String Url=intent.getDataString();
        System.out.println(Url);
        Url=Url.substring(Url.indexOf("addlock/")+8);
        Url=new String(Base64.getDecoder().decode(Url));
        Url=Url.substring(Url.indexOf("account|")+8);
        String[] us=Url.split("\\|");
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            ssp = EncryptedSharedPreferences.create(
                    "yunmei_secure",
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            locks =ssp.getStringSet("locks",new HashSet<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
        toAdd=new Lock(getLockNameAuto(),us[0],us[1],us[2],us[3]);
        ((EditText)findViewById(R.id.lockLabel)).setText(toAdd.label);
    }

    private String getLockNameAuto() {
        for(int i = 0;; i++){
            boolean exi=false;
            for(String s : locks){
                if(s.startsWith("LockData#"+ i)){
                    exi=true;
                    break;
                }
            }
            if(!exi){
                return "LockData#"+ i;
            }
        }
    }

    public void delete(View view){
        toAdd=null;
        finish();
    }
    public void close(View view){
        finish();
    }
    @Override
    public void onStop() {
        super.onStop();
        if(toAdd!=null){
            toAdd.label= String.valueOf(((EditText)findViewById(R.id.lockLabel)).getText());
            locks.add(toAdd.toString());
            ssp.edit().putStringSet("locks",locks).apply();
        }
    }
}