package cc.xypp.yunmeiui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cc.xypp.yunmeiui.eneity.Lock;
import cc.xypp.yunmeiui.utils.AlertUtils;
import cc.xypp.yunmeiui.utils.LockManageUtil;
import cc.xypp.yunmeiui.utils.ToastUtil;

public class lockDebug extends AppCompatActivity {
    private final String COMMAND_MAPSTR = "SECKEY:D0\n" +
            "REG:A1\n" +
            "RESET:A2\n" +
            "RESECRET:A3\n" +
            "RENAME:A4\n" +
            "NEWPWD:A5\n" +
            "DELPWD:A6\n" +
            "OPEN:A7\n" +
            "GETOPENRECORD:BF\n" +
            "SETTIME:AD\n" +
            "CARD_START:B2\n" +
            "CART_BEGIN:B3\n" +
            "CARD_ADD:B4\n" +
            "CARD_CANCEL:B5\n" +
            "CARD_DELETE:B6\n" +
            "CARD_NO:E6\n" +
            "CARD_SAVE:B7";
    private String[] SELECT_COMMANDS;
    Lock curlock;
    Activity context;
    private BleDevice currentDevice;
    EditText res;

    private class commands {
        String command;
        String commandDesc;
        String arg;
    }

    private List<commands> commandsList = new ArrayList<>();

    private class bleNotifyCallback extends BleNotifyCallback {
        @Override
        public void onNotifySuccess() {
            res.append("+事件订阅：成功\n");
        }

        @Override
        public void onNotifyFailure(BleException exception) {
            res.append("X事件订阅：失败\n");
        }

        @Override
        public void onCharacteristicChanged(byte[] data) {
            res.append("=======Notify======\n");
            res.append(data2tipstr(data));
            res.append("===================\n");

        }
    }

    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_lock_debug);
        res = findViewById(R.id.editTextTextMultiLine2);
        curlock = new Lock(getIntent().getDataString());
        SELECT_COMMANDS = COMMAND_MAPSTR.split("\\n");
    }


    private String data2tipstr(byte[] data) {
        StringBuilder hexString = new StringBuilder();
        StringBuilder asciiString = new StringBuilder();
        for (byte b : data) {
            hexString.append(String.format("%02X ", b));
            asciiString.append((char) b);
        }
        return hexString.append("\n").append(asciiString).append("\n").toString();
    }

    public void connectEvent(View view) {
        connect();
    }

    public void sendEvent(View view) {
        AlertUtils.show(this, "确定发送", descDat(), new AlertUtils.callbacker() {
            @Override
            public void select(int id) {
                BleManager.getInstance().write(
                        currentDevice,
                        curlock.D_SERV,
                        curlock.D_CHAR,
                        getDat(),
                        new BleWriteCallback() {
                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                if (current == total) {
                                    res.append("+ 发送完成\n");
                                    command_initseg();
                                } else {
                                    res.append(String.format("+ %d/%d 发送中\n", current, total));
                                }
                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                res.append("X 发送失败\n");
                            }
                        });
            }
            @Override
            public void cancel() {

            }
        });
    }

    public void command_removeseg(View view) {
        List<String> textList = new ArrayList<>();
        commandsList.forEach(commands -> textList.add(commands.commandDesc));
        AlertUtils.showList(this, "选择要删除的命令", textList, new AlertUtils.callbacker() {
            @Override
            public void select(int id) {
                commandsList.remove(id);
                updatecmd();
            }

            @Override
            public void cancel() {

            }
        });
    }

    public void command_addseg(View view) {
        AlertUtils.showList(this, "选择命令", Arrays.asList(SELECT_COMMANDS), new AlertUtils.callbacker() {
            @Override
            public void select(int id) {
                commands cmd = new commands();
                String[] cmd_tst = SELECT_COMMANDS[id].split(":");
                cmd.command = cmd_tst[1];
                cmd.commandDesc = cmd_tst[0];
                AlertUtils.input(context, "输入参数", new AlertUtils.callbacker_Str() {
                    @Override
                    public void select(String val) {
                        if (val.equals("") && cmd.command.equals("D0")) {
                            val = curlock.D_SEC;
                        }
                        cmd.arg = val;
                        commandsList.add(cmd);
                        updatecmd();
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }

            @Override
            public void cancel() {

            }
        });
    }

    private void command_initseg() {
        command_initseg(null);
    }

    public void command_initseg(View view) {
        commandsList.clear();
        commands command = new commands();
        command.command = "D0";
        command.commandDesc = "SECKEY";
        command.arg = curlock.D_SEC;
        commandsList.add(command);
        updatecmd();
    }

    private String descDat() {
        StringBuilder sb = new StringBuilder();
        commandsList.forEach(commands -> {
            sb.append(commands.commandDesc).append("(").append(commands.arg).append(");");
        });
        return sb.toString();
    }

    private byte[] getDat() {
        List<Byte> byteList = new ArrayList<>();
        commandsList.forEach(commands -> {
            byteList.add(
                    (byte) (Byte.parseByte(commands.command.substring(0, 1), 16) * 16 +
                            Byte.parseByte(commands.command.substring(1, 2), 16))
            );
            if (byteList.size() == 1) {
                byteList.add((byte) 0);
            }
            byte[] tmpBytes = commands.arg.getBytes();
            for (byte tmpByte : tmpBytes) {
                byteList.add(tmpByte);
            }
        });
        byteList.set(1, (byte) byteList.size());
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }

    private void connect() {
        BleManager.getInstance().connect(curlock.D_Mac, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                res.setText("+开始链接\n");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                runOnUiThread(() -> ((TextView) context.findViewById(R.id.connective_info)).setText("连接失败"));
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                currentDevice = bleDevice;
                BleManager.getInstance().notify(bleDevice, curlock.D_SERV, curlock.D_CHAR.replace("6E400002", "6E400003"), new bleNotifyCallback());
                runOnUiThread(() -> ((TextView) context.findViewById(R.id.connective_info)).setText("连接成功"));
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                currentDevice = null;
                runOnUiThread(() -> ((TextView) context.findViewById(R.id.connective_info)).setText("连接断开"));
            }
        });
    }

    private void updatecmd() {
        ((TextView) findViewById(R.id.toSendCmd)).setText(descDat());
    }
}