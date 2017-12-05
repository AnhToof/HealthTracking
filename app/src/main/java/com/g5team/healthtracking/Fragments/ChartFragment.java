package com.g5team.healthtracking.Fragments;


import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.g5team.healthtracking.Adapters.ChartDataAdapter;
import com.g5team.healthtracking.Models.ChartItem;
import com.g5team.healthtracking.Models.LineChartItem;
import com.g5team.healthtracking.Models.Result;
import com.g5team.healthtracking.R;
import com.g5team.healthtracking.Utils.AppConfig;
import com.g5team.healthtracking.Utils.AppController;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChartFragment extends Fragment {
    public static String TAG = "ChartFragment";
    private ArrayList<Result> results;
    private ProgressDialog progressDialog;
    private ArrayList<ChartItem> chartItems;
    private ListView listView;
    private ChartDataAdapter chartDataAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tv;
    public ChartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);


        listView = view.findViewById(R.id.listView1);
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        tv = view.findViewById(R.id.chart_notify);
        swipeRefreshLayout.setEnabled(false);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean scrollEnabled;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    scrollEnabled = true;
                }else {
                    scrollEnabled = false;
                }
                swipeRefreshLayout.setEnabled(scrollEnabled);
            }
        });
        progressDialog = new ProgressDialog(getContext(),
                R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllResults();
            }
        });

        getAllResults();


        return view;
    }

    private void showDialog(){
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Đang lấy dữ liệu");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    private void hideDialog(){
        progressDialog.dismiss();
    }
    private void getAllResults(){
        showDialog();
        listView.setVisibility(View.VISIBLE);
        tv.setVisibility(View.GONE);
        final StringRequest stringRequest = new StringRequest(Request.Method.GET,
                AppConfig.URL_RESULT,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "getAllResults onResponse: " + response.toString());

                        hideDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            results = new ArrayList<>();
                            if (jsonArray.length() > 0){
                                for(int i = 0; i< jsonArray.length(); i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    String heart_rate = object.getString("heart_rate");
                                    String blood_pressure = object.getString("blood_pressure");
                                    int op = blood_pressure.indexOf("/");
                                    String systolicPressure = blood_pressure.substring(0, op);
                                    String diastolicPressure = blood_pressure.substring(op + 1, blood_pressure.length());
                                    Result result = new Result(heart_rate, systolicPressure, diastolicPressure);
                                    results.add(result);

                                }
                                Collections.reverse(results);
                                chartItems = new ArrayList<>();
                                chartDataAdapter = new ChartDataAdapter(getContext(), chartItems);
                                chartItems.add(new LineChartItem(generateDataLineHR(), getContext()));
                                chartItems.add(new LineChartItem(generateDataLineBPSys(), getContext()));
                                chartItems.add(new LineChartItem(generateDataLineBPDias(), getContext()));
                                listView.setAdapter(chartDataAdapter);
                                swipeRefreshLayout.setRefreshing(false);
                                swipeRefreshLayout.setEnabled(false);
                            }else {
                                swipeRefreshLayout.setRefreshing(false);
                                listView.setVisibility(View.GONE);
                                tv.setVisibility(View.VISIBLE);

                            }


                        }catch (JSONException e){
                            Log.e(TAG, "getAllResults error" +  e.getMessage());
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                        }

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getAllResults onErrorResponse: " + error.getMessage());
                // hide the progress dialog
                hideDialog();
                listView.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Lỗi kết nối! Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
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

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return
     */
    private LineData generateDataLineHR() {

        ArrayList<Entry> e1 = new ArrayList<Entry>();

        for (int i = 0; i < results.size(); i++) {
            e1.add(new Entry(i, Integer.parseInt(results.get(i).getHeartRate())));
        }

        LineDataSet d1 = new LineDataSet(e1, "Nhịp tim ");
        d1.setLineWidth(2.5f);
        d1.setCircleRadius(4.5f);
        d1.setHighLightColor(Color.rgb(244, 117, 117));
        d1.setDrawValues(false);

        ArrayList<ILineDataSet> sets = new ArrayList<ILineDataSet>();
        sets.add(d1);

        LineData cd = new LineData(sets);
        return cd;
    }
    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return
     */
    private LineData generateDataLineBPSys() {

        ArrayList<Entry> e1 = new ArrayList<Entry>();

        for (int i = 0; i < results.size(); i++) {
            e1.add(new Entry(i, Integer.parseInt(results.get(i).getSystolicPressure())));
        }

        LineDataSet d1 = new LineDataSet(e1, "Huyết áp tâm thu ");
        d1.setLineWidth(2.5f);
        d1.setCircleRadius(4.5f);
        d1.setHighLightColor(Color.rgb(244, 117, 117));
        d1.setDrawValues(false);

        ArrayList<ILineDataSet> sets = new ArrayList<ILineDataSet>();
        sets.add(d1);

        LineData cd = new LineData(sets);
        return cd;
    }
    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return
     */
    private LineData generateDataLineBPDias() {

        ArrayList<Entry> e1 = new ArrayList<Entry>();

        for (int i = 0; i < results.size(); i++) {
            e1.add(new Entry(i, Integer.parseInt(results.get(i).getDiastolicPressure())));
        }

        LineDataSet d1 = new LineDataSet(e1, "Huyết áp tâm trương ");
        d1.setLineWidth(2.5f);
        d1.setCircleRadius(4.5f);
        d1.setHighLightColor(Color.rgb(244, 117, 117));
        d1.setDrawValues(false);

        ArrayList<ILineDataSet> sets = new ArrayList<ILineDataSet>();
        sets.add(d1);

        LineData cd = new LineData(sets);
        return cd;
    }



}
