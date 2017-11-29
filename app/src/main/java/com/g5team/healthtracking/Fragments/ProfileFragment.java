package com.g5team.healthtracking.Fragments;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    public static String TAG = "ProfileFragment";
    private Button btnUpdate, btnChangePassword;
    private EditText etEmail, etFullname, etDob;
    private RadioGroup rdg;
    private RadioButton rdMale, rdFemale;
    private ProgressDialog progressDialog;
    private String email, fullname, dob, sex;
    private String currentPassword, newPassword, newPasswordConfirmation;
    private FloatingActionButton fbtnDob;
    private SessionManager session;
    private EditText etCurrentPassword, etNewPassword, etNewPasswordConfirmation;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        getActivity().setTitle("Thông tin cá nhân");

        session = new SessionManager(getContext());
        //init
        btnUpdate = view.findViewById(R.id.btn_update);
        btnChangePassword = view.findViewById(R.id.btn_change_password);

        etEmail = view.findViewById(R.id.et_username_tt);
        etFullname = view.findViewById(R.id.et_fullname_tt);
        etDob = view.findViewById(R.id.et_dob_tt);

        rdg = view.findViewById(R.id.rdg_tt);
        rdMale = view.findViewById(R.id.rd_male_tt);
        rdFemale = view.findViewById(R.id.rd_female_tt);
        fbtnDob = view.findViewById(R.id.fbtn_dob_tt);

        etEmail.setText(AppConfig.EMAIL);
        etFullname.setText(AppConfig.FULLNAME);
        etDob.setText(AppConfig.DOB);
        progressDialog = new ProgressDialog(getContext(),
                R.style.Theme_AppCompat_DayNight_Dialog_Alert);

        if (AppConfig.SEX == false) {
            rdFemale.setChecked(true);
            rdMale.setChecked(false);
        }
        else {
            rdMale.setChecked(true);
            rdFemale.setChecked(false);
        }
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString();
                fullname = etFullname.getText().toString();
                dob = etDob.getText().toString();
                sex = rdFemale.isChecked()?"0":"1";
                update();
            }
        });
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View mView = inflater.inflate(R.layout.custom_dialog_change_password, null);
                etCurrentPassword = mView.findViewById(R.id.et_current_password);
                etNewPassword = mView.findViewById(R.id.et_new_password);
                etNewPasswordConfirmation = mView.findViewById(R.id.et_new_password_confirmation);
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Nhập các chỉ số");
                builder.setView(mView);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (validateChangePassword() == false)
                            return;
                        currentPassword = etCurrentPassword.getText().toString();
                        changePassword();

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        fbtnDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        etDob.setText(year + "-" + ((month + 1)<10?"0"+(month + 1):(month + 1))
                                + "-" + (dayOfMonth<10?"0"+dayOfMonth:dayOfMonth));
                    }
                }, calendar.get(calendar.YEAR), calendar.get(calendar.MONTH), calendar.get(calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        return view;
    }
    private boolean validateChangePassword(){
        boolean valid = true;
        newPassword = etNewPassword.getText().toString();
        newPasswordConfirmation = etNewPasswordConfirmation.getText().toString();

        if (newPassword.length() < 6){
            etNewPassword.setError("Mật khẩu phải lớn hơn hoặc bằng 6 ký tự");
            valid = false;
        }else etNewPassword.setError(null);
        if (!newPasswordConfirmation.equals(newPassword)){
            etNewPasswordConfirmation.setError("Mật khẩu không khớp");
            valid = false;
        }else etNewPasswordConfirmation.setError(null);
        return valid;
    }

    private void showDialog(String massage){
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage(massage);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    private void hideDialog(){
        progressDialog.dismiss();
    }
    private void update() {
        showDialog("Updating...");
        final StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_EDIT,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "UPDATE onResponse: " + response.toString());
                        hideDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String e = jsonObject.getString("email");
                            String f = jsonObject.getString("fullname");
                            String d = jsonObject.getString("dob");
                            String s = jsonObject.getString("sex");

                            AppConfig.EMAIL = e;
                            AppConfig.FULLNAME = f;
                            AppConfig.SEX = s=="0"?false:true;
                            AppConfig.DOB = d;
                            int year = Integer.parseInt(AppConfig.DOB.substring(0, 4));
                            int month = Integer.parseInt(AppConfig.DOB.substring(5, 7));
                            int day = Integer.parseInt(AppConfig.DOB.substring(8, 10));
                            AppConfig.AGE = Integer.parseInt(getAge(year, month, day));
                            session.setProfile();
                            Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                        }catch (JSONException e){
                            Log.e(TAG, "UPDATE error: " + e.getMessage());
                        }

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "UPDATE onErrorResponse: " + error.getMessage());
                // hide the progress dialog
                hideDialog();
            }
        }){

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("Authorization", AppConfig.TOKEN_TYPE+ " " +AppConfig.ACCESS_TOKEN);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("fullname", fullname);
                params.put("dob", dob);
                params.put("sex", sex);
                return params;
            }

        };

        AppController.getInstance(getContext()).addToRequestQueue(stringRequest);
    }
    private void changePassword(){
        showDialog("Updating...");
        final StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_CHANGE_PASS,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "UPDATE PASSWORD onResponse: " + response.toString());
                        hideDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.has("error")){
                                etCurrentPassword.setError("Mật khẩu hiện tại không đúng");
                            }else {
                                Toast.makeText(getContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            }

                        }catch (JSONException e){
                            Log.e(TAG, "UPDATE PASSWORD error: " + e.getMessage());
                        }

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "UPDATE PASSWORD onErrorResponse: " + error.getMessage());
                // hide the progress dialog
                hideDialog();
            }
        }){

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("Authorization", AppConfig.TOKEN_TYPE+ " " +AppConfig.ACCESS_TOKEN);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("current_password", currentPassword);
                params.put("password", newPassword);
                return params;
            }

        };

        AppController.getInstance(getContext()).addToRequestQueue(stringRequest);
    }
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
}
