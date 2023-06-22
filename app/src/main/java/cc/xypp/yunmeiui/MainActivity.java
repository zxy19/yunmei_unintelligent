package cc.xypp.yunmeiui;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.clj.fastble.BleManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cc.xypp.yunmeiui.eneity.Lock;
import cc.xypp.yunmeiui.eneity.User;
import cc.xypp.yunmeiui.service.CodeService;
import cc.xypp.yunmeiui.service.SignService;
import cc.xypp.yunmeiui.service.UnlockService;
import cc.xypp.yunmeiui.utils.LockManageUtil;
import cc.xypp.yunmeiui.utils.SecureStorage;
import cc.xypp.yunmeiui.utils.ToastUtil;
import cc.xypp.yunmeiui.utils.UserUtils;
import cc.xypp.yunmeiui.wigets.CircleProgress;

public class MainActivity extends AppCompatActivity {
    private List<Lock> locks = new ArrayList<>();
    Button btn_unlock;
    CircleProgress circleProgress;
    private Activity context;
    private SharedPreferences sp;
    SecureStorage secureStorage;
    LockManageUtil lockManageUtil;
    UnlockService unlockService;
    SignService signService;
    private Boolean config_quickConnect;
    private Lock currentLock;
    private Lock tmpLock = null;
    private boolean exitOnce = false;
    private boolean config_autoCode;
    private CodeService codeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BleManager.getInstance().init(getApplication());
        context = this;
        if (!BleManager.getInstance().isSupportBle()) {
            toast("设备不支持蓝牙！");
            finish();
        }
        secureStorage = new SecureStorage(this);
        lockManageUtil = new LockManageUtil(this);
        ((Button)findViewById(R.id.btn_getcode)).setText("获取开门密码\n[点击获取]");
        sp = getSharedPreferences("storage", MODE_PRIVATE);

        boolean config_autoConnect = sp.getBoolean("autoCon", false);
        config_quickConnect = sp.getBoolean("quickCon", true);
        config_autoCode = sp.getBoolean("autoCode", false);

        if(sp.getBoolean("hideSign",false)){
            Button sigBtn = findViewById(R.id.btn_sign);
            sigBtn.setWidth(0);
            sigBtn.setLayoutParams(new LinearLayout.LayoutParams(0,0,0));
            sigBtn.setVisibility(View.INVISIBLE);
        }
        if(sp.getBoolean("hideCode",false)){
            Button codeBtn = findViewById(R.id.btn_getcode);
            ((Button)findViewById(R.id.btn_sign)).setText("打卡");
            codeBtn.setWidth(0);
            codeBtn.setLayoutParams(new LinearLayout.LayoutParams(0,0,0));
            codeBtn.setVisibility(View.INVISIBLE);
        }

        circleProgress = findViewById(R.id.circleProgress);
        reloadLocks();
        setPss(0, "等待开始");
        //自动连接检查
        if (config_autoConnect) {
            //自动连接开始
            new Thread(this::openDoorPss).start();
        }
        //快捷方式操作
        String quick = getIntent().getAction();
        if (quick != null) {
            if (quick.equals("cc.xypp.yunmeiui.unlock")) {
                String data = getIntent().getDataString();
                if(data!=null && data.startsWith("yunmeiui://lock_info/")){
                    tmpLock = currentLock = new Lock(data);
                    exitOnce = true;
                }
                //开门
                new Thread(this::openDoorPss).start();
            } else if (quick.equals("cc.xypp.yunmeiui.sign")) {
                //签到
                new Thread(this::signPss).start();
            }
        }

        if (sp.getBoolean("firstrun",false)) {
            findViewById(R.id.tip_f).setVisibility(View.INVISIBLE);
        }else{
            SharedPreferences.Editor a = sp.edit();
            a.putBoolean("firstrun",true);
            a.apply();
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        reloadLocks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadLocks();
    }

    private void reloadLocks() {
        List<String> nameList = new ArrayList<>();
        locks = new ArrayList<>();
        locks.addAll(lockManageUtil.getAll());
        locks.forEach(lock -> nameList.add(lock.label));
        Lock def = lockManageUtil.getDef();
        if(def!=null){
            locks.add(0,def);
            nameList.add(0,def.label+"[默认]");
        }
        if(tmpLock!=null){
            nameList.add(0,tmpLock.label+"[本次]");
            locks.add(0,tmpLock);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, nameList);
        ((Spinner) findViewById(R.id.lockSelector)).setAdapter(adapter);
        if(locks.size() > 0){
            currentLock = locks.get(0);
            ((Spinner) findViewById(R.id.lockSelector)).setSelection(0);
        }else{
            setPss(0,"欢迎使用，请点击右下角加号登录账号");
        }
        ((Spinner) findViewById(R.id.lockSelector)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentLock = locks.get(i);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                currentLock = null;
            }
        });
    }

    void setPss(int pss, String tip) {
        setPss(pss, tip, false);
    }

    void setPss(int pss, String tip, boolean toast) {
        runOnUiThread(() -> {
            circleProgress.setTip(tip);
            circleProgress.SetCurrent(pss);
            circleProgress.SetMax(100);
            if (toast) {
                ToastUtil.show(this, tip);
            }
        });
    }

    public void loginClick(View view) {
        startActivity(new Intent(this, loginActivity.class));
    }

    public void settingClick(View view) {
        startActivity(new Intent(this, settingActivity.class));
    }

    public void openDoorClick(View view) {
        new Thread(this::openDoorPss).start();
        if(config_autoCode){
            new Thread(this::getCodePss).start();
        }
    }

    public void clickSign(View view) {
        new Thread(this::signPss).start();
    }

    private void toast(String tip) {
        runOnUiThread(() -> {
            ToastUtil.show(this, tip);
        });
    }

    private void disableBtn(boolean ds) {
        runOnUiThread(() -> {
            Button btn_unlock = findViewById(R.id.btn_unlock);
            btn_unlock.setClickable(!ds);
            Button btn_sign = findViewById(R.id.btn_sign);
            btn_sign.setClickable(!ds);
        });
    }
    public void clickGetCode(View view){
        new Thread(this::getCodePss).start();
    }
    private void openDoorPss() {
        if (      currentLock == null
                ||currentLock.D_CHAR == null
                || currentLock.D_CHAR.equals("")
                || currentLock.D_SERV == null
                || currentLock.D_SERV.equals("")) {
            setPss(0, "当前门锁不可用，您可能需要登录或选择其他门锁", true);
        } else {
            unlockService=new UnlockService(this, new UnlockService.Callback() {
                @Override
                public void setpss(int pss, String tip, boolean toast) {
                    setPss(pss,tip,toast);
                }
                @Override
                public void start() {
                    disableBtn(true);
                }
                @Override
                public void end() {
                    disableBtn(false);
                    //自动退出判断
                    if(exitOnce){
                        runOnUiThread(()->{
                            Handler handler = new Handler();
                            handler.postDelayed(() -> {
                                finish();
                            }, 3000);
                        });
                    }
                }

                @Override
                public void successed() {
                    if(sp.getBoolean("autoExit",false)){
                        toast("开门完成，程序将自动退出");
                        //自动退出生效
                        runOnUiThread(()->{
                            Handler handler = new Handler();
                            handler.postDelayed(() -> {
                                finish();
                                System.exit(0);
                            }, 3000);
                        });
                    }
                }
            },currentLock,config_quickConnect);
            unlockService.openDoorWork();
        }
    }
    private void signPss() {
        UserUtils userUtils = new UserUtils(context);
        User user = userUtils.getByNameMD5(currentLock.username);
        if(user==null){
            ToastUtil.show(context,"当前门锁的账号未保存");
            return;
        }
        signService = new SignService(context,currentLock,user, new SignService.Callback() {
            @Override
            public void setpss(int pss, String tip, boolean toast) {
                setPss(pss,tip,toast);
            }

            @Override
            public void start() {
                disableBtn(true);
            }

            @Override
            public void end() {
                disableBtn(false);
            }
        });
        signService.signEve();
    }
    private void getCodePss(){
        codeService = new CodeService(context, currentLock, new CodeService.Callback() {
            @Override
            public void setpss(int pss, String tip, boolean toast) {
                runOnUiThread(()->((Button)findViewById(R.id.btn_getcode)).setText(String.format("获取开门密码\n[%s]", tip)));

            }

            @Override
            public void start() {
                setpss(0,"登录",true);
                runOnUiThread(()->((Button)findViewById(R.id.btn_getcode)).setEnabled(false));
            }

            @Override
            public void end(String code) {
                if(!Objects.equals(code, "")){
                    setpss(100,code);
                }
                runOnUiThread(()->((Button)findViewById(R.id.btn_getcode)).setEnabled(true));
            }
        });
        codeService.getCode();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            //蓝牙扫描相关权限
            unlockService.onRequestPermissionsResult(requestCode,permissions,grantResults);
        } else if (requestCode == 105) {
            //蓝牙连接相关权限
            unlockService.onRequestPermissionsResult(requestCode,permissions,grantResults);
        } else if (requestCode == 109) {
            //GPS相关权限
            signService.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

}