package cc.xypp.yunmeiui.function;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;

import cc.xypp.yunmeiui.eneity.Lock;
import cc.xypp.yunmeiui.eneity.User;
import cc.xypp.yunmeiui.utils.UserUtils;
import cc.xypp.yunmeiui.utils.YunmeiAPI;

public class RecordService {
    public static String defaultJSON = "";

    public static abstract class Callback {

        public abstract void result(String result, boolean toast);
    }

    private final RecordService.Callback callback;
    Activity context;
    UserUtils userUtils;
    Lock currentLock;
    private SharedPreferences sp;


    public RecordService(Activity activity, Lock currentLock, RecordService.Callback callback) {
        context = activity;
        userUtils = new UserUtils(activity);
        this.currentLock = currentLock;
        sp = context.getSharedPreferences("storage", MODE_PRIVATE);
        this.callback = callback;
    }

    public void record(int battery) {
        User user = userUtils.getByNameMD5(currentLock.username);
        if (user == null) {
            callback.result("当前账号未保存密码，取消上报", false);
            return;
        }
        try {
            YunmeiAPI api = new YunmeiAPI(user.username, user.passwordMD5, true);
            for (int i = 0; i < api.schools.size(); i++) {
                if (api.schools.get(i).schoolNo.equals(currentLock.schoolNo)) {
                    api.setSchool(i);
                }
            }

        } catch (RuntimeException e) {
            callback.result(e.getMessage(), true);
        } catch (Exception e) {
            callback.result("未知错误", true);
        }
    }

    public void logCollect(String i){
        //TODO
    }
}
