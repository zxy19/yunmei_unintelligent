package cc.xypp.yunmeiui;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.TileService;

import androidx.annotation.Nullable;

public class serviceOpenDoor extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        Intent in = new Intent(this, MainActivity.class);
        in.setAction("cc.xypp.yunmeiui.unlock");
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityAndCollapse(in);
    }
}
