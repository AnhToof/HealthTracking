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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.g5team.healthtracking.R;
import com.g5team.healthtracking.Utils.AppConfig;
import com.g5team.healthtracking.Utils.AppController;
import com.g5team.healthtracking.Utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private static long back_pressed_time;
    private static long PERIOD = 1000;
    boolean status = false;
    private ProgressDialog progressDialog;
    private Button btnLogin;
    private TextInputLayout inputEmail, inputPassword;
    private EditText etEmail, etPassword;
    private TextView tvLinkToRegister;
    private String email, password;
    private SessionManager session;
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

        session = new SessionManager(getApplicationContext());
        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            AppConfig.ACCESS_TOKEN = session.getKey();
            AppConfig.TOKEN_TYPE = session.getType();
            AppConfig.EMAIL = session.getEmail();
            AppConfig.FULLNAME = session.getName();
            AppConfig.TOKEN_TYPE = session.getType();
            AppConfig.REFRESH_TOKEN = session.getRefreshToken();
            AppConfig.DOB = session.getDob();
            AppConfig.WEIGTH = session.getWeight();
            AppConfig.HEIGHT = session.getHeight();
            AppConfig.AGE = session.getAge();
            AppConfig.SEX = session.getSex();
            AppConfig.FIRST  = session.getFirst();
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }
    private void showDialog(){
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Đang xác thực...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    private void hideDialog(){
        progressDialog.dismiss();
    }

    private void login(){
        if (!validate()){

            return;
        }
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();
        checkLogin(email, password);
    }

    //check login into server
    private void checkLogin(final String email, final String password) {

        showDialog();
        //



        final StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "LOGIN onResponse: " + response.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.has("error")){

                                String errorMsg = jsonObject.getString("message");
                                Toast.makeText(getApplicationContext(), "Sai Email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                            }else {
                                String access_token = jsonObject.getString("access_token");
                                String token_type = jsonObject.getString("token_type");
                                String refresh_token = jsonObject.getString("refresh_token");
                                checkActive(token_type, access_token, refresh_token);

                            }
                        }catch (JSONException e){
                            Log.e(TAG, "LOGIN error: " + e.getMessage());
                        }

                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "LOGIN onErrorResponse: " + error.getMessage());
                        // hide the progress dialog
                        hideDialog();
                        Toast.makeText(getApplicationContext(), "Sai Email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
            }){

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
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

        AppController.getInstance(getBaseContext()).addToRequestQueue(stringRequest);

    }

    //check user account which is active or not
    private void checkActive(final String token_type, final String access_token, final String refresh_token) {

         final StringRequest stringRequest = new StringRequest(Request.Method.GET,
                AppConfig.URL_SHOW,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "checkActive onResponse: " + response.toString());

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            AppConfig.EMAIL = email;
                            AppConfig.FULLNAME = jsonObject.getString("fullname");
                            AppConfig.SEX = jsonObject.getString("sex")=="0" ? false:true;
                            String dob = jsonObject.getString("dob");
                            AppConfig.DOB = dob;
                            int year = Integer.parseInt(AppConfig.DOB.substring(0, 4));
                            int month = Integer.parseInt(AppConfig.DOB.substring(5, 7));
                            int day = Integer.parseInt(AppConfig.DOB.substring(8, 10));

                            AppConfig.AGE = Integer.parseInt(getAge(year, month, day));
                            String s = jsonObject.getString("status");
                            if (s == "0"){
                                Toast.makeText(getApplicationContext(),
                                        "Tài khoản của bạn chưa được kích hoạt", Toast.LENGTH_LONG).show();
                            }

                            else{
                                AppConfig.ACCESS_TOKEN = access_token;
                                AppConfig.TOKEN_TYPE = token_type;
                                AppConfig.REFRESH_TOKEN = refresh_token;
                                session.setLogin(true);
                                session.setToken();
                                session.setProfile();
                                onLoginSuccess();
                            }

                        }catch (JSONException e){
                            Log.e(TAG, "check Active Json error onResponse" +  e.getMessage());
                        }

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "checkActive onErrorResponse: " + error.getMessage());
                // hide the progress dialog
                hideDialog();

                Toast.makeText(LoginActivity.this, "Lỗi kết nối! Vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        }){

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("Authorization", token_type+ " " +access_token);
                return headers;
            }

        };

        AppController.getInstance(getBaseContext()).addToRequestQueue(stringRequest);
    }

    //calculate age from date of birth
    private String getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }

    //called when values inputted that confirmed from server
    private void onLoginSuccess(){
        hideDialog();

        Toast.makeText(getApplicationContext(), "Bạn đã đăng nhập thành công", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    //validate input values
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
            etPassword.setError("Mật khẩu phải lớn hơn 6 ký tự");
            valid = false;
        }
        else etPassword.setError(null);
        return valid;
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
