package com.myapps.calculator.models;

public abstract class Action {

    public Action() {

    }

    public static Action from(String symbol) {
        switch (symbol) {
            case "+":
                return new AddAction();
            case "*":
                return new MultipleAction();
            case "-":
                return new SubtractionAction();
            case "/":
                return new DivideAction();
            default:
                return null;
        }
    }

    public abstract double equal(double d1, double d2);

}
