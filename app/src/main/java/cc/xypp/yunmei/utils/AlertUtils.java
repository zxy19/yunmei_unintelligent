package cc.xypp.yunmei.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.List;

public class AlertUtils {
    static public abstract class callbacker{
        public abstract void select(int id);
        public abstract void cancel();
    }
    static public void showList(Context context, String title, List<String> opt, callbacker callback){
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
    }
}
