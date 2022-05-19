package cc.xypp.yunmei.utils;

import android.content.SharedPreferences;

public class OldVerConver {
    public static void dealInsecure(SharedPreferences sp,SharedPreferences ssp){
        SharedPreferences.Editor e=ssp.edit();
        e.putString("name", sp.getString("name",""));
        e.putString("userId", sp.getString("userId",""));
        e.putString("area", sp.getString("area",""));
        e.putString("areaNo", sp.getString("areaNo",""));
        e.putString("build", sp.getString("build",""));
        e.putString("buildNo", sp.getString("buildNo",""));
        e.putString("school", sp.getString("school",""));
        e.putString("schoolNo", sp.getString("schoolNo",""));
        e.putString("dorm", sp.getString("dorm",""));
        e.putString("dormNo", sp.getString("dormNo",""));
        e.putString("lockNo",sp.getString("lockNo",""));
        e.putString("lockSec",sp.getString("lockSec",""));
        e.putString("LUUID",sp.getString("LUUID",""));
        e.putString("SUUID",sp.getString("SUUID",""));
        e.putString("LMAC",sp.getString("LMAC",""));
        e.putString("loginUsr",sp.getString("loginUsr",""));
        e.putString("loginPsw",sp.getString("loginPsw",""));
        e.apply();

        SharedPreferences.Editor oe=sp.edit();
        oe.remove("lockNo");
        oe.remove("lockSec");
        oe.remove("LUUID");
        oe.remove("SUUID");
        oe.remove("LMAC");
        oe.remove("loginUsr");
        oe.remove("loginPsw");
        oe.remove("area");
        oe.remove("areaNo");
        oe.remove("build");
        oe.remove("buildNo");
        oe.remove("school");
        oe.remove("schoolNo");
        oe.remove("dorm");
        oe.remove("dormNo");
        oe.apply();
    }
}
