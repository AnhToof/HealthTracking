package com.g5team.healthtracking.Models;

/**
 * Created by Toof on 11/25/2017.
 */

public class HRDiagnose {
    private int fromIndex, toIndex, fromAge, toAge;
    private boolean sex;
    private String diagnose, nutrition;

    public HRDiagnose(int fromIndex, int toIndex, int fromAge, int toAge, boolean sex, String diagnose, String nutrition) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.fromAge = fromAge;
        this.toAge = toAge;
        this.sex = sex;
        this.diagnose = diagnose;
        this.nutrition = nutrition;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }

    public void setToIndex(int toIndex) {
        this.toIndex = toIndex;
    }

    public int getFromAge() {
        return fromAge;
    }

    public void setFromAge(int fromAge) {
        this.fromAge = fromAge;
    }

    public int getToAge() {
        return toAge;
    }

    public void setToAge(int toAge) {
        this.toAge = toAge;
    }

    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
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
