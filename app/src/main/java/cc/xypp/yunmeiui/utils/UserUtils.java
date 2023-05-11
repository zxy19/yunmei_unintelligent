package cc.xypp.yunmeiui.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import cc.xypp.yunmeiui.eneity.Lock;
import cc.xypp.yunmeiui.eneity.User;

public class UserUtils {
    SecureStorage ssp;
    public UserUtils(Context context){
        ssp = new SecureStorage(context);
    }
    public List<User> getAll() {
        Set<String> userSet = ssp.getVal("users", new HashSet<>());
        List<User> users = new ArrayList<>();
        userSet.forEach(v -> {
            users.add(new User(v));
        });
        return users;
    }
    public User getByName(String username){
        List<User> all = getAll();
        for (User user : all) {
            if(user.username.equals(username))return user;
        }
        return null;
    }
    public User getByNameMD5(String usernameMD5){
        List<User> all = getAll();
        for (User user : all) {
            if(user.usernameMD5.equals(usernameMD5))return user;
        }
        return null;
    }
    public void add(User user){
        List<User> all = getAll();
        Set<String> userSet = new HashSet<>();
        userSet.add(user.toString());
        for (User user1 : all) {
            if(!Objects.equals(user1.usernameMD5, user.usernameMD5)){
                userSet.add(user.toString());
            }
        }
        ssp.setVal("users",userSet);
    }
    public void remove(User user){
        List<User> all = getAll();
        Set<String> userSet = new HashSet<>();
        for (User user1 : all) {
            if(!Objects.equals(user1.usernameMD5, user.usernameMD5)){
                userSet.add(user.toString());
            }
        }
        ssp.setVal("users",userSet);
    }
}
