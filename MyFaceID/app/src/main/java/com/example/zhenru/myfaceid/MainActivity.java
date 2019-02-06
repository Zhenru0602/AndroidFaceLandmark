package com.example.zhenru.myfaceid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendMessage(View view) {
        // Do something in response to button
        final TextView textView=(TextView)findViewById(R.id.displayText);
        textView.setText("开始识别");
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("STATUS", "Recognition");
        startActivity(intent);
        startActivity(intent);
        finish();
    }

    public void register(View view) {
        // Do something in response to button
        final TextView textView=(TextView)findViewById(R.id.displayText);
        textView.setText("开始注册");
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("STATUS", "Register");
        startActivity(intent);
        finish();
    }
}
