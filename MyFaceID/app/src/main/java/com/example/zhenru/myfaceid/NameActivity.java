package com.example.zhenru.myfaceid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class NameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);
    }

    public void saveName(View view) {
        // Do something in response to button
        EditText nameInputView = findViewById(R.id.name);
        String nameInput = nameInputView.getText().toString();
        Intent intent = new Intent(NameActivity.this, RegisterActivity.class);
        intent.putExtra("NAME", nameInput);
        startActivity(intent);
        finish();
    }
}
