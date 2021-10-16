package com.myapps.calculator.models;

import java.math.BigDecimal;

public class MultipleAction extends Action {
    @Override
    public double equal(double d1, double d2) {
        return new BigDecimal(d1 + "").multiply(new BigDecimal(d2 + "")).doubleValue();
    }
}
