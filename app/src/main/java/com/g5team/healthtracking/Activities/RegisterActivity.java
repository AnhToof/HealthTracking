package com.g5team.healthtracking.Activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private static long back_pressed_time;
    private static long PERIOD = 1000;

    private EditText etUsername, etPassword, etPassword2, etDob, etFullname;
    private RadioGroup rdg;
    private RadioButton rdMale, rdFemale;
    private FloatingActionButton fbtnDob;
    private TextView tvLinkToLogin;
    private Button btnRegister;
    private ProgressDialog progressDialog;
    private SessionManager session;
    private String email, password, fullname, dob, sex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initialize();
        session = new SessionManager(getApplicationContext());
        tvLinkToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        fbtnDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        etDob.setText(year + "-" + ((month + 1)<10?"0"+(month + 1):(month + 1))
                                + "-" + (dayOfMonth<10?"0"+dayOfMonth:dayOfMonth));
                    }
                }, calendar.get(calendar.YEAR), calendar.get(calendar.MONTH), calendar.get(calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }
    private void showDialog(){
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Đang đăng ký...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    private void hideDialog(){
        progressDialog.dismiss();
    }
    private void signUp(){
        if (!validate()){
            return;
        }
        Log.e("CHECK", email+ " " +
                password + " " +
                fullname + " " +
                dob + " " +
                sex );
        checkSignUp();

    }

    private void checkSignUp(){
        showDialog();
        final StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "SignUp onResponse: " + response.toString());
                        hideDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.has("token_type")){
                                session.setLogin(false);
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                builder.setTitle("Thông báo");
                                builder.setMessage("Thông tin tài khoản của bạn đã được gửi đến ban quản trị. " +
                                        "Vui lòng quay lại khi tài khoản được cấp phép");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();

                            }else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                builder.setTitle("Thông báo");
                                builder.setMessage("Tài khoản đã tồn tại");
                                builder.setPositiveButton("Đăng nhập", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        }catch (JSONException e){
                            Log.e(TAG, "SignUp error: " + e.getMessage());
                        }

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "SignUp onErrorResponse: " + error.getMessage());
                // hide the progress dialog
                hideDialog();
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối! Vui lòng thử lại", Toast.LENGTH_SHORT).show();
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
                params.put("fullname", fullname);
                params.put("dob", dob);
                params.put("sex", sex);
                return params;
            }

        };

        AppController.getInstance(getBaseContext()).addToRequestQueue(stringRequest);

    }
    private void initialize() {

        etUsername = (EditText)findViewById(R.id.et_username_reg);
        etPassword = (EditText)findViewById(R.id.et_password_reg);
        etPassword2 = (EditText)findViewById(R.id.et_password2_reg);
        etFullname = (EditText)findViewById(R.id.et_fullname_reg);
        etDob = (EditText)findViewById(R.id.et_dob_reg);

        fbtnDob = (FloatingActionButton)findViewById(R.id.fbtn_dob);

        rdg = (RadioGroup)findViewById(R.id.rdg);
        rdMale = (RadioButton)findViewById(R.id.rd_male);
        rdFemale = (RadioButton)findViewById(R.id.rd_female);

        tvLinkToLogin = (TextView)findViewById(R.id.tv_link_to_login);

        btnRegister = (Button)findViewById(R.id.btn_register);
        progressDialog = new ProgressDialog(RegisterActivity.this,
                R.style.Theme_AppCompat_DayNight_Dialog_Alert);

        etDob.setEnabled(false);
    }
    private boolean validate(){
        boolean valid = true;

        email = etUsername.getText().toString();
        password = etPassword.getText().toString();
        String password2 = etPassword2.getText().toString();;
        fullname = etFullname.getText().toString();
        dob = etDob.getText().toString();
        sex = rdMale.isChecked()? "1":"0";
        if (email.isEmpty() || email.contains(" ")){
            etUsername.setError("Email không hợp lệ");
            valid = false;
        }else etUsername.setError(null);
        if (password.length() < 6){
            etPassword.setError("Mật khẩu phải có 6 ký tự trở lên");
            valid = false;
        }else etPassword.setError(null);
        if (!password2.equals(password)){
            etPassword2.setError("Mẩu khẩu không khớp");
            valid = false;
        }else etPassword2.setError(null);
        if (fullname.isEmpty()){
            etFullname.setError("Vui lòng nhập Họ v Tên hợp lệ");
            valid = false;
        }else etFullname.setError(null);
        if (dob.isEmpty()){
            etDob.setError("Vui lòng chọn Ngày Sinh");
            valid = false;
        }else etDob.setError(null);
        if (rdg.getCheckedRadioButtonId()==-1){
            rdMale.setError("Vui lòng chọn giới tính");
            valid = false;
        }else {
            rdMale.setError(null);
        }
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
