package com.g5team.healthtracking.Fragments;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.g5team.healthtracking.Activities.DiagnoseActivity;
import com.g5team.healthtracking.R;
import com.g5team.healthtracking.Utils.AppConfig;
import com.g5team.healthtracking.Utils.AppController;
import com.g5team.healthtracking.Utils.ImageProcessing;
import com.g5team.healthtracking.Utils.SessionManager;
import com.g5team.healthtracking.Views.CircleProgressBar;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeasureFragment extends Fragment implements SurfaceHolder.Callback{

    private static final String TAG = "MeasureFragment";

    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static View image = null;
    private static PowerManager.WakeLock wakeLock = null;
    private static int averageIndex = 0;
    private static int beatsIndex = 0;
    private static double beats = 0;
    private static long startTime = 0;
    private static int beatsAvg = 0;
    private static TextView tvResultHR = null;
    private static TextView tvResultBP = null;
    private static EditText etWeigth;
    private static EditText etHeigth;
    private static TYPE currentType = TYPE.DARK;
    private String hrDiagnose, hrNutrition, bpDiagnose, bpNutrition, weight, height;
    private int systolicPressure, diastolicPressure;
    private boolean finish = false;
    private Button btnDiagnose;
    private FloatingActionButton fab, fabGuide;
    private SessionManager session;
    private ConstraintLayout constraintLayout;
    private ProgressDialog progressDialog;
    private CircleProgressBar circleProgressBar;
    private TapTargetSequence targetSequence;
    public MeasureFragment() {
        // Required empty public constructor

    }

    public static TYPE getCurrent() {
        return currentType;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_measure, container, false);

        tvResultHR = view.findViewById(R.id.tv_result_hr);
        tvResultBP = view.findViewById(R.id.tv_result_bp);
        image = view.findViewById(R.id.image);
        constraintLayout = view.findViewById(R.id.constraint_result);
        fab = view.findViewById(R.id.floatingActionButton);
        fabGuide = view.findViewById(R.id.btn_floating);
        btnDiagnose = view.findViewById(R.id.btn_diagnose);
        session = new SessionManager(getContext());

        progressDialog = new ProgressDialog(getContext(),
                R.style.Theme_AppCompat_DayNight_Dialog_Alert);

        circleProgressBar = view.findViewById(R.id.customProgressBar);


        preview = view.findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(this);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");

        //event for get guide use app
        fabGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGuide(v.getRootView());
                targetSequence.start();
            }
        });

        //event for diagnose
        btnDiagnose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDiagnoseHR();

            }
        });

        //event for set height weight
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View mView = inflater.inflate(R.layout.custom_dialog_wh, null);
                etHeigth = mView.findViewById(R.id.et_height);
                etWeigth = mView.findViewById(R.id.et_weigth);
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.MyAlertDialogStyle);
                builder.setTitle("Nhập các chỉ số");
                builder.setView(mView);
                builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                final AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (validateHW() == true){
                            AppConfig.WEIGTH = Integer.parseInt(weight);
                            AppConfig.HEIGHT = Integer.parseInt(height);
                            session.setWH();
                            dialog.dismiss();

                        }
                    }
                });

            }
        });


        //Event for measure
        circleProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] permissionsRequired = new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WAKE_LOCK};
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WAKE_LOCK) ==
                                PackageManager.PERMISSION_DENIED) {
                    requestPermissions(permissionsRequired, 0);
                } else {
                    if (AppConfig.HEIGHT == 0 || AppConfig.WEIGTH == 0){
                        final View mView = inflater.inflate(R.layout.custom_dialog_wh, null);
                        etHeigth = mView.findViewById(R.id.et_height);
                        etWeigth = mView.findViewById(R.id.et_weigth);
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.MyAlertDialogStyle);
                        builder.setTitle("Nhập các chỉ số");
                        builder.setView(mView);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (validateHW() == true){
                                    AppConfig.WEIGTH = Integer.parseInt(weight);
                                    AppConfig.HEIGHT = Integer.parseInt(height);
                                    session.setWH();
                                    dialog.dismiss();
                                }

                            }
                        });

                        Toast.makeText(getContext(), "Vui lòng nhập chiều cao và cân nặng", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if (finish == false){
                            preview.setVisibility(View.VISIBLE);
                            constraintLayout.setVisibility(View.GONE);

                            new CountDownTimer(500, 1000) {
                               public void onTick(long millisUntilFinished) {
                                }
                                public void onFinish() {
                                    doInProcess();
                                }
                           }.start();
                        }
                    }
                }
            }
        });

        return view;
    }
    private boolean validateHW(){
        boolean valid = true;
        weight = etWeigth.getText().toString();
        height = etHeigth.getText().toString();
        if (weight.isEmpty()){
            etWeigth.setError("Cân nặng không được để trống");
            valid = false;
        }else etWeigth.setError(null);
        if (height.isEmpty()){
            etHeigth.setError("Chiều cao không được để trống");
            valid = false;
        }else etHeigth.setError(null);

        return valid;
    }

    //guide use app
    private void setGuide(final View view) {
        targetSequence = new TapTargetSequence(getActivity())
                .targets(
                        TapTarget.forView(view.findViewById(R.id.floatingActionButton), "Nhập chỉ số cơ thể", "Để chúng tôi có thể đo chính xác, bạn phải nhập chính xác chiều cao và cân nặng")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(30)
                                .outerCircleColor(R.color.orange)
                                .targetCircleColorInt(Color.RED)
                                .outerCircleAlpha(0.96f)
                                .descriptionTextColor(R.color.white)
                                .transparentTarget(true)
                                .targetRadius(40),
                        TapTarget.forView(view.findViewById(R.id.customProgressBar), "Đo", "Đặt nhẹ ngón tay vào máy ảnh phía sau. Đảm bảo máy ảnh bị che phủ và một phần đèn flash bị che phủ. Không di chuyển ngón tay của bạn cho đến khi phép đo hoàn thành")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(30)
                                .outerCircleAlpha(0.96f)
                                .outerCircleColor(R.color.red)
                                .descriptionTextColor(R.color.white)
                                .transparentTarget(true)
                                .targetRadius(90),
                        TapTarget.forView(view.findViewById(R.id.tab_layout), "Chuyển trang", "Trượt để chuyển qua lại giữa trang Đo và trang Biểu Đồ")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(30)
                                .outerCircleAlpha(0.96f)
                                .titleTextColor(R.color.red)
                                .descriptionTextColor(R.color.black)
                                .transparentTarget(true)
                                .targetRadius(100),
                        TapTarget.forView(view.findViewById(R.id.btn_floating), "Hướng dẫn", "Khi bạn muốn xem lại hướng dẫn")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(30)
                                .outerCircleAlpha(0.96f)
                                .titleTextColor(R.color.orange)
                                .descriptionTextColor(R.color.black)
                                .transparentTarget(true)
                                .targetRadius(40)

                )
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        // Yay
                        setShowCasePreview(view);

                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Boo
                    }
                });

    }

    private void setShowCasePreview(final View view) {
        preview.setVisibility(View.VISIBLE);
        TapTargetView.showFor(getActivity(),
                TapTarget.forView(view.findViewById(R.id.preview), "Màn hình", "Nơi ghi nhận hình ảnh ngón tay")
                        .outerCircleAlpha(0.96f)
                        .drawShadow(true)
                        .titleTextSize(30)
                        .cancelable(false)
                        .targetRadius(150)
                        .transparentTarget(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView v) {
                        super.onTargetClick(v);      // This call is optional
                        preview.setVisibility(View.GONE);
                        setShowCaseDiagnose(view);

                    }
                }
        );
    }

    private void setShowCaseDiagnose(final View view) {
        constraintLayout.setVisibility(View.VISIBLE);
        TapTargetSequence tapTargetSequence = new TapTargetSequence(getActivity())
                .targets(
                        TapTarget.forView(view.findViewById(R.id.tv_result_hr), "Nhịp tim", "Kết quả về chỉ số nhịp tim sẽ hiện thị ở đây")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(30)
                                .outerCircleColor(R.color.orange)
                                .targetCircleColorInt(Color.RED)
                                .outerCircleAlpha(0.96f)
                                .descriptionTextColor(R.color.white)
                                .transparentTarget(true)
                                .targetRadius(50),
                        TapTarget.forView(view.findViewById(R.id.tv_result_bp), "Huyết áp", "Kết quả về chỉ số nhịp tim sẽ hiện thị ở đây")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(30)
                                .outerCircleAlpha(0.96f)
                                .outerCircleColor(R.color.red)
                                .descriptionTextColor(R.color.white)
                                .transparentTarget(true)
                                .targetRadius(55),
                        TapTarget.forView(view.findViewById(R.id.btn_diagnose), "Chẩn đoán", "Dùng để lấy chẩn đoán từ kết quả đo của bạn")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(30)
                                .outerCircleAlpha(0.96f)
                                .titleTextColor(R.color.red)
                                .descriptionTextColor(R.color.black)
                                .transparentTarget(true)
                                .targetRadius(60)

                )
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        // Yay
                        constraintLayout.setVisibility(View.GONE);
                        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                                .setTitle("Chúc mừng")
                                .setMessage("Bạn đã hoàn thành hướng dẫn")
                                .setPositiveButton("OK", null).show();

                        new CountDownTimer(1000, 1000) {
                            public void onTick(long millisUntilFinished) {
                            }

                            public void onFinish() {
                                TapTargetView.showFor(dialog,
                                        TapTarget.forView(dialog.getButton(DialogInterface.BUTTON_POSITIVE), "OK", "Nhấn vào đây để hoàn tất hướng dẫn")
                                                .transparentTarget(true)
                                                .outerCircleAlpha(0.5f)
                                                .drawShadow(true)
                                                .cancelable(false)
                                                .tintTarget(true)
                                                .outerCircleColor(R.color.orange)
                                                .targetCircleColorInt(Color.RED)
                                                .descriptionTextColor(R.color.white)
                                                .titleTextSize(30), new TapTargetView.Listener() {
                                            @Override
                                            public void onTargetClick(TapTargetView view) {
                                                super.onTargetClick(view);
                                                dialog.dismiss();
                                            }
                                        });
                                if (tvResultBP.getText().toString() == "0")
                                    constraintLayout.setVisibility(View.VISIBLE);
                            }
                        }.start();


                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Boo
                    }
                });
        tapTargetSequence.start();
    }

    //set blood pressure value based on heart rate value
    public void setBloodPressure(double heartRate) {
        double R = 18.5; // Average R = 18.31; // Vascular resistance // Very hard to calculate from person to person
        double Q = AppConfig.SEX == true ? 5 : 4.5; // Liters per minute of blood through heart
        double ejectionTime = 364.5 - 1.23 * heartRate; //for not laying down
        double bodySurfaceArea = 0.007184 * (Math.pow(AppConfig.WEIGTH, 0.425)) * (Math.pow(AppConfig.HEIGHT, 0.725));
        double strokeVolume = -6.6 + 0.25 * (ejectionTime - 35) - 0.62 * heartRate + 40.4 * bodySurfaceArea - 0.51 * AppConfig.AGE; // Volume of blood pumped from heart in one ic_beat
        double pulsePressure = Math.abs(strokeVolume / ((0.013 * AppConfig.WEIGTH - 0.007 * AppConfig.AGE - 0.004 * heartRate) + 1.307));
        double meanPulsePressure = Q * R;

        systolicPressure = (int) (meanPulsePressure + (3 / 2 * pulsePressure));
        diastolicPressure = (int) (meanPulsePressure - (pulsePressure / 3));
        tvResultBP.setText(systolicPressure + "/" + diastolicPressure);


    }

    //measure heart rate through camera and flash light.
    private void doInProcess(){
        finish = true;

        camera = Camera.open();
        startTime = System.currentTimeMillis();
        circleProgressBar.setProgressWithAnimation();
        Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

            @Override
            public void onPreviewFrame(byte[] data, Camera cam) {
                if (data == null) throw new NullPointerException();
                Camera.Size size = cam.getParameters().getPreviewSize();
                if (size == null) throw new NullPointerException();

                if (!processing.compareAndSet(false, true)) return;

                int width = size.width;
                int height = size.height;

                int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);

                if (imgAvg == 0 || imgAvg == 255) {
                    processing.set(false);
                    return;
                }

                int averageArrayAvg = 0;
                int averageArrayCnt = 0;
                for (int i = 0; i < averageArray.length; i++) {
                    if (averageArray[i] > 0) {
                        averageArrayAvg += averageArray[i];
                        averageArrayCnt++;
                    }
                }

                int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
                TYPE newType = currentType;
                if (imgAvg < rollingAverage) {
                    newType = TYPE.RED;
                    if (newType != currentType) {
                        beats++;

                    }
                } else if (imgAvg > rollingAverage) {
                    newType = TYPE.DARK;
                }

                if (averageIndex == averageArraySize) averageIndex = 0;
                averageArray[averageIndex] = imgAvg;

                averageIndex++;

                // Transitioned from one state to another to the same
                if (newType != currentType) {
                    currentType = newType;
                }

                long endTime = System.currentTimeMillis();
                double totalTimeInSecs = (endTime - startTime) / 1000d;
                if (totalTimeInSecs >= 20) {
                    double bps = (beats / totalTimeInSecs);
                    int dpm = (int) (bps * 60d);
                    if (dpm < 30 || dpm > 180) {
                        startTime = System.currentTimeMillis();
                        beats = 0;
                        processing.set(false);
                        circleProgressBar.setProgress(0);
                        Toast.makeText(getContext(), "Đo thất bại", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (beatsIndex == beatsArraySize) beatsIndex = 0;
                    beatsArray[beatsIndex] = dpm;
                    beatsIndex++;

                    int beatsArrayAvg = 0;
                    int beatsArrayCnt = 0;
                    for (int i = 0; i < beatsArray.length; i++) {
                        if (beatsArray[i] > 0) {
                            beatsArrayAvg += beatsArray[i];
                            beatsArrayCnt++;
                        }
                    }
                    beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                    tvResultHR.setText(String.valueOf(beatsAvg));
                    setBloodPressure(beatsAvg);
                    startTime = System.currentTimeMillis();
                    beats = 0;
                }
                processing.set(false);
            }
        };

        try {

            camera.setPreviewDisplay(previewHolder);
            camera.setPreviewCallback(previewCallback);


        } catch (Throwable t) {
            Log.e("surfaceCallback", "Exception in setPreviewDisplay()", t);
        }

        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
        camera.startPreview();
        new CountDownTimer(20000, 10){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                wakeLock = null;
                camera.setPreviewCallback(null);
                camera.release();
                camera = null;
                finish = false;
                if (beatsAvg != 0){
                    preview.setVisibility(View.GONE);
                    constraintLayout.setVisibility(View.VISIBLE);
                    saveResult();
                }else {
                    circleProgressBar.setProgress(0);
                    Toast.makeText(getContext(), "Đo thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        }.start();

    }

    private void showDialog(String message){
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideDialog(){
        progressDialog.dismiss();
    }

    //event save result after measure and send to server
    private void saveResult() {
        showDialog("Đang lưu kết quả");
        //
        final StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_RESULT,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "SaveResult onResponse: " + response.toString());
                        hideDialog();

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "SaveResult onErrorResponse: " + error.getMessage());
                // hide the progress dialog
                hideDialog();
                Toast.makeText(getContext(), "Lỗi kết nối! Vui lòng thử lại", Toast.LENGTH_SHORT).show();
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
                params.put("heart_rate", String.valueOf(beatsAvg));
                params.put("blood_pressure", (systolicPressure + "/" + diastolicPressure));
                return params;
            }

        };

        AppController.getInstance(getContext()).addToRequestQueue(stringRequest);

    }

    //get diagnose heart rate from server
    private void onDiagnoseHR() {

        showDialog("Đang chẩn đoán");
        final StringRequest stringRequest = new StringRequest(Request.Method.GET,
                AppConfig.URL_HRDIAGNOSE,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "onDiagnoseHR onResponse: " + response.toString());
                        try{
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int from_index = Integer.parseInt(jsonObject.getString("from_index"));
                                int to_index = Integer.parseInt(jsonObject.getString("to_index"));
                                int from_age = Integer.parseInt(jsonObject.getString("from_age"));
                                int to_age = Integer.parseInt(jsonObject.getString("to_age"));
                                String sex = jsonObject.getString("sex");
                                String diagnose = jsonObject.getString("diagnose");
                                String nutrition = jsonObject.getString("nutrition");
                                if ((sex.equals("1") ? true : false == AppConfig.SEX)
                                        &&(beatsAvg >= from_index && beatsAvg <= to_index)
                                        && (AppConfig.AGE >= from_age && AppConfig.AGE <= to_age)){
                                    hrDiagnose = diagnose;
                                    hrNutrition = nutrition;
                                    break;
                                }


                            }


                            onDiagnoseBP();
                        }catch (JSONException e){
                            Log.e(TAG, "onDiagnoseHR jsonError" +  e.getMessage());
                        }
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onDiagnoseHR onErrorResponse: " + error.getMessage());
                // hide the progress dialog
                Toast.makeText(getContext(), "Lỗi kết nối! Vui lòng thử lại", Toast.LENGTH_SHORT).show();
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

        };

        AppController.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    //get diagnose blood pressure from server
    private void onDiagnoseBP(){
        final StringRequest stringRequest = new StringRequest(Request.Method.GET,
                AppConfig.URL_BPDIAGNOSE,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "onDiagnoseBP onResponse: " + response.toString());

                        hideDialog();
                        try{
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int from_systolic = Integer.parseInt(jsonObject.getString("from_systolic"));
                                int to_systolic = Integer.parseInt(jsonObject.getString("to_systolic"));
                                boolean operator = jsonObject.getString("operator").equals("0")? true:false;
                                int from_diastolic = Integer.parseInt(jsonObject.getString("from_diastolic"));
                                int to_diastolic = Integer.parseInt(jsonObject.getString("to_diastolic"));
                                String diagnose = jsonObject.getString("diagnose");
                                String nutrition = jsonObject.getString("nutrition");
                                if (systolicPressure >= from_systolic && systolicPressure <= to_systolic){
                                    if (operator == false){
                                        bpDiagnose = diagnose;
                                        bpNutrition = nutrition;
                                        break;
                                    }else {
                                        if (diastolicPressure >= from_diastolic && diastolicPressure <= to_diastolic){
                                            bpDiagnose = diagnose;
                                            bpNutrition = nutrition;
                                            break;
                                        }
                                    }
                                }
                                if (diastolicPressure >= from_diastolic && diastolicPressure <= to_diastolic){
                                    if (operator == false){
                                        bpDiagnose = diagnose;
                                        bpNutrition = nutrition;
                                        break;
                                    }else {
                                        if (diastolicPressure >= from_systolic && diastolicPressure <= to_systolic){
                                            bpDiagnose = diagnose;
                                            bpNutrition = nutrition;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (hrNutrition.isEmpty() && hrDiagnose.toString().isEmpty()
                                    && bpNutrition.toString().isEmpty() && bpDiagnose.toString().isEmpty()){
                                Toast.makeText(getContext(), "Không có dữ liệu để so sánh với kết quả của bạn", Toast.LENGTH_SHORT).show();
                            }else {
                                Bundle bundle = new Bundle();
                                bundle.putInt("heartRate", beatsAvg);
                                bundle.putInt("systolicPressure", systolicPressure);
                                bundle.putInt("diastolicPressure", diastolicPressure);
                                bundle.putString("hrDiagnose", hrDiagnose);
                                bundle.putString("hrNutrition", hrNutrition);
                                bundle.putString("bpDiagnose", bpDiagnose);
                                bundle.putString("bpNutrition", bpNutrition);
                                Intent intent = new Intent(getActivity(), DiagnoseActivity.class);
                                intent.putExtra("Values", bundle);
                                startActivity(intent);
                            }
                        }catch (JSONException e){
                            Log.e(TAG, "onDiagnoseBP jsonError" +  e.getMessage());
                        }

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onDiagnoseBP onErrorResponse: " + error.getMessage());
                // hide the progress dialog
                Toast.makeText(getContext(), "Lỗi kết nối! Vui lòng thử lại", Toast.LENGTH_SHORT).show();
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

        };

        AppController.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public static enum TYPE {
        DARK, RED
    }


}