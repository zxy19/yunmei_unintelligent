package cc.xypp.yunmeiui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.xypp.yunmeiui.eneity.Lock;
import cc.xypp.yunmeiui.eneity.User;
import cc.xypp.yunmeiui.utils.AlertUtils;
import cc.xypp.yunmeiui.utils.SecureStorage;
import cc.xypp.yunmeiui.utils.ToastUtil;
import cc.xypp.yunmeiui.utils.UserUtils;

public class settingActivity extends AppCompatActivity {
    private final List<Lock> locks = new ArrayList<>();
    private SharedPreferences sp, ssp;
    private int currentLock = -1;
    SecureStorage secureStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sp = getSharedPreferences("storage", MODE_PRIVATE);
        System.out.println(((Switch) findViewById(R.id.quickConn)).isChecked());
        System.out.println(((Switch) findViewById(R.id.autoConn)).isChecked());
        System.out.println(((Switch) findViewById(R.id.autoExit)).isChecked());
        ((Switch) findViewById(R.id.quickConn)).setChecked(sp.getBoolean("quickCon", true));
        ((Switch) findViewById(R.id.autoConn)).setChecked(sp.getBoolean("autoCon", false));
        ((Switch) findViewById(R.id.autoExit)).setChecked(sp.getBoolean("autoExit", false));
        ((Switch) findViewById(R.id.autocode)).setChecked(sp.getBoolean("autoCode", false));
        ((Switch) findViewById(R.id.always_code)).setChecked(sp.getBoolean("alwaysCode", false));
        ((Switch) findViewById(R.id.hide_sign)).setChecked(sp.getBoolean("hideSign", false));
        ((Switch) findViewById(R.id.force_orientation)).setChecked(sp.getBoolean("forceOrientation", false));
        ((Switch) findViewById(R.id.attempt_upload)).setChecked(sp.getBoolean("attemptUpload", false));
        ((EditText) findViewById(R.id.record_object)).setText(sp.getString("recordObject", ""));
        if (!sp.getBoolean("attemptUpload", false)) {
            findViewById(R.id.record_obj_label).setVisibility(View.GONE);
            findViewById(R.id.record_object).setVisibility(View.GONE);
            findViewById(R.id.record_obj_btn).setVisibility(View.GONE);
        }
        switch (sp.getString("sigLoc", "ask")) {
            case "ask":
                ((RadioButton) findViewById(R.id.sigLocOpt_ask)).setChecked(true);
                break;
            case "rel":
                ((RadioButton) findViewById(R.id.sigLocOpt_rel)).setChecked(true);
                break;
            case "lst":
                ((RadioButton) findViewById(R.id.sigLocOpt_lst)).setChecked(true);
                break;
            default:
                ((RadioButton) findViewById(R.id.sigLocOpt_ask)).setChecked(true);
                break;
        }
        secureStorage = new SecureStorage(this);
    }

    public void edit_quickCon(View view) {
        SharedPreferences.Editor a = sp.edit();
        a.putBoolean("quickCon", ((Switch) findViewById(R.id.quickConn)).isChecked());
        a.apply();
    }

    public void edit_autoCon(View view) {
        if (((Switch) findViewById(R.id.autoConn)).isChecked() && ((Switch) findViewById(R.id.autoExit)).isChecked()) {
            checkDanger();
            return;
        }
        SharedPreferences.Editor a = sp.edit();
        a.putBoolean("autoCon", ((Switch) findViewById(R.id.autoConn)).isChecked());
        a.apply();
    }

    public void edit_autoExit(View view) {
        if (((Switch) findViewById(R.id.autoConn)).isChecked() && ((Switch) findViewById(R.id.autoExit)).isChecked()) {
            checkDanger();
            return;
        }
        SharedPreferences.Editor a = sp.edit();
        a.putBoolean("autoExit", ((Switch) findViewById(R.id.autoExit)).isChecked());
        a.apply();
    }

    public void checkDanger() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("危险操作提醒");
        builder.setMessage("警告！您正在执行一项非常危险的操作！\n理论上同时打开自动开门和自动退出会导致以后无法再进行APP的其他操作。\n您只应该在确定了风险后执行此操作。\n确定要执行该操作吗？");
        builder.setPositiveButton("放弃", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ((Switch) findViewById(R.id.autoConn)).setChecked(sp.getBoolean("autoCon", false));
                ((Switch) findViewById(R.id.autoExit)).setChecked(sp.getBoolean("autoExit", false));
            }
        });
        builder.setNeutralButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                alertAgain();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void alertAgain() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("危险操作再次提醒");
        builder.setMessage("警告！您正在执行一项的操作确实是非常危险的！\n您不应该为了图方便而忽视风险\n您需要在确定不使用本APP其他功能时才执行此操作。\n再次确定，要执行该操作吗？");
        builder.setPositiveButton("放弃", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ((Switch) findViewById(R.id.autoConn)).setChecked(sp.getBoolean("autoCon", false));
                ((Switch) findViewById(R.id.autoExit)).setChecked(sp.getBoolean("autoExit", false));
            }
        });
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                alertLast();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void alertLast() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最后提醒");
        builder.setMessage("确认操作后将同时打开这两项功能。请再次确认。\n如需恢复，可选择清除软件数据或重新安装。\n您确定要打开吗");
        builder.setNeutralButton("放弃", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ((Switch) findViewById(R.id.autoConn)).setChecked(sp.getBoolean("autoCon", false));
                ((Switch) findViewById(R.id.autoExit)).setChecked(sp.getBoolean("autoExit", false));
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences.Editor a = sp.edit();
                a.putBoolean("autoCon", ((Switch) findViewById(R.id.autoConn)).isChecked());
                a.putBoolean("autoExit", ((Switch) findViewById(R.id.autoExit)).isChecked());
                a.apply();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void clickClearInfo(View view) {
        UserUtils userUtils = new UserUtils(this);
        List<User> userList = userUtils.getAll();
        if (userList.size() == 0) {
            ToastUtil.show(this, "无保存的账号");
        } else {
            List<String> nameList = new ArrayList<>();
            userList.forEach(user -> nameList.add(user.username));
            AlertUtils.showList(this, "选择用户", nameList, new AlertUtils.callbacker() {
                @Override
                public void select(int id) {
                    userUtils.remove(userList.get(id));
                }

                @Override
                public void cancel() {

                }
            });
        }
    }

    public void ClickSigLoc(View view) {
        SharedPreferences.Editor a = sp.edit();
        if (view.getId() == R.id.sigLocOpt_ask)
            a.putString("sigLoc", "ask");
        else if (view.getId() == R.id.sigLocOpt_lst)
            a.putString("sigLoc", "lst");
        else if (view.getId() == R.id.sigLocOpt_rel)
            a.putString("sigLoc", "rel");
        a.apply();
    }

    public void clickCheckInfo(View view) {
        startActivity(new Intent(this, lockInfoActivity.class));
    }

    public void about(View view) {
        Uri uri = Uri.parse("https://yunmei.xypp.cc/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void scanQR(View view) {
        // 创建IntentIntegrator对象
        IntentIntegrator intentIntegrator = new IntentIntegrator(settingActivity.this);
        // 开始扫描
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 获取解析结果
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "取消扫描", Toast.LENGTH_LONG).show();
            } else {
                String c = result.getContents();
                if (c.startsWith("yunmeiui://lock_info/") || c.startsWith("https://yunmeiui.xypp.cc/#/lock_info/")) {
                    Intent i = new Intent(this, addLockActivity.class);
                    i.setData(Uri.parse(c));
                    startActivity(i);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void edit_hideSign(View view) {
        SharedPreferences.Editor a = sp.edit();
        a.putBoolean("hideSign", ((Switch) findViewById(R.id.hide_sign)).isChecked());
        a.apply();
    }

    public void edit_alwaysCode(View view) {
        SharedPreferences.Editor a = sp.edit();
        a.putBoolean("alwaysCode", ((Switch) findViewById(R.id.always_code)).isChecked());
        a.apply();
    }

    public void edit_autocode(View view) {
        SharedPreferences.Editor a = sp.edit();
        a.putBoolean("autoCode", ((Switch) findViewById(R.id.autocode)).isChecked());
        a.apply();
    }

    public void edit_hideCode(View view) {
        SharedPreferences.Editor a = sp.edit();
        a.putBoolean("hideCode", ((Switch) findViewById(R.id.hide_code)).isChecked());
        a.apply();
    }

    public void edit_forceOrientation(View view) {
        SharedPreferences.Editor a = sp.edit();
        a.putBoolean("forceOrientation", ((Switch) findViewById(R.id.force_orientation)).isChecked());
        a.apply();
    }

    public void edit_attemptUpload(View view) {
        if (!((Switch) findViewById(R.id.attempt_upload)).isChecked()) {
            SharedPreferences.Editor a = sp.edit();
            a.putBoolean("attemptUpload", false);
            a.apply();
            findViewById(R.id.record_obj_label).setVisibility(View.GONE);
            findViewById(R.id.record_object).setVisibility(View.GONE);
            findViewById(R.id.record_obj_btn).setVisibility(View.GONE);
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("不确定性功能提醒");
        builder.setMessage("该功能将尝试向云莓智能服务器上报开锁结果，包含电量等信息\n当前版本不会获取您的个人信息，而是使用特定的信息固定上报。\n该功能未经过大量验证，可能导致问题，请谨慎使用。");
        builder.setNeutralButton("放弃", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ((Switch) findViewById(R.id.attempt_upload)).setChecked(false);
                findViewById(R.id.record_obj_label).setVisibility(View.GONE);
                findViewById(R.id.record_object).setVisibility(View.GONE);
                findViewById(R.id.record_obj_btn).setVisibility(View.GONE);
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences.Editor a = sp.edit();
                a.putBoolean("attemptUpload", true);
                a.apply();
                findViewById(R.id.record_obj_label).setVisibility(View.VISIBLE);
                findViewById(R.id.record_object).setVisibility(View.VISIBLE);
                findViewById(R.id.record_obj_btn).setVisibility(View.VISIBLE);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void btn_recordObj(View view) {
        SharedPreferences.Editor a = sp.edit();
        EditText b = findViewById(R.id.record_object);
        a.putString("recordObj", b.getText().toString());
        a.apply();
        ToastUtil.show(this, "已保存");
    }
}