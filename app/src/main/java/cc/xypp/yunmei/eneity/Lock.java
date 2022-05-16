package cc.xypp.yunmei.eneity;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Lock implements Serializable {
    public String label;
    public String D_Mac;
    public String D_SERV;
    public String D_LOCK;
    public String D_SEC;

    public Lock(String label, String d_mac, String d_serv, String d_lock, String d_sec) {
        this.label=label;
        this.D_Mac=d_mac;
        this.D_SERV=d_serv;
        this.D_LOCK=d_lock;
        this.D_SEC=d_sec;
    }

    public Lock() {
        label="未添加门锁";
    }
    @NonNull
    public String toString(){
        return new StringBuilder().append(label).append("|").append(D_Mac).append("|").append(D_SERV).append("|").append(D_LOCK).append("|").append(D_SEC).toString();
    }
}
