package cc.xypp.yunmeiui.service;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Thread.sleep;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.xypp.yunmeiui.eneity.Lock;
import cc.xypp.yunmeiui.eneity.User;
import cc.xypp.yunmeiui.utils.SecureStorage;
import cc.xypp.yunmeiui.utils.YunmeiAPI;

public class SignService {


    private final Lock currentLock;
    private final User user;
    private boolean uwait;

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 109) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                signWork();
            } else {
                callback.setpss(0, "您拒绝了权限请求", true);
                callback.end();
            }
        }
    }

    public static abstract class Callback {
        public void setpss(int pss, String tip) {
            setpss(pss, tip, false);
        }

        ;

        public abstract void setpss(int pss, String tip, boolean toast);

        public abstract void start();

        public abstract void end();
    }

    private final String[] permission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};


    private final Activity ctx;
    private final Callback callback;
    private boolean useLastLocation;
    private boolean StoreLocation;

    SecureStorage secureStorage;


    private String UserLocation;
    private SharedPreferences sp;


    public SignService(Activity context,Lock currentLock, User user, Callback _callback) {
        ctx = context;
        callback = _callback;
        secureStorage = new SecureStorage(ctx);
        sp = ctx.getSharedPreferences("storage", MODE_PRIVATE);
        this.currentLock = currentLock;
        this.user = user;
    }

    public void signEve() {
        callback.start();
        Thread sigWork = new Thread(this::signWork);
        String lstLoca = sp.getString("lstLoca", "不存在");
        switch (sp.getString("sigLoc", "ask")) {
            case "lst":
                useLastLocation = true;
                StoreLocation = false;
                UserLocation = lstLoca;
                callback.setpss(5, "将使用上次的位置打卡");
                sigWork.start();
                return;
            case "rel":
                useLastLocation = false;
                StoreLocation = true;
                callback.setpss(5, "将重新定位并打卡");
                sigWork.start();
                return;
        }
        ctx.runOnUiThread(()->{
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("打卡位置询问");
            builder.setMessage("即将进行打卡，您上次的打卡位置为：" + lstLoca + "。请选择本次打卡的定位方式：");
            builder.setPositiveButton("定位并保存", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    useLastLocation = false;
                    StoreLocation = true;
                    sigWork.start();
                }
            });
            builder.setNeutralButton("定位", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    useLastLocation = false;
                    StoreLocation = false;
                    sigWork.start();
                }
            });
            builder.setNegativeButton("上次的位置", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    useLastLocation = true;
                    StoreLocation = false;
                    UserLocation = lstLoca;
                    sigWork.start();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void signWork() {
        if (useLastLocation) {
            if (UserLocation.equals("不存在")) {
                callback.setpss(0, "上次定位信息不存在，请重新定位", true);
                callback.end();
                return;
            } else {
                located();
                return;
            }
        }
        List<String> denyPermissions = new ArrayList<>();
        for (String value : permission) {
            if (ContextCompat.checkSelfPermission(ctx, value) != PackageManager.PERMISSION_GRANTED) {//判断权限是否已授权
                //没有权限 就添加
                denyPermissions.add(value);
            }
        }
        if (!denyPermissions.isEmpty()) {
            //申请权限授权
            ActivityCompat.requestPermissions(ctx, denyPermissions.toArray(new String[denyPermissions.size()]), 109);
            callback.setpss(5, "申请权限");
            return;
        }
        LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        boolean networkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!networkEnable && !gpsEnable) {
            callback.setpss(0, "定位服务不可用", true);
            callback.end();
        } else {
            LocationListener mLocationListener = new LocationListener() {
                // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                // Provider被enable时触发此函数，比如GPS被打开
                @Override
                public void onProviderEnabled(String provider) {
                }

                // Provider被disable时触发此函数，比如GPS被关闭
                @Override
                public void onProviderDisabled(String provider) {

                }

                //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                @Override
                public void onLocationChanged(Location location) {
                    System.out.println(location);
                    //更新位置信息
                    if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                        uwait = false;
                        UserLocation = location.getLongitude() + "," + location.getLatitude();
                        locationManager.removeUpdates(this);
                        located();
                    } else {
                        callback.setpss(25, "等待定位完成");
                        UserLocation = location.getLongitude() + "," + location.getLatitude();
                        uwait = true;
                        new Thread(() -> {
                            try {
                                sleep(5000);
                                if (uwait) {
                                    locationManager.removeUpdates(this);
                                    located();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();

                    }
                }
            };
            ctx.runOnUiThread(() -> locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, mLocationListener));
            ctx.runOnUiThread(() -> locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener));
            callback.setpss(15, "正在定位");

        }
    }

    private void located() {
        callback.setpss(40, "定位完成");
        if (StoreLocation) {
            SharedPreferences.Editor a = sp.edit();
            a.putString("lstLoca", UserLocation);
            a.apply();
        }
        new Thread(() -> {
            System.out.println(UserLocation);
            String LoInfo = UserLocation;
            try {
                callback.setpss(55, "登录");
                YunmeiAPI api = new YunmeiAPI(user.username, user.passwordMD5,true);
                for (int i = 0; i < api.schools.size(); i++) {
                    if (api.schools.get(i).schoolNo.equals(currentLock.schoolNo)) {
                        api.setSchool(i);
                        break;
                    }
                }
                callback.setpss(85, "获取门锁");
                List<Lock> lockList = api.loginAndGetLock();
                String lockNoConfirm = "";
                for (Lock lock : lockList) {
                    if (lock.D_Mac.equals(currentLock.lockNo)) {
                        lockNoConfirm = currentLock.lockNo;
                        break;
                    }
                }
                Map<String, String> sigDat = new HashMap<>();
                sigDat.put("schoolNo", currentLock.schoolNo);
                sigDat.put("lockNo", currentLock.lockNo);
                sigDat.put("location", LoInfo);
                callback.setpss(95, "打卡");
                JSONObject sigRes = new JSONObject(api.sess.post("/signrecord/signbyschool", sigDat));
                if (!sigRes.getBoolean("success")) {
                    callback.setpss(0, sigRes.getString("msg"), true);
                    callback.end();
                } else {
                    callback.setpss(100, sigRes.getString("msg"));
                    callback.end();
                }
            }catch(RuntimeException e){
                callback.setpss(0, e.getMessage(), true);
                callback.end();
            } catch (Exception e) {
                e.printStackTrace();
                callback.setpss(0, "打卡时出错！", true);
                callback.end();
            }
        }).start();
    }


}
