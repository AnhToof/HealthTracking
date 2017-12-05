package com.g5team.healthtracking.Models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.g5team.healthtracking.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.LineData;


/**
 * Created by Toof on 11/23/2017.
 */

public class LineChartItem extends ChartItem {


    public LineChartItem(ChartData<?> cd, Context c) {
        super(cd);
    }

    @Override
    public int getItemType() {
        return TYPE_LINECHART;
    }

    @Override
    public View getView(int position, View convertView, Context c) {

        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(c).inflate(
                    R.layout.list_item_linechart, null);
            holder.chart = convertView.findViewById(R.id.chart);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // apply styling
        // holder.chart.setValueTypeface(mTf);
        holder.chart.getDescription().setEnabled(false);
        holder.chart.setDrawGridBackground(false);

        XAxis xAxis = holder.chart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextSize(12);


        YAxis leftAxis = holder.chart.getAxisLeft();
        leftAxis.setLabelCount(5, false);
        leftAxis.setAxisMinimum(30f);
        leftAxis.setTextSize(12);

        YAxis rightAxis = holder.chart.getAxisRight();
        rightAxis.setEnabled(false);


        // set data
        holder.chart.setData((LineData) mChartData);
        mChartData.setDrawValues(true);
        mChartData.setValueTextSize(10);


        // do not forget to refresh the chart
        // holder.chart.invalidate();
        holder.chart.animateX(750);

        return convertView;
    }

    private static class ViewHolder {
        LineChart chart;
    }
}