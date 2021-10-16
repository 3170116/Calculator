package com.myapps.calculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.myapps.calculator.fragments.ConvertFragment;
import com.myapps.calculator.models.Action;
import com.myapps.calculator.models.MultipleAction;

public class MainActivity extends AppCompatActivity {

    private String rest = "";
    private String current = "";
    private String action = "";

    private TextView restText;
    private TextView currentText;
    private TextView actionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restText = findViewById(R.id.restText);
        currentText = findViewById(R.id.currentText);
        actionText = findViewById(R.id.actionText);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.transformMenu:
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                        if (connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected()) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        ConvertFragment convertFragment = new ConvertFragment();
                        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet));

                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        convertFragment.show(getSupportFragmentManager(), ConvertFragment.TAG);

                        break;
                    case R.id.refreshMenu:
                        rest = action = current = "";

                        displayNumber(rest, restText);
                        displayNumber(action, actionText);
                        displayNumber(current, currentText);

                        break;
                    default:
                        break;
                }

                return false;
            }
        });
    }


    public void addNumber(View view) {
        //Αφού πατήσουμε '='
        if (!rest.isEmpty() && action.isEmpty()) {
            return;
        }

        String digit = ((Button) view).getText().toString();

        if (digit.equals(",") && current.contains(",")) {
            return;
        }

        if (digit.equals(",") && current.isEmpty() && action.equals("/")) {
            return;
        }

        if (digit.equals(",") && (current.isEmpty() || current.equals("-"))) {
            current = current.isEmpty() ? "0," : "-0,";
            displayNumber(current, currentText);
            return;
        }

        if (digit.equals("0") && (current.equals("0") || current.equals("-0"))) {
            return;
        }

        if (digit.equals("0") && action.equals("/") && current.isEmpty()) {
            return;
        }

        current = current + digit;
        displayNumber(current, currentText);
    }

    public void action(View view) {
        //Ειδική μέριμνα για αρνητικούς αριθμούς
        if (((Button) view).getText().toString().equals("-") && current.isEmpty()) {
            current = "-" + current;
            displayNumber(current, currentText);
            return;
        }

        if (!action.isEmpty() || (current.isEmpty() && rest.isEmpty()) || current.equals("-") || current.endsWith(",")) {
            return;
        }

        action = ((Button) view).getText().toString();
        displayNumber(action, actionText);

        //Μόνο την πρώτη φορά που δεν υπάρχει υπόλοιπο.
        if (rest.isEmpty()) {
            rest = current;
            displayNumber(rest, restText);
        }

        current = "";
        displayNumber(current, currentText);
    }

    public void clear(View view) {
        current = "";
        displayNumber(current, currentText);
    }

    public void equal(View view) {
        if (rest.isEmpty() || action.isEmpty() || current.isEmpty() || current.endsWith(",")) {
            return;
        }

        //Ειδική μέριμνα για διαίρεση
        if (action.equals("/") && toDouble(current) == 0.0) {
            return;
        }

        double d1 = toDouble(rest);
        double d2 = toDouble(current);

        Action a = Action.from(action);

        rest = a.equal(d1, d2) == (int) a.equal(d1, d2) ? (a.equal(d1, d2) + "").substring(0, (a.equal(d1, d2) + "").indexOf('.')) : (a.equal(d1, d2) + "").replace(".", ",");
        action = "";
        current = "";

        displayNumber(rest, restText);
        displayNumber(action, actionText);
        displayNumber(current, currentText);
    }


    private double toDouble(String number) {
        double result = 0;
        int prefix = number.startsWith("-") ? -1 : 1;

        if (number.startsWith("-")) {
            number = number.substring(1);
        }

        int commaIdx = number.indexOf(',');

        if (commaIdx != -1) {
            result = Double.parseDouble(number.substring(0, commaIdx).replace(".", ""));
            result += Double.parseDouble(number.substring(commaIdx + 1)) / Math.pow(10, number.substring(commaIdx + 1).length());
        } else {
            result = Double.parseDouble(number.replace(".", ""));
        }

        return new MultipleAction().equal(prefix, result);
    }

    private void displayNumber(String number, TextView textView) {
        String displayedNumbered = "";
        int idx = 0;

        //Αν είναι αρνητικός, αφαιρούμε από μπροστά το '-'
        boolean isNegative = false;
        if (number.startsWith("-")) {
            isNegative = true;
            number = number.substring(1);
        }

        for (int i = number.length() - 1; i >= 0; i--) {
            //Πριν την υποδιαστολή δε κάνουμε κάτι
            if (number.substring(0, i + 1).contains(",")) {
                displayedNumbered = number.charAt(i) + displayedNumbered;
            } else {
                idx += 1;
                //Μετά την υποδυαστολή ανά 3 προσθέτουμε '.'
                if (idx - 1 > 0 && (idx - 1) % 3 == 0) {
                    displayedNumbered = number.charAt(i) + "." + displayedNumbered;
                } else {
                    displayedNumbered = number.charAt(i) + displayedNumbered;
                }
            }
        }

        if (isNegative) {
            displayedNumbered = "-" + displayedNumbered;
        }

        textView.setText(displayedNumbered);
    }
}