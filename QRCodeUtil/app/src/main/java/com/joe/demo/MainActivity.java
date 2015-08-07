package com.joe.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.client.android.BarCodeUtil;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.LocalPictureUtil;
import com.joe.com.R;

public class MainActivity extends AppCompatActivity {

    private TextView resultTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTextview = (TextView) findViewById(R.id.resultTxtView);

        findViewById(R.id.scanbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 0xf0);
            }
        });

        findViewById(R.id.readpicbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalPictureUtil.getLocalPic(MainActivity.this, 0xee);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == CaptureActivity.DECODERESULT) {
            String result = data.getStringExtra(CaptureActivity.RESULTKEY);
            resultTextview.setText(result);
        }
        if (requestCode == 0xee) {
            String codePath = LocalPictureUtil.receiveIntent(this, data);
            System.out.println("joe=========path" + codePath);
            String result = BarCodeUtil.decodeQRImage(codePath);
            System.out.println("joe=-========result" + result);
            resultTextview.setText(result);
        }
    }
}
