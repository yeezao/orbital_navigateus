//package com.example.myapptest.data.busstopinformation;
//
//import android.app.Activity;
//import android.util.Log;
//
//import com.android.volley.AuthFailureError;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class BusStopsList extends Activity {
//
//    ArrayList<StopDetails> listStops = new ArrayList<StopDetails>();
//
//    public void GetBusStopsList() {
//
//        String url = "https://nnextbus.nus.edu.sg/BusStops";
//        String auth = "Basic TlVTbmV4dGJ1czoxM2RMP3pZLDNmZVdSXiJU";
//
//        StringRequest stringRequest = new StringRequest
//                (Request.Method.GET, url, new Response.Listener<String>() {
//
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d("GetBusStopsList response is", response.toString());
//                        final ObjectMapper objectMapper = new ObjectMapper();
//                        try {
//                            listStops = objectMapper.readValue(response, new TypeReference<List<StopDetails>>() {
//                            });
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
////                    textView.setText("Response: " + response.toString());
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // TODO: Handle error
//                        Log.e("volley API error", "" + error);
//                    }
//
//
//                }) {
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("Content-Type", "application/json; charset=UTF-8");
//                params.put("Authorization", auth);
//                return params;
//            }
//        };
//
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(stringRequest);
//
//        return listStops;
//
//    }
//}
