package cc.xypp.yunmei.utils;


import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtil {
    static Toast toast = null;

    public static void show(Context context, String text) {
        if (toast != null) {
            toast.setText(text);
        } else {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }
        toast.show();
    }
}
