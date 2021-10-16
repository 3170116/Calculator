package com.myapps.calculator.models;

public class AddAction extends Action {
    @Override
    public double equal(double d1, double d2) {
        return d1 + d2;
    }
}
