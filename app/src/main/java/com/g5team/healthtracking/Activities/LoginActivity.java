package com.g5team.healthtracking.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.g5team.healthtracking.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private ProgressDialog progressDialog;
    private Button btnLogin;
    private TextInputLayout inputEmail, inputPassword;
    private EditText etEmail, etPassword;
    private TextView tvLinkToRegister;
    String email, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();

        tvLinkToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initialize() {
        inputEmail = (TextInputLayout)findViewById(R.id.input_email);
        inputPassword = (TextInputLayout)findViewById(R.id.input_password);

        etEmail = (EditText)findViewById(R.id.et_username);
        etPassword = (EditText)findViewById(R.id.et_password);

        btnLogin = (Button)findViewById(R.id.btn_login);

        tvLinkToRegister = (TextView)findViewById(R.id.tv_link_to_register);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.Theme_AppCompat_DayNight_Dialog_Alert);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }
    private void showDialog(){
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
    }
    private void hideDialog(){
        progressDialog.dismiss();
    }
    private void login(){
        if (!validate()){
            onLoginFailed();
            return;
        }
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();
        checkLogin(email, password);
    }

    private void checkLogin(final String email, final String password) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String tag_string_req = "req_login";
        showDialog();
        //
        String url = "http://169.254.155.66/api/login";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.has("error")){
                        onLoginFailed();
                        Toast.makeText(getApplicationContext(), jsonObject.getString("error") , Toast.LENGTH_SHORT).show();
                    }else {
                        onLoginSuccess();
                        Toast.makeText(getApplicationContext(),"Logged in" , Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                hideDialog();
            }
        }) {

            @Override
            public  Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }

        };
        queue.add(strReq);

    }



    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Đăng nhập thất bại", Toast.LENGTH_LONG).show();

    }
    private void onLoginSuccess(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private boolean validate(){
        boolean valid = true;

        String username = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || username.contains(" ")){
            etEmail.setError("Vui lòng nhập tên đăng nhập hợp lệ");
            valid = false;
        }
        else etEmail    .setError(null);
        if (password.isEmpty() || password.length() < 6){
            etPassword.setText("Mật khẩu phải lớn hơn 6 ký tự");
            valid = false;
        }
        else etPassword.setError(null);
        return valid;
    }
}
