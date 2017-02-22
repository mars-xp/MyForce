package com.xiang.batterytest;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xiang.batterytest.util.SystemUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button mForceStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        mForceStop = (Button)findViewById(R.id.id_btn_force);
        mForceStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vIntent = new Intent(MainActivity.this, MFScanActivity.class);
                startActivity(vIntent);
            }
        });
    }
}
