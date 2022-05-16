package cc.xypp.yunmei;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import cc.xypp.yunmei.utils.QRUtils;

public class QrActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        Intent i = getIntent();
        ((ImageView)findViewById(R.id.QRImg)).setImageBitmap(QRUtils.createQRCodeBitmap("yunmei://addlock/"+i.getDataString(),400,400));
    }
}