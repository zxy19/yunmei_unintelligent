package cc.xypp.yunmeiui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import cc.xypp.yunmeiui.eneity.Lock;
import cc.xypp.yunmeiui.utils.LockManageUtil;
import cc.xypp.yunmeiui.utils.ToastUtil;

public class NfcActivity extends AppCompatActivity {
    String data = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NFCIntent(intent);
        } else if (intent.getAction().equals("cc.xypp.yunmeiui.nfc")) {
            data = intent.getDataString();
            Lock lock = new Lock(data);
            ((TextView)findViewById(R.id.nfc_tiptxt)).setText("准备写入名为 "+lock.label+" 的门锁ID\nUUID:"+lock.D_SERV);
        } else {
            data = "";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NFCIntent(intent);
        }
    }

    private void NFCIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (data.equals("")) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                LockManageUtil lockManageUtil = new LockManageUtil(this);
                List<Lock> lockList = lockManageUtil.getAll();
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                    for (NdefRecord record : messages[i].getRecords()) {
                        if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_URI)) {
                            byte[] payload = record.getPayload();
                            String url = new String(payload, 1, payload.length - 1, Charset.forName("UTF-8"));
                            if (url.startsWith("yunmeiui://lock_id/")) {
                                Lock lockid = new Lock(url);
                                for (Lock lock : lockList) {
                                    if (Objects.equals(lock.D_CHAR, lockid.D_CHAR) && Objects.equals(lock.D_SERV, lockid.D_SERV)) {
                                        Intent in = new Intent(this, MainActivity.class);
                                        in.setAction("cc.xypp.yunmeiui.unlock");
                                        in.setData(Uri.parse(lock.toString()));
                                        startActivity(in);
                                        finish();
                                        return;
                                    }
                                }
                                ((TextView)findViewById(R.id.nfc_tiptxt)).setText("该TAG需要名为 "+lockid.label+" 的门锁信息\nUUID:"+lockid.D_SERV+"\n请添加该门锁后再尝试");
                            }
                        }
                    }
                }
            }
        } else {
            NdefRecord[] ndefRecords = new NdefRecord[2];
            ndefRecords[0] = NdefRecord.createUri(data);
            ndefRecords[1] = NdefRecord.createApplicationRecord("cc.xypp.yunmeiui");
            NdefMessage ndefMessage = new NdefMessage(ndefRecords);
            writeNdefMessage(tag, ndefMessage);
        }
    }

    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage) {
        try {
            if (tag == null) {
                ToastUtil.show(this, "标签不可写！");
                return;
            }
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // 格式化标签并将消息写入标签
                formatTag(tag, ndefMessage);
            } else {
                ndef.connect();
                if (!ndef.isWritable()) {
                    ToastUtil.show(this, "标签不可写！");
                    ndef.close();
                    return;
                }
                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                ToastUtil.show(this, "成功！");
                finish();
            }
        } catch (Exception e) {
            ToastUtil.show(this, "未知错误");
        }
    }

    private void formatTag(Tag tag, NdefMessage ndefMessage) {
        try {
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            if (ndefFormatable == null) {
                ToastUtil.show(this, "标签不可写！");
                return;
            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
            ToastUtil.show(this, "成功");
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.show(this, "未知错误");
        }
    }

}