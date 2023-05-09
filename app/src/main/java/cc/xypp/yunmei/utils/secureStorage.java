package cc.xypp.yunmei.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.util.Map;

public class secureStorage {
    private final SharedPreferences ssp;
    private secureStorage(Context context) {
        SharedPreferences tssp;
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            tssp = EncryptedSharedPreferences.create(
                    "yunmei_secure",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            tssp = null;
            e.printStackTrace();
        }
        ssp = tssp;
    }
    public boolean setVal(Map<String,String> data){
        if(ssp==null)return false;
        SharedPreferences.Editor sedit = ssp.edit();
        data.forEach(sedit::putString);
        sedit.apply();
        return true;
    }
    public boolean setVal(String k,String v){
        if(ssp==null)return false;
        SharedPreferences.Editor sedit = ssp.edit();
        sedit.putString(k,v);
        sedit.apply();
        return true;
    }
    public String getVal(String k,String def){
        if(ssp==null)return def;
        return ssp.getString(k,def);
    }
}
