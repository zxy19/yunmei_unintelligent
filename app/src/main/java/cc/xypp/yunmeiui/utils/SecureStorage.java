package cc.xypp.yunmeiui.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.util.Map;
import java.util.Set;

public class SecureStorage {
    private final SharedPreferences ssp;
    public SecureStorage(Context context) {
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
    public boolean setVal(String k,Set<String> v){
        if(ssp==null)return false;
        SharedPreferences.Editor sedit = ssp.edit();
        sedit.putStringSet(k,v);
        sedit.apply();
        return true;
    }
    public String getVal(String k,String def){
        if(ssp==null)return def;
        return ssp.getString(k,def);
    }
    public Set<String> getVal(String k, Set<String> def){
        if(ssp==null)return def;
        return ssp.getStringSet(k,def);
    }
}
