package com.g5team.healthtracking.Fragments;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.g5team.healthtracking.R;
import com.g5team.healthtracking.Utils.ImageProcessing;
import com.g5team.healthtracking.Utils.Settings;
import com.g5team.healthtracking.Views.CircleProgressBar;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements SurfaceHolder.Callback{
    private static final String TAG = "HomeFragment";
    private static final AtomicBoolean processing = new AtomicBoolean(false);

    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static View image = null;
    private static TextView tvResultHB = null;
    private static TextView tvResultBP = null;
    private static PowerManager.WakeLock wakeLock = null;
    private static String beatsPerMinuteValue="";
    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];
    private static int beatsIndex = 0;
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;
    private static int beatsAvg = 0;

    private static EditText etWeigth;
    private static EditText etHeigth;

    public static enum TYPE {
        DARK, RED
    };

    private static TYPE currentType = TYPE.DARK;

    public static TYPE getCurrent() {
        return currentType;
    }

    private CircleProgressBar circleProgressBar;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        tvResultHB = (TextView)view.findViewById(R.id.tv_result_hb);
        tvResultBP = (TextView)view.findViewById(R.id.tv_result_bp);
        image = view.findViewById(R.id.image);

        preview = (SurfaceView) view.findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(this);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");


        circleProgressBar = (CircleProgressBar) view.findViewById(R.id.customProgressBar);

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
                    final View mView = inflater.inflate(R.layout.custom_dialog, null);
                    etHeigth = (EditText)mView.findViewById(R.id.et_height);
                    etWeigth = (EditText)mView.findViewById(R.id.et_weigth);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Nhập các chỉ số");
                    builder.setView(mView);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String weigth = etWeigth.getText().toString();
                            String heigth = etHeigth.getText().toString();
                            if (weigth.isEmpty() || heigth.isEmpty()){
                                Toast.makeText(getActivity(), "Vui Lòng Nhập đầy đủ chiều cao và cân nặng", Toast.LENGTH_SHORT);
                            }
                            else {
                                Settings.WEIGTH = Integer.parseInt(weigth);
                                Settings.HEIGTH = Integer.parseInt(heigth);
                                doInProcess();
                            }

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();

                }




            }
        });

        return view;
    }

    public void setBloodPressure(double heartRate) {
        double R = 18.5; // Average R = 18.31; // Vascular resistance // Very hard to calculate from person to person
        double Q = Settings.SEX.equalsIgnoreCase("Male") ? 5 : 4.5; // Liters per minute of blood through heart
        double ejectionTime = 364.5 - 1.23 * heartRate; //for not laying down
        double bodySurfaceArea = 0.007184 * (Math.pow(Settings.WEIGTH, 0.425)) * (Math.pow(Settings.HEIGTH, 0.725));
        double strokeVolume = -6.6 + 0.25 * (ejectionTime - 35) - 0.62 * heartRate + 40.4 * bodySurfaceArea - 0.51 * Settings.AGE; // Volume of blood pumped from heart in one beat
        double pulsePressure = Math.abs(strokeVolume / ((0.013 * Settings.HEIGTH - 0.007 * Settings.AGE - 0.004 * heartRate) + 1.307));
        double meanPulsePressure = Q * R;

        int systolicPressure = (int) (meanPulsePressure + 4.5 / 3 * pulsePressure);
        int diastolicPressure = (int) (meanPulsePressure - pulsePressure / 3);
        tvResultBP.setText(systolicPressure + "/" + diastolicPressure);


    }

    private void doInProcess(){

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
                // Log.i(TAG, "imgAvg="+imgAvg);
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
                        // Log.d(TAG, "BEAT!! beats="+beats);
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
                   // image.postInvalidate();
                }

                long endTime = System.currentTimeMillis();
                double totalTimeInSecs = (endTime - startTime) / 1000d;
                if (totalTimeInSecs >= 10) {
                    double bps = (beats / totalTimeInSecs);
                    int dpm = (int) (bps * 60d);
                    if (dpm < 30 || dpm > 180) {
                        startTime = System.currentTimeMillis();
                        beats = 0;
                        processing.set(false);
                        return;
                    }

                    // Log.d(TAG,
                    // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);

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
                    tvResultHB.setText(String.valueOf(beatsAvg));
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
        new CountDownTimer(10000, 10){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                wakeLock = null;
                camera.setPreviewCallback(null);
                camera.release();
                camera = null;
            }
        }.start();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (circleProgressBar.isFinish())
        {
            wakeLock.release();
            wakeLock = null;
            camera.setPreviewCallback(null);
            camera.getParameters().setFlashMode(null);
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }





    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }

        return result;
    }
}
