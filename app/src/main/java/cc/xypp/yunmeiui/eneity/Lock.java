package cc.xypp.yunmeiui.eneity;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Base64;

public class Lock implements Serializable {
    private static final String DATA_VER = "1.2";
    public String label;
    public String D_Mac;
    public String D_CHAR;
    public String D_SERV;
    public String D_SEC;

    public String username;
    public String schoolNo;
    public String lockNo;

    public Lock(){
        this.label = "未添加门锁";
        this.D_Mac = "";
        this.D_CHAR = "";
        this.D_SERV = "";
        this.D_SEC = "";
        this.schoolNo="";
        this.lockNo="";
        this.username="";
    }
    public Lock(String label, String d_mac, String d_serv, String d_lock, String d_sec) {
        this.label = label;
        this.D_Mac = d_mac;
        this.D_CHAR = d_serv;
        this.D_SERV = d_lock;
        this.D_SEC = d_sec;
    }
    public Lock(String label, String d_mac, String d_serv, String d_lock, String d_sec, String _username, String _schoolNo, String _lockNo) {
        this(label, d_mac, d_serv, d_lock, d_sec);
        this.schoolNo=_schoolNo;
        this.lockNo=_lockNo;
        this.username=_username;
    }
    public Lock(String Url) {
        this();
        if (Url.contains("addlock/")) {
            Url = Url.substring(Url.indexOf("addlock/") + 8);
            Url = new String(Base64.getDecoder().decode(Url));
        }
        if (Url.contains("lock_id/")) {
            Url = Url.substring(Url.indexOf("lock_id/") + 8);
            Url = new String(Base64.getDecoder().decode(Url));
        }
        if (Url.contains("lock_info/")) {
            Url = Url.substring(Url.indexOf("lock_info/") + 10);
            Url = new String(Base64.getDecoder().decode(Url));
        }
        if (Url.contains("lockInfo/")) {
            Url = Url.substring(Url.indexOf("lockInfo/") + 9);
            Url = new String(Base64.getDecoder().decode(Url));
        }
        String[] us = Url.split("\\|");
        if (us.length == 4) {
            String[] us_cp = new String[5];
            us_cp[0] = "account";
            System.arraycopy(us, 0, us_cp, 1, 4);
            us = us_cp;
        }
        if(us.length >= 5) {
            this.label = us[0];
            this.D_Mac = us[1];
            this.D_CHAR = us[2];
            this.D_SERV = us[3];
            this.D_SEC = us[4];
        }
        if(us.length >= 8){
            this.username = us[5];
            this.schoolNo = us[6];
            this.lockNo = us[7];
        }
    }
    public void removeSec(){
        D_SEC="";
        username="";
        lockNo="";
        schoolNo="";
    }

    @NonNull
    public String toString() {
        String body = label + "|" + D_Mac + "|" + D_CHAR + "|" + D_SERV + "|" + D_SEC+"|"+ username+"|"+ schoolNo+"|"+ lockNo+"|"+DATA_VER;
        if(D_SEC.equals("")){
            return "yunmeiui://lock_id/"+Base64.getEncoder().encodeToString(body.getBytes());
        }
        return "yunmeiui://lock_info/"+Base64.getEncoder().encodeToString(body.getBytes());
    }

}
