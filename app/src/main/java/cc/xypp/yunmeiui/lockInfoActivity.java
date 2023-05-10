package cc.xypp.yunmeiui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cc.xypp.yunmeiui.eneity.Lock;
import cc.xypp.yunmeiui.utils.AlertUtils;
import cc.xypp.yunmeiui.utils.LockManageUtil;
import cc.xypp.yunmeiui.utils.SecureStorage;

public class lockInfoActivity extends AppCompatActivity {
    Context ctx;
    SecureStorage secureStorage;
    LockManageUtil lockManageUtil;
    List<Lock> lockList;
    Lock currentLock = null;
    private Lock defaultLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_lock_info);
        secureStorage = new SecureStorage(this);
        lockManageUtil = new LockManageUtil(this);
        defaultLock = lockManageUtil.getDef();
        reloadLock();
    }

    private void showLock(Lock lock) {
        String sb = "锁标签：" +
                lock.label +
                "\n" +
                "锁UUID：" +
                lock.D_SERV +
                "\n" +
                "服务UUID：" +
                lock.D_CHAR +
                "\n" +
                "锁Mac：" +
                lock.D_Mac +
                "\n";
        if(defaultLock != null && Objects.equals(defaultLock.label, lock.label)){
            sb += "【默认门锁】";
            ((Switch)findViewById(R.id.lock_setdefault)).setChecked(true);
        }else{
            ((Switch)findViewById(R.id.lock_setdefault)).setChecked(false);
        }
        ((EditText) findViewById(R.id.info)).setText(sb);
    }

    public void deleteLock(View view) {
        AlertUtils.show(this, "确认删除", "您确定要删除这个门锁", new AlertUtils.callbacker() {
            @Override
            public void select(int id) {
                lockManageUtil.remove(currentLock);
                if(defaultLock != null && Objects.equals(defaultLock.label, currentLock.label)){
                    lockManageUtil.setDef(null);
                }
                reloadLock();
            }

            @Override
            public void cancel() {
            }
        });

    }

    private void reloadLock() {
        ((EditText) findViewById(R.id.info)).setText("");
        lockList = lockManageUtil.getAll();
        List<String> nameList = new ArrayList<>();
        lockList.forEach(lock -> nameList.add(lock.label));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, nameList);
        ((Spinner) findViewById(R.id.spinner_lockinfo)).setAdapter(adapter);
        ((Spinner) findViewById(R.id.spinner_lockinfo)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentLock = lockList.get(i);
                showLock(currentLock);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                currentLock = null;
                ((EditText) findViewById(R.id.info)).setText("无门锁");
            }
        });
    }


    public void shareLock(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("危险操作提醒");
        builder.setMessage("警告！您正在执行一项非常危险的操作！\n分享您的门锁信息这一行为是不可逆的，门锁的保密数据理论上会伴随一个账号从创建到注销，一旦数据被获取，将会是安全数据的永久性泄露。\n您只应该于您非常非常信任的人或者您自己的其他设备线下共享该信息。\n确定要执行该操作吗？");
        builder.setPositiveButton("放弃", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setNeutralButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                alertB();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void alertB() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("危险操作再次提醒");
        builder.setMessage("警告！您正在执行一项的操作确实是非常危险的！\n您不应该为了图方便而将门锁通过互联网等途径共享给其他人，即便对方急需开门进入寝室\n您只应该于您非常非常信任的人或者您自己的其他设备线下共享该信息。\n再次确定，要执行该操作吗？");
        builder.setPositiveButton("放弃", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                alertC();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void alertC() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最后提醒");
        builder.setMessage("确认操作后将在屏幕上显示门锁数据的二维码。如非必要，请不要截图保存或发送给他人。扫码时请关注身边，避免二维码被盗扫。\n您确定要分享数据吗");
        builder.setNeutralButton("放弃", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Uri uri = Uri.parse(currentLock.toString());
                Intent i = new Intent(ctx, QrActivity.class);
                i.setData(uri);
                startActivity(i);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void createNFC(View view){
        Intent in = new Intent(this,NfcActivity.class);
        in.setAction("cc.xypp.yunmeiui.nfc");
        Lock tmp = new Lock(currentLock.toString());
        tmp.D_SEC="";
        tmp.D_Mac="";
        in.setData(Uri.parse(tmp.toString()));
        startActivity(in);
    }
    public void setDefault(View view){
        if(((Switch)findViewById(R.id.lock_setdefault)).isChecked()){
            lockManageUtil.setDef(currentLock);
            defaultLock = currentLock;
        }else{
            lockManageUtil.setDef(null);
            defaultLock=null;
        }
        showLock(currentLock);
    }
}