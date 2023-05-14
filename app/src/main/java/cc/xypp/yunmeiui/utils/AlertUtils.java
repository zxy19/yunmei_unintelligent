package cc.xypp.yunmeiui.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import java.util.List;

public class AlertUtils {
    static public abstract class callbacker{
        public abstract void select(int id);
        public abstract void cancel();
    }
    static public abstract class callbacker_Str{
        public abstract void select(String val);
        public abstract void cancel();
    }
    static public void showList(Activity context, String title, List<String> opt, callbacker callback){
        context.runOnUiThread(()->{
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title)
                    .setItems(opt.toArray(new String[0]), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            callback.select(which);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            callback.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }
    static public void show(Activity context, String title,String msg, callbacker callback) {
        context.runOnUiThread(() -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(msg);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    callback.select(0);
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    callback.cancel();
                }
            });
            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.show();
        });
    }
    static public void input(Activity context,String title,callbacker_Str callbacker){
        final EditText input = new EditText(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("请输入")
            .setView(input)
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String inputString = input.getText().toString();
                    callbacker.select(inputString);
                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    callbacker.cancel();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
}
