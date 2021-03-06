package com.g5team.healthtracking.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.g5team.healthtracking.R;

public class SplashActivity extends AppCompatActivity{
    private static final String TAG = "SplashActivity";
    private static long back_pressed_time;
    private static long PERIOD = 1000;
    private Button btnLogin, btnRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initialize();
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void initialize(){
        btnLogin = (Button) findViewById(R.id.btn_login_sp);
        btnRegister = (Button) findViewById(R.id.btn_register_sp);
        String[] permissionsRequired = new String[]{Manifest.permission.INTERNET};
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) ==
                        PackageManager.PERMISSION_DENIED) {
            requestPermissions(permissionsRequired, 0);
        }
    }

    @Override
    public void onBackPressed() {
        if (back_pressed_time + PERIOD > System.currentTimeMillis()) {
            System.exit(0);
        } else
            Toast.makeText(getBaseContext(), "Nhấn trở lại lần nữa để thoát!", Toast.LENGTH_SHORT).show();
        back_pressed_time = System.currentTimeMillis();
    }
}
