package cc.xypp.yunmeiui.eneity;

import androidx.annotation.NonNull;

import java.util.Base64;

import cc.xypp.yunmeiui.utils.MD5Utils;

public class User {
    static private String CURVER = "1.1";
    public String ver = "";
    public String username;
    public String usernameMD5;
    public String passwordMD5;

    public User() {
        username = usernameMD5 = passwordMD5 = "";
        ver = CURVER;
    }

    public User(String username, String password) {
        this();
        this.username = username;
        this.usernameMD5 = MD5Utils.stringToMD5(username);
        this.passwordMD5 = password;
    }

    public User(String data) {
        this();
        String[] us = data.split("\\|");
        if (us.length >= 3) {
            this.username = us[0];
            this.usernameMD5 = us[1];
            this.passwordMD5 = us[2];
        }
    }

    @NonNull
    @Override
    public String toString() {
        return username + "|" + usernameMD5 + "|" + passwordMD5 + "|" + ver;
    }
}
