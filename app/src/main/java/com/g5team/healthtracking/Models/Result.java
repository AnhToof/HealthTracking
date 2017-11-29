package com.g5team.healthtracking.Models;

/**
 * Created by Toof on 11/21/2017.
 */

public class Result {

    private String heartRate, systolicPressure, diastolicPressure;

    public Result(String heartRate, String systolicPressure, String diastolicPressure) {
        this.heartRate = heartRate;
        this.systolicPressure = systolicPressure;
        this.diastolicPressure = diastolicPressure;
    }

    public String getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(String heart_rate) {
        this.heartRate = heart_rate;
    }

    public String getSystolicPressure() {
        return systolicPressure;
    }

    public void setSystolicPressure(String systolicPressure) {
        this.systolicPressure = systolicPressure;
    }

    public String getDiastolicPressure() {
        return diastolicPressure;
    }

    public void setDiastolicPressure(String diastolicPressure) {
        this.diastolicPressure = diastolicPressure;
    }
}
