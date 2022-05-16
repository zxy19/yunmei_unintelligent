package cc.xypp.yunmei;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URI;
import java.util.Base64;
import java.util.HashSet;

import cc.xypp.yunmei.eneity.Lock;

public class lockInfoActivity extends AppCompatActivity {
    Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx=this;
        setContentView(R.layout.activity_lock_info);
        SharedPreferences ssp;
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            ssp = EncryptedSharedPreferences.create(
                    "yunmei_secure",
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("姓名：");
        sb.append(ssp.getString("name", "未导入"));
        sb.append("\n");

        sb.append("用户ID：");
        sb.append(ssp.getString("userId", "未导入"));
        sb.append("\n");

        sb.append("学校：");
        sb.append(ssp.getString("school", "未导入"));
        sb.append("#");
        sb.append(ssp.getString("schoolNo", "未导入"));
        sb.append("\n");

        sb.append("区域：");
        sb.append(ssp.getString("area", "未导入"));
        sb.append("#");
        sb.append(ssp.getString("areaNo", "未导入"));
        sb.append("\n");

        sb.append("楼号：");
        sb.append(ssp.getString("build", "未导入"));
        sb.append("#");
        sb.append(ssp.getString("buildNo", "未导入"));
        sb.append("\n");

        sb.append("房间：");
        sb.append(ssp.getString("dorm", "未导入"));
        sb.append("#");
        sb.append(ssp.getString("dormNo", "未导入"));
        sb.append("\n");

        sb.append("锁UUID：");
        sb.append(ssp.getString("LUUID", "未导入"));
        sb.append("\n");
        sb.append("服务UUID：");
        sb.append(ssp.getString("SUUID", "未导入"));
        sb.append("\n");

        sb.append("上次定位：");
        sb.append(ssp.getString("lstLoca", "从未定位"));
        sb.append("\n");

        ((EditText)findViewById(R.id.info)).setText(sb.toString());
    }
    public void shareLock(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("危险操作提醒");
        builder.setMessage("警告！您正在执行一项非常危险的操作！\n分享您的门锁信息这一行为是不可逆的，门锁的保密数据理论上会伴随一个账号从创建到注销，一旦数据被获取，将会是安全数据的永久性泄露。\n您只应该于您非常非常信任的人或者您自己的其他设备线下共享该信息。\n确定要执行该操作吗？");
        builder.setPositiveButton("放弃", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int id) {}});
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
        builder.setPositiveButton("放弃", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int id) {}});
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
        builder.setNeutralButton("放弃", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int id) {}});
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences ssp;
                try {
                    String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                    ssp = EncryptedSharedPreferences.create(
                            "yunmei_secure",
                            masterKeyAlias,
                            ctx,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                String D_Mac = ssp.getString("LMAC", "");
                String D_SERV = ssp.getString("SUUID", "");
                String D_LOCK = ssp.getString("LUUID", "");
                String D_SEC = ssp.getString("lockSec", "");

                Uri uri = Uri.parse(Base64.getEncoder().encodeToString(new Lock("account", D_Mac, D_SERV, D_LOCK, D_SEC).toString().getBytes()));
                Intent i = new Intent(ctx,QrActivity.class);
                i.setData(uri);
                startActivity(i);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}