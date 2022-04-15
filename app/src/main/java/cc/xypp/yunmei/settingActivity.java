package cc.xypp.yunmei;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Switch;

public class settingActivity extends AppCompatActivity {
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sp = getSharedPreferences("storage", MODE_PRIVATE);
        System.out.println(((Switch)findViewById(R.id.quickConn)).isChecked());
        System.out.println(((Switch)findViewById(R.id.autoConn)).isChecked());
        ((Switch)findViewById(R.id.quickConn)).setChecked(sp.getBoolean("quickCon",false));
        ((Switch)findViewById(R.id.autoConn)).setChecked(sp.getBoolean("autoCon",false));
        switch (sp.getString("sigLoc","ask")){
            case "ask":
                ((RadioButton)findViewById(R.id.sigLocOpt_ask)).setChecked(true);
            case "rel":
                ((RadioButton)findViewById(R.id.sigLocOpt_rel)).setChecked(true);
            case "lst":
                ((RadioButton)findViewById(R.id.sigLocOpt_lst)).setChecked(true);
        }
    }
    public void edit_quickCon(View view){
        SharedPreferences.Editor a = sp.edit();
        a.putBoolean("quickCon",((Switch)findViewById(R.id.quickConn)).isChecked());
        a.apply();
    }
    public void edit_autoCon(View view){
        SharedPreferences.Editor a = sp.edit();
        a.putBoolean("autoCon",((Switch)findViewById(R.id.autoConn)).isChecked());
        a.apply();
    }
    public void clickClearInfo(View view){
        Boolean atco=sp.getBoolean("autoCon",false);
        Boolean qkcn=sp.getBoolean("quickCon",true);
        SharedPreferences.Editor a = sp.edit();
        a.clear();
        a.apply();
    }
    public void ClickSigLoc(View view){
        SharedPreferences.Editor a = sp.edit();
        if(view.getId()==R.id.sigLocOpt_ask)a.putString("sigLoc","ask");
        else if(view.getId()==R.id.sigLocOpt_lst)a.putString("sigLoc","lst");
        else if(view.getId()==R.id.sigLocOpt_rel)a.putString("sigLoc","rel");
        a.apply();
    }
    public void clickCheckInfo(View view){
        startActivity(new Intent(this, lockInfoActivity.class));
    }
    public void about(View view){
        Uri uri = Uri.parse("https://yunmei.xypp.cc/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

}