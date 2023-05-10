package cc.xypp.yunmeiui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.xypp.yunmeiui.eneity.Lock;
import cc.xypp.yunmeiui.utils.AlertUtils;
import cc.xypp.yunmeiui.utils.MD5Utils;
import cc.xypp.yunmeiui.utils.SecureStorage;
import cc.xypp.yunmeiui.utils.ToastUtil;
import cc.xypp.yunmeiui.utils.YunmeiAPI;

public class loginActivity extends AppCompatActivity {
    private Activity context;
    private String stored;
    private SecureStorage secureStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_login);

        secureStorage = new SecureStorage(context);

        ((EditText) findViewById(R.id.ipt_phone)).setText(secureStorage.getVal("loginUsr", ""));
        ((EditText) findViewById(R.id.ipt_pasw)).setText(stored = secureStorage.getVal("loginPsw", ""));
    }

    public void doLoginClick(View view) {
        new Thread(login).start();
    }

    public void InfoClickE(View view) {
        startActivity(new Intent(this, lockInfoActivity.class));
    }

    private void toast(String tip) {
        runOnUiThread(() -> {
            ToastUtil.show(this, tip);
        });

    }

    Runnable login = new Runnable() {
        //保存当前运行的账号和学校
        private boolean saveCurrent = false;
        private String save_schoolNo = null;
        private String save_LockNo = null;
        private String userName = null;
        private String pwc = null;

        @Override
        public void run() {
            try {
                userName = ((EditText) findViewById(R.id.ipt_phone)).getText().toString();
                pwc = ((EditText) findViewById(R.id.ipt_pasw)).getText().toString();
                saveCurrent = ((CheckBox) findViewById(R.id.save_primary)).isChecked();
                if (!pwc.equals(stored)) {
                    pwc = MD5Utils.stringToMD5(pwc);
                }

                YunmeiAPI api = new YunmeiAPI(userName, pwc, true);
                if (api.schools.size() == 0) {
                    toast("账号不属于任何一所学校，请检查后重新添加");
                    return;
                }else if(api.schools.size()==1){
                    if(saveCurrent) {
                        save_schoolNo = api.schools.get(0).schoolNo;
                    }
                    api.setSchool(0);
                    afterSchoolSelect(api);
                } else {
                    List<String> schools = new ArrayList<>();
                    api.schools.forEach(schools1 -> schools.add(schools1.schoolName));
                    AlertUtils.showList(context, "选择要添加门锁的学校", schools, new AlertUtils.callbacker() {
                        @Override
                        public void select(int id) {
                            api.setSchool(id);
                            if (saveCurrent) {
                                save_schoolNo = api.schools.get(id).schoolNo;
                            }

                            new Thread(() -> afterSchoolSelect(api)).start();
                        }

                        @Override
                        public void cancel() {
                            toast("取消");
                        }
                    });
                }
            } catch (RuntimeException e) {
                toast(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                toast("发生了未知错误，请尝试重新提交");
            }

        }

        private void afterSchoolSelect(YunmeiAPI api) {
            try {
                List<Lock> locks = api.loginAndGetLock();
                if (locks.size() == 0) {
                    toast("当前账号不存在任何门锁");
                } else if (locks.size() == 1) {
                    addLock(locks.get(0));
                    if (saveCurrent) {
                        save_LockNo = locks.get(0).D_Mac;
                    }
                } else {
                    List<String> lockNames = new ArrayList<>();
                    locks.forEach(lock -> lockNames.add(lock.label));
                    AlertUtils.showList(context, "选择要添加的门锁", lockNames, new AlertUtils.callbacker() {
                        @Override
                        public void select(int id) {
                            if (saveCurrent) {
                                save_LockNo = locks.get(id).D_Mac;
                            }
                            addLock(locks.get(id));
                        }

                        @Override
                        public void cancel() {
                            toast("取消");
                        }
                    });
                }
            } catch (RuntimeException e) {
                toast(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                toast("发生了未知错误，请尝试重新提交");
            }
        }

        private void addLock(Lock lock) {
            if (saveCurrent) {
                Map<String, String> sallDat = new HashMap<>();
                sallDat.put("loginUsr", userName);
                sallDat.put("loginPsw", pwc);
                sallDat.put("schoolNo", save_schoolNo);
                sallDat.put("lockNo", save_LockNo);
                secureStorage.setVal(sallDat);
            }
            toast("门锁添加完成");
            Intent i = new Intent(context, addLockActivity.class);
            i.setData(Uri.parse(lock.toString()));
            startActivity(i);
            finish();
        }
    };
}