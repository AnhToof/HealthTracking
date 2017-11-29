package com.g5team.healthtracking.Models;

/**
 * Created by Toof on 11/25/2017.
 */

public class BPDiagnose {
    private int fromSystolic, toSystolic, fromDiastolic, toDiastolic;
    private boolean operator;
    private String diagnose, nutrition;

    public BPDiagnose(int fromSystolic, int toSystolic, int fromDiastolic, int toDiastolic, boolean operator, String diagnose, String nutrition) {
        this.fromSystolic = fromSystolic;
        this.toSystolic = toSystolic;
        this.fromDiastolic = fromDiastolic;
        this.toDiastolic = toDiastolic;
        this.operator = operator;
        this.diagnose = diagnose;
        this.nutrition = nutrition;
    }

    public int getFromSystolic() {
        return fromSystolic;
    }

    public void setFromSystolic(int fromSystolic) {
        this.fromSystolic = fromSystolic;
    }

    public int getToSystolic() {
        return toSystolic;
    }

    public void setToSystolic(int toSystolic) {
        this.toSystolic = toSystolic;
    }

    public int getFromDiastolic() {
        return fromDiastolic;
    }

    public void setFromDiastolic(int fromDiastolic) {
        this.fromDiastolic = fromDiastolic;
    }

    public int getToDiastolic() {
        return toDiastolic;
    }

    public void setToDiastolic(int toDiastolic) {
        this.toDiastolic = toDiastolic;
    }

    public boolean isOperator() {
        return operator;
    }

    public void setOperator(boolean operator) {
        this.operator = operator;
    }

    public String getDiagnose() {
        return diagnose;
    }

    public void setDiagnose(String diagnose) {
        this.diagnose = diagnose;
    }

    public String getNutrition() {
        return nutrition;
    }

    public void setNutrition(String nutrition) {
        this.nutrition = nutrition;
    }
}
