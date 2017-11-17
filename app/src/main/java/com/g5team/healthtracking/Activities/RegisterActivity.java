package com.g5team.healthtracking.Activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.g5team.healthtracking.R;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private TextInputLayout inputUsername, inputPassword, inputPassword2, inputDob, inputFirstname, inputLastname;
    private EditText etUsername, etPassword, etPassword2, etDob, etFirstname, etLastname;
    private RadioGroup rdg;
    private RadioButton rdMale, rdFemale;
    private FloatingActionButton fbtnDob;
    private TextView tvLinkToLogin;
    private Calendar calendar;
    private Button btnRegister;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initialize();

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
                calendar = Calendar.getInstance();

                int year = calendar.get(calendar.YEAR);
                int month = calendar.get(calendar.MONTH);
                final int day = calendar.get(calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        etDob.setText(day + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
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
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
    }
    private void hidDialog(){
        progressDialog.dismiss();
    }
    private void signUp(){
        if (!validate()){
            onSignUpFalied();
            return;
        }

    }
    private void onSignUpFalied(){

    }
    private void onSignUpSuccess(){

    }

    private void initialize() {
        inputUsername = (TextInputLayout)findViewById(R.id.input_username_reg);
        inputPassword = (TextInputLayout)findViewById(R.id.input_password_reg);
        inputPassword2 = (TextInputLayout)findViewById(R.id.input_password2_reg);
        inputFirstname = (TextInputLayout)findViewById(R.id.input_firstname_reg);
        inputLastname = (TextInputLayout)findViewById(R.id.input_lastname_reg);
        inputDob = (TextInputLayout)findViewById(R.id.input_dob_reg);

        etUsername = (EditText)findViewById(R.id.et_username_reg);
        etPassword = (EditText)findViewById(R.id.et_password_reg);
        etPassword2 = (EditText)findViewById(R.id.et_password2_reg);
        etFirstname = (EditText)findViewById(R.id.et_firstname_reg);
        etLastname = (EditText)findViewById(R.id.et_lastname_reg);
        etDob = (EditText)findViewById(R.id.et_dob_reg);

        fbtnDob = (FloatingActionButton)findViewById(R.id.fbtn_dob);

        rdg = (RadioGroup)findViewById(R.id.rdg);
        rdMale = (RadioButton)findViewById(R.id.rd_male);
        rdFemale = (RadioButton)findViewById(R.id.rd_female);

        tvLinkToLogin = (TextView)findViewById(R.id.tv_link_to_login);

        btnRegister = (Button)findViewById(R.id.btn_register);

        etDob.setEnabled(false);
    }
    private boolean validate(){
        boolean valid = true;

        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        String password2 = etPassword2.getText().toString();
        String firstname = etFirstname.getText().toString();
        String lastname = etLastname.getText().toString();
        String dob = etDob.getText().toString();

        if (username.isEmpty() || username.contains(" ")){
            etUsername.setError("Sai tên đăng nhập");
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
        if (firstname.isEmpty()){
            etFirstname.setError("Vui lòng nhập Họ hợp lệ");
            valid = false;
        }else etFirstname.setError(null);
        if (lastname.isEmpty()){
            etLastname.setError("Vui lòng nhập Tên hợp lệ");
            valid = false;
        }else etLastname.setError(null);
        if (dob.isEmpty()){
            etDob.setError("Vui lòng chọn Ngày Sinh");
            valid = false;
        }else etDob.setError(null);
        return valid;
    }


}
