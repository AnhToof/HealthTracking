package com.g5team.healthtracking.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.g5team.healthtracking.R;

public class DiagnoseActivity extends AppCompatActivity {
    public static String TAG = "DiagnoseActivity";
    private TextView tvHeartRate, tvHRDiagnose, tvHRNutrition, tvBloodPressure, tvBPDiagnose, tvBPNutrition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);

        tvHeartRate = (TextView)findViewById(R.id.tv_hear_rate_diagnose);
        tvHRDiagnose = (TextView)findViewById(R.id.tv_diagnose_heart_rate);
        tvHRNutrition = (TextView)findViewById(R.id.tv_nutrition_heart_rate);
        tvBloodPressure = (TextView)findViewById(R.id.tv_blood_pressure_diagnose);
        tvBPDiagnose = (TextView)findViewById(R.id.tv_diagnose_blood_pressure);
        tvBPNutrition = (TextView)findViewById(R.id.tv_nutrition_blood_pressure);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("Values");
        if (bundle != null){
            tvHeartRate.setText(String.valueOf(bundle.getInt("heartRate")));
            tvBloodPressure.setText(bundle.getInt("systolicPressure") + "/" + bundle.getInt("diastolicPressure"));
            tvHRDiagnose.setText(bundle.getString("hrDiagnose") == null ? "Không có dữ liệu":bundle.getString("hrDiagnose"));
            tvHRNutrition.setText(bundle.getString("hrNutrition") == null ? "Không có dữ liệu":bundle.getString("hrNutrition"));
            tvBPDiagnose.setText(bundle.getString("bpDiagnose") == null ? "Không có dữ liệu":bundle.getString("bpDiagnose"));
            tvBPNutrition.setText(bundle.getString("bpNutrition") == null ? "Không có dữ liệu":bundle.getString("bpNutrition"));
        }else {
            Log.e(TAG, "Lỗi bundle");
        }

    }


}
