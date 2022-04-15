package cc.xypp.yunmei;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class lockInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_info);
        //if(savedInstanceState.containsKey("success"))
         //   ((TextView)findViewById(R.id.ttitle)).setText("已成功导入信息");
        SharedPreferences sp = getSharedPreferences("storage", MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        sb.append("姓名：");
        sb.append(sp.getString("name", "未导入"));
        sb.append("\n");

        sb.append("用户ID：");
        sb.append(sp.getString("userId", "未导入"));
        sb.append("\n");

        sb.append("学校：");
        sb.append(sp.getString("school", "未导入"));
        sb.append("#");
        sb.append(sp.getString("schoolNo", "未导入"));
        sb.append("\n");

        sb.append("区域：");
        sb.append(sp.getString("area", "未导入"));
        sb.append("#");
        sb.append(sp.getString("areaNo", "未导入"));
        sb.append("\n");

        sb.append("楼号：");
        sb.append(sp.getString("build", "未导入"));
        sb.append("#");
        sb.append(sp.getString("buildNo", "未导入"));
        sb.append("\n");

        sb.append("房间：");
        sb.append(sp.getString("dorm", "未导入"));
        sb.append("#");
        sb.append(sp.getString("dormNo", "未导入"));
        sb.append("\n");

        sb.append("锁UUID：");
        sb.append(sp.getString("LUUID", "未导入"));
        sb.append("\n");
        sb.append("服务UUID：");
        sb.append(sp.getString("SUUID", "未导入"));
        sb.append("\n");

        sb.append("上次定位：");
        sb.append(sp.getString("lstLoca", "从未定位"));
        sb.append("\n");

        ((EditText)findViewById(R.id.info)).setText(sb.toString());
    }
}