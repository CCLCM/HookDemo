package com.ccl.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            HookUtils.hookIActivityManager();
            HookUtils.hookActivityThreadHandler();
        } catch (Exception e) {
            Log.d("chencl_",e.toString());
            e.printStackTrace();
        }
        startActivity(new Intent(MainActivity.this, GoalActivity.class));
    }

}