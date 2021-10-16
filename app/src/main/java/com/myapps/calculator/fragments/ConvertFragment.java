package com.myapps.calculator.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.myapps.calculator.R;
import com.myapps.calculator.models.MultipleAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ConvertFragment extends BottomSheetDialogFragment {

    public static final String TAG = "CONVERT_FRAGMENT";

    private RequestQueue queue;

    public ConvertFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_convert, container, false);

        TextInputEditText fromText = view.findViewById(R.id.fromText);
        TextInputEditText toText = view.findViewById(R.id.toText);

        Spinner fromSpinner = view.findViewById(R.id.fromSpinner);
        Spinner toSpinner = view.findViewById(R.id.toSpinner);


        String url = "https://free.currconv.com/api/v7/currencies?apiKey=" + getActivity().getResources().getString(R.string.key);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {

                        try {

                            JSONObject jsonResponse = new JSONObject(response.toString());
                            JSONObject results = jsonResponse.getJSONObject("results");

                            List<String> currencies = new ArrayList<>();

                            for (Iterator<String> it = results.keys(); it.hasNext();) {
                                currencies.add(results.getJSONObject(it.next()).getString("id"));
                            }

                            Collections.sort(currencies);

                            ArrayAdapter<String> mySpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, currencies);

                            fromSpinner.setAdapter(mySpinnerAdapter);
                            toSpinner.setAdapter(mySpinnerAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        error.printStackTrace();
                    }
                });

        jsonObjectRequest.setTag(ConvertFragment.TAG);
        jsonObjectRequest.setShouldCache(true);
        queue.add(jsonObjectRequest);


        Button convertButton = view.findViewById(R.id.convertButton);

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

                if (connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected()) {
                    Toast.makeText(getContext(), getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (fromText.getText().toString().isEmpty()) {
                    fromText.setText("0.0");
                }

                if (fromText.getText().toString().startsWith(".")) {
                    fromText.setText("0" + fromText.getText());
                }

                if (fromText.getText().toString().endsWith(".")) {
                    fromText.setText(fromText.getText() + "0");
                }

                RequestQueue queue = Volley.newRequestQueue(getContext());
                String url = "https://free.currconv.com/api/v7/convert?q=" + fromSpinner.getSelectedItem() + "_" + toSpinner.getSelectedItem() + "&compact=y&apiKey=" + getActivity().getResources().getString(R.string.key);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener() {
                            @Override
                            public void onResponse(Object response) {

                                try {

                                    JSONObject jsonResponse = new JSONObject(response.toString());

                                    double rate = Double.parseDouble(jsonResponse.getJSONObject(fromSpinner.getSelectedItem() + "_" + toSpinner.getSelectedItem()).getString("val"));
                                    double fromValue = Double.parseDouble(fromText.getText() + "");

                                    toText.setText(new MultipleAction().equal(rate, fromValue) + "");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                error.printStackTrace();
                            }
                        });

                jsonObjectRequest.setTag(ConvertFragment.TAG);
                jsonObjectRequest.setShouldCache(true);
                queue.add(jsonObjectRequest);
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        queue.cancelAll(ConvertFragment.TAG);
    }
}
