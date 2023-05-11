package cc.xypp.yunmeiui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import cc.xypp.yunmeiui.eneity.Lock;
import cc.xypp.yunmeiui.utils.LockManageUtil;
import cc.xypp.yunmeiui.utils.ToastUtil;

public class addLockActivity extends AppCompatActivity {
    LockManageUtil lockManageUtil;
    Lock toAdd;
    List<Lock> locksList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lock);

        lockManageUtil = new LockManageUtil(this);

        Intent intent = getIntent();
        String Url=intent.getDataString();
        toAdd = new Lock(Url);
        if(toAdd.label.equals("account")){
            toAdd.label=getLockNameAuto();
        }

        locksList = lockManageUtil.getAll();
        ((EditText)findViewById(R.id.lockLabel)).setText(toAdd.label);
    }

    private String getLockNameAuto() {
        for(int i = 0;; i++){
            boolean exi=false;
            for(Lock s : locksList){
                if(s.label.startsWith("LockData#"+ i)){
                    exi=true;
                    break;
                }
            }
            if(!exi){
                return "LockData#"+ i;
            }
        }
    }

    public void delete(View view){
        toAdd=null;
        finish();
    }
    public void close(View view){
        if(toAdd!=null){
            toAdd.label= String.valueOf(((EditText)findViewById(R.id.lockLabel)).getText());
            try {
                lockManageUtil.add(toAdd);
            }catch (RuntimeException e){
                ToastUtil.show(this,e.getMessage());
            }
        }
        finish();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
}