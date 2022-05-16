package cc.xypp.yunmei;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cc.xypp.yunmei.eneity.Lock;
import cc.xypp.yunmei.utils.OldVerConver;
import cc.xypp.yunmei.utils.ToastUtil;
import cc.xypp.yunmei.utils.http;
import cc.xypp.yunmei.wigets.CircleProgress;

public class MainActivity extends AppCompatActivity {
    Button btn_unlock;
    private Context context;
    private boolean noLocalMac=false;

    private String Uname;
    private String Upsw;
    private boolean USE_LST;
    private boolean STORE_THIS;
    private BleDevice D_LOBJ;
    private final String[] permission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private SharedPreferences sp;
    private SharedPreferences ssp;
    private Boolean qkcn;
    private String ULocate;
    private boolean uwait;
    CircleProgress circ;

    private Lock currentLock;
    private final List<Lock> locks = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BleManager.getInstance().init(getApplication());
        context=this;
        if(!BleManager.getInstance().isSupportBle()){
            toast("设备不支持蓝牙！");
            finish();
        }

        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            ssp = EncryptedSharedPreferences.create(
                    "yunmei_secure",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }


        sp = getSharedPreferences("storage", MODE_PRIVATE);

        System.out.println(sp.getAll().toString());

        boolean atco=sp.getBoolean("autoCon",false);
        qkcn=sp.getBoolean("quickCon",true);


        if(!sp.getString("LockSec","").equals("")){
            OldVerConver.dealInsecure(sp,ssp);
        }

        Uname = ssp.getString("loginUsr","");
        Upsw = ssp.getString("loginPsw","");


        circ = findViewById(R.id.circleProgress);
        setPss(0,"等待开始");
        if(atco)new Thread(this::openDoorPss).start();
        String quick = getIntent().getAction();
        if(quick!=null){
            if(quick.equals("cc.xypp.yunmei.unlock")){
                new Thread(this::openDoorPss).start();
            }else if(quick.equals("cc.xypp.yunmei.sign")){
                signEve();
            }
        }

        if(!Uname.equals("")){
            findViewById(R.id.tip_f).setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        String D_Mac = ssp.getString("LMAC", "");
        String D_SERV = ssp.getString("SUUID", "");
        String D_LOCK = ssp.getString("LUUID", "");
        String D_SEC = ssp.getString("lockSec", "");

        locks.add(currentLock=new Lock("账号门锁", D_Mac, D_SERV, D_LOCK, D_SEC));
        Set<String> lockSet = ssp.getStringSet("locks", new HashSet<>());
        List<String> nameSet=new ArrayList<>();
        nameSet.add("账号门锁");

        lockSet.forEach((v)->{
            String[] tmp = v.split("\\|");
            if(tmp.length==5) {
                locks.add(new Lock(tmp[0], tmp[1], tmp[2], tmp[3], tmp[4]));
                nameSet.add(tmp[0]);
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, nameSet);
        ((Spinner)findViewById(R.id.lockSelector)).setAdapter(adapter);
        ((Spinner)findViewById(R.id.lockSelector)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentLock=locks.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                currentLock=new Lock();
            }
        });
    }
    void setPss(int pss,String tip){
        setPss(pss,tip,false);
    }
    void setPss(int pss,String tip,boolean toast){
        runOnUiThread(()->{
            circ.setTip(tip);
            circ.SetCurrent(pss);
            circ.SetMax(100);
            if(toast){
                ToastUtil.show(this, tip);
            }
        });
    }
    public void loginClick(View view) {
        startActivity(new Intent(this, loginActivity.class));
    }
    public void settingClick(View view) {
        startActivity(new Intent(this, settingActivity.class));
    }
    public void openDoorClick(View view) {
        new Thread(this::openDoorPss).start();
    }
    public void clickSign(View view){
        signEve();
    }
    private void toast(String tip) {
        runOnUiThread(() -> {
            ToastUtil.show(this, tip);
        });

    }
    private void disableBtn(boolean ds){
        runOnUiThread(() -> {
            Button btn_unlock = findViewById(R.id.btn_unlock);
            btn_unlock.setClickable(!ds);
            Button btn_sign = findViewById(R.id.btn_sign);
            btn_sign.setClickable(!ds);
        });

    }
    private void openDoorPss() {
        qkcn=sp.getBoolean("quickCon",true);
        disableBtn(true);
        noLocalMac=!qkcn;

        if (currentLock==null || currentLock.D_SERV==null || currentLock.D_SERV.equals("") || currentLock.D_LOCK==null || currentLock.D_LOCK.equals("")) {
            setPss(0,"当前门锁不可用，您可能需要登录或选择其他门锁",true);
            disableBtn(false);
        }else openDoorWork();
    }
    private void openDoorWork() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            String[] genPms = new String[]{Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT};
            List<String> denyPermissions = new ArrayList<>();
            for (String value : genPms) {
                if (ContextCompat.checkSelfPermission(this, value) !=PackageManager.PERMISSION_GRANTED) {//判断权限是否已授权
                    //没有权限 就添加
                    denyPermissions.add(value);
                }
            }
            if (!denyPermissions.isEmpty()) {
                //申请权限授权
                setPss(10,"申请权限");
                ActivityCompat.requestPermissions(this, denyPermissions.toArray(new String[denyPermissions.size()]), 105);
                return;
            }
        }
        BluetoothAdapter blueadapter= BluetoothAdapter.getDefaultAdapter();
        if(!blueadapter.isEnabled()){
            setPss(13,"开启蓝牙...");
            if(!blueadapter.enable()) {
                setPss(0, "蓝牙没有启用", true);
                disableBtn(false);
                return;
            }
            setPss(15,"等待蓝牙...");
            try {
                for(int i=0;i<5;i++) {
                    sleep(2000);
                    if(blueadapter.isEnabled()){
                        break;
                    }else if(i==4){
                        setPss(0, "蓝牙没有启用", true);
                        disableBtn(false);
                        return;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!currentLock.D_Mac.equals("") && !noLocalMac){
            setPss(20,"开始快速连接");
            connect(currentLock.D_Mac);
        }else{
            setPss(20,"开始扫描");
            scan();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scan();
            } else {
                setPss(0,"您拒绝了权限请求",true);
                disableBtn(false);
            }
        }else if (requestCode == 105) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openDoorWork();
            } else {
                setPss(0,"您拒绝了权限请求",true);
                disableBtn(false);
            }
        }else if (requestCode == 109) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                signWork();
            } else {
                setPss(0,"您拒绝了权限请求",true);
                disableBtn(false);
            }
        }
    }
    private void scan(){
        List<String> denyPermissions = new ArrayList<>();
        for (String value : permission) {
            if (ContextCompat.checkSelfPermission(this, value) !=PackageManager.PERMISSION_GRANTED) {//判断权限是否已授权
                //没有权限 就添加
                denyPermissions.add(value);
            }
        }
        if (!denyPermissions.isEmpty()) {
            //申请权限授权
            ActivityCompat.requestPermissions(this, denyPermissions.toArray(new String[denyPermissions.size()]), 100);
            return;
        }

        UUID[] uuids={UUID.fromString(currentLock.D_LOCK)};
        System.out.println(Arrays.toString(uuids));
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(uuids)
                .build();
        BleManager bleManager = BleManager.getInstance();
        bleManager.initScanRule(scanRuleConfig);
        bleManager.scanAndConnect(new BleScanAndConnectCallback() {
            @Override
            public void onScanStarted(boolean success) {
                if(!success){
                    disableBtn(false);
                    setPss(0,"设备未找到",true);
                }
            }
            @Override
            public void onScanning(BleDevice bleDevice) {
                System.out.println(bleDevice);
            }

            @Override
            public void onScanFinished(BleDevice scanResult) {
                // 扫描结束，结果即为扫描到的第一个符合扫描规则的BLE设备，如果为空表示未搜索到（主线程）
                if(scanResult==null){
                    disableBtn(false);
                    setPss(0,"设备未找到",true);
                }else setPss(40,"设备已找到");
            }

            @Override
            public void onStartConnect() {
                setPss(43,"正在连接");
                // 开始连接（主线程）
            }

            @Override
            public void onConnectFail(BleDevice bleDevice,BleException exception) {
                setPss(0,"连接失败",true);
                disableBtn(false);
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                D_LOBJ=bleDevice;
                sendMsg();
                setPss(50,"连接成功");
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                // 连接断开，isActiveDisConnected是主动断开还是被动断开（主线程）
            }
        });
    }
    private void connect(String Mac){
        BleManager.getInstance().connect(Mac, new BleGattCallback() {
            @Override
            public void onStartConnect() {}

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                noLocalMac = true;
                scan();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                D_LOBJ = bleDevice;
                setPss(50,"连接成功");
                sendMsg();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {

            }
        });
    }
    private void sendMsg() {
        BleManager.getInstance().write(
                D_LOBJ,
                currentLock.D_LOCK,
                currentLock.D_SERV,
                getPwd(currentLock.D_SEC),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        if(current==total) {
                            setPss(100,"开门完成");
                            if (noLocalMac) {
                                currentLock.D_Mac = D_LOBJ.getMac();
                                SharedPreferences sp = getSharedPreferences("storage", MODE_PRIVATE);
                                SharedPreferences.Editor edit = sp.edit();
                                edit.putString("LMAC", currentLock.D_Mac);
                                edit.apply();
                            }
                            disableBtn(false);
                        }else{
                            setPss(75,"正在发送数据");
                        }
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        toast("开门失败");
                        disableBtn(false);
                    }
                });
    }
    private byte[] getPwd(String secret) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int pw = (int) Math.floor(Math.random() * 1000000);
        bos.write(208);
        int len = secret.length() + 2 + 2 + 10;
        bos.write(len);
        byte[] a = secret.getBytes();
        for (byte sa : a) {
            bos.write(sa);
        }
        bos.write(165);
        for (int i = 0; i < 6; i++) {
            bos.write((pw % 10));
            pw /= 10;
        }
        bos.write(73);
        bos.write(68);
        bos.write(48);
        bos.write(49);

        bos.write(167);
        return bos.toByteArray();
    }

    private void signEve(){
        Uname = ssp.getString("loginUsr","");
        Upsw = ssp.getString("loginPsw","");
        disableBtn(true);
        Thread sigWork = new Thread(this::signWork);
        String lstLoca = sp.getString("lstLoca","不存在");
        switch (sp.getString("sigLoc","ask")){
            case "lst":
                USE_LST=true;
                STORE_THIS=false;
                ULocate=lstLoca;
                setPss(5,"将使用上次的位置打卡");
                sigWork.start();
                return;
            case "rel":
                USE_LST=false;
                STORE_THIS=true;
                setPss(5,"将重新定位并打卡");
                sigWork.start();
                return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("打卡位置询问");
        builder.setMessage("即将进行打卡，您上次的打卡位置为："+lstLoca+"。请选择本次打卡的定位方式：");
        builder.setPositiveButton("定位并保存", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                USE_LST=false;
                STORE_THIS=true;
                sigWork.start();
            }
        });
        builder.setNeutralButton("定位", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                USE_LST=false;
                STORE_THIS=false;
                sigWork.start();
            }
        });
        builder.setNegativeButton("上次的位置", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                USE_LST=true;
                STORE_THIS=false;
                ULocate=lstLoca;
                sigWork.start();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void signWork(){
        if(Uname.equals("")||Upsw.equals("")){
            setPss(0,"请登录账号并重启程序",true);
            disableBtn(false);
            return;
        }
        if(USE_LST){
            if(ULocate.equals("不存在")){
                setPss(0,"上次定位信息不存在，请重新定位",true);
                disableBtn(false);
                return;
            }else{
                located();
                return;
            }
        }
        List<String> denyPermissions = new ArrayList<>();
        for (String value : permission) {
            if (ContextCompat.checkSelfPermission(this, value) !=PackageManager.PERMISSION_GRANTED) {//判断权限是否已授权
                //没有权限 就添加
                denyPermissions.add(value);
            }
        }
        if (!denyPermissions.isEmpty()) {
            //申请权限授权
            ActivityCompat.requestPermissions(this, denyPermissions.toArray(new String[denyPermissions.size()]), 109);
            setPss(5,"申请权限");
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean networkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!networkEnable &&!gpsEnable){
            setPss(0,"定位服务不可用",true);
            disableBtn(false);
        }else{
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
                    if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
                        uwait=false;
                        ULocate=location.getLongitude() + "," + location.getLatitude();
                        locationManager.removeUpdates(this);
                        located();
                    }else{
                        setPss(25,"等待定位完成");
                        ULocate=location.getLongitude() + "," + location.getLatitude();
                        uwait=true;
                        new Thread(()->{
                            try {
                                sleep(5000);
                                if(uwait){
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
            runOnUiThread(()->locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, mLocationListener));
             runOnUiThread(()->locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener));
            setPss(15,"正在定位");

        }
    }

    private void located() {
        setPss(40,"定位完成");
        if(STORE_THIS){
            SharedPreferences.Editor a = sp.edit();
            a.putString("lstLoca",ULocate);
            a.apply();
        }
        new Thread(()-> {
            System.out.println(ULocate);
            String LoInfo = ULocate;
            try {
                String userId;
                setPss(55,"登录");
                http sess = new http("https://base.yunmeitech.com/");
                Map<String, String> loginDat = new HashMap<>();
                loginDat.put("userName", Uname);
                loginDat.put("userPwd", Upsw);
                JSONObject loginRes = new JSONObject(sess.post("/login", loginDat));
                if (!loginRes.getBoolean("success")) {
                    setPss(0,loginRes.getString("msg"),true);
                    disableBtn(false);
                    return;
                }
                JSONObject temp = loginRes.getJSONObject("o");
                sess.setToken(temp.getString("token"), (userId = temp.getString("userId")));
                Map<String, String> schoolDat = new HashMap<>();
                schoolDat.put("userId", userId);
                setPss(70,"获取学校");
                JSONObject schoolRes = new JSONArray(sess.post("/userschool/getbyuserid", schoolDat)).getJSONObject(0);
                if (schoolRes == null) {
                    setPss(0,"学校获取异常！",true);
                    disableBtn(false);
                    return;
                }
                sess.setBaseURL(schoolRes.getJSONObject("school").getString("serverUrl"));
                sess.setToken(schoolRes.getString("token"), userId);
                String schoolNo = schoolRes.getString("schoolNo");
                //LOCK
                setPss(85,"获取门锁");
                Map<String, String> lockDat = new HashMap<>();
                schoolDat.put("schoolNo", schoolNo);
                JSONObject lockRes = new JSONArray(sess.post("/dormuser/getuserlock", schoolDat)).getJSONObject(0);
                if (lockRes == null) {
                    setPss(0,"门锁请求出错！",true);
                    disableBtn(false);
                    return;
                }
                Map<String, String> sigDat = new HashMap<>();
                sigDat.put("schoolNo", schoolNo);
                sigDat.put("lockNo", lockRes.getString("lockNo"));
                sigDat.put("location", LoInfo);
                setPss(95,"打卡");
                JSONObject sigRes = new JSONObject(sess.post("/signrecord/signbyschool", sigDat));
                if (!sigRes.getBoolean("success")) {
                    setPss(0,sigRes.getString("msg"),true);
                    disableBtn(false);
                }else{
                    setPss(100,sigRes.getString("msg"));
                    disableBtn(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                setPss(0,"打卡时出错！",true);
                disableBtn(false);
            }
        }).start();
    }
}