package com.doublefree.navigateus.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.doublefree.navigateus.R;
import com.doublefree.navigateus.data.NextbusAPIs;
import com.doublefree.navigateus.data.busrouteinformation.BusLocationInfo;
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;
import com.doublefree.navigateus.data.busstopinformation.StopList;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BusLocationDisplayDialogFragment extends DialogFragment implements OnMapReadyCallback {

    String serviceNumToCheck;
    String stopNameString;
    String fullServiceNumToCheck;
    StopList busStop = new StopList();
    public static String TAG = "BusLocationDisplayDialogFragment";

    private GoogleMap map;

    private final Handler handler = new Handler();
    private final Handler newHandler = new Handler();

    private MapView mapView;
    
    TextView serviceNum, serviceFirstArrival, serviceSecondArrival;
    ImageView serviceFirstArrivalLive, serviceSecondArrivalLive;
    ProgressBar loadingStopInfoProgressBar;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setRetainInstance(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.bus_location_display_dialogfragment, null);

        builder.setView(view).setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.removeCallbacksAndMessages(null);
                newHandler.removeCallbacksAndMessages(null);
            }
        });

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);
//        map.getUiSettings().setMyLocationButtonEnabled(false);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
//        map.animateCamera(cameraUpdate);

        TextView stopName = view.findViewById(R.id.busLocationStopName);
        stopName.setText(stopNameString);
        
        serviceNum = view.findViewById(R.id.list_child);
        serviceNum.setText(fullServiceNumToCheck);

        serviceFirstArrival = view.findViewById(R.id.list_child_timing1);
        serviceSecondArrival = view.findViewById(R.id.list_child_timing2);
        serviceFirstArrivalLive = view.findViewById(R.id.live_timing_imageview);
        serviceSecondArrivalLive = view.findViewById(R.id.live_timing_imageview_2);
        loadingStopInfoProgressBar = view.findViewById(R.id.singleServiceMapProgressBar);

        newHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("newhandler", "running");
                setArrivalTimings();
                newHandler.postDelayed(this, 10000);
            }
        }, 0);

        return builder.create();

    }

    /**
     * Method to pull arrival time data from NextBus servers and display it on the dialog.
     * This method is similar to the {@link StopsMainAdapter#getChildView(int, int, boolean, View, ViewGroup)} method.
     *
     */
    private void setArrivalTimings() {

        NextbusAPIs.callSingleStopInfo(getActivity(), getContext(), busStop.getStopId(), 0, true, new NextbusAPIs.VolleyCallBackSingleStop() {
            @Override
            public void onSuccessSingleStop(List<ServiceInStopDetails> servicesAllInfoAtStop) {
                Log.e("re call", "bus stop list");
                for (int i = 0; i < servicesAllInfoAtStop.size(); i++) {
                    if (servicesAllInfoAtStop.get(i).getServiceNum().equals(fullServiceNumToCheck)) {
                        ServiceInStopDetails serviceInStopDetails = servicesAllInfoAtStop.get(i);
                        if (serviceInStopDetails.getFirstArrivalLive() != null) {
                            if (serviceInStopDetails.getFirstArrival().charAt(0) == '-') {
                                serviceFirstArrival.setText("No Service");
                                serviceFirstArrival.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                                serviceFirstArrival.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                                serviceFirstArrival.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
                                serviceFirstArrivalLive.setVisibility(ImageView.INVISIBLE);
//                textViewTime1Live.setText("");
//                textViewTime1Live.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
                            } else {
                                serviceFirstArrival.setText(serviceInStopDetails.getFirstArrival());
                                serviceFirstArrival.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                                if (serviceInStopDetails.getFirstArrivalLive().length() == 0) {
                                    serviceFirstArrival.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                                    serviceFirstArrival.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                                    serviceFirstArrivalLive.setVisibility(ImageView.INVISIBLE);
//                    textViewTime1Live.setText("");
//                    textViewTime1Live.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
                                } else {
                                    if ((serviceInStopDetails.getFirstArrival().length() == 1 && serviceInStopDetails.getFirstArrival().contains("1")) || serviceInStopDetails.getFirstArrival().contains("Arr")) {
                                        serviceFirstArrival.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.NUS_Blue));
                                        serviceFirstArrival.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                    } else {
                                        serviceFirstArrival.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                                        serviceFirstArrival.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                                    }
                                    serviceFirstArrivalLive.setVisibility(ImageView.VISIBLE);
//                    textViewTime1Live.setText("LIVE");
//                    textViewTime1Live.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
//                    textViewTime1Live.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
                                }
                            }



//            TextView serviceSecondArrivalLive = convertView.findViewById(R.id.list_serviceInStopDetails_timing2_live);
                            if (serviceInStopDetails.getFirstArrival().charAt(0) == '-') {
                                serviceSecondArrival.setText("");
                                serviceSecondArrival.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                                serviceSecondArrival.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                                serviceSecondArrivalLive.setVisibility(ImageView.INVISIBLE);
//                serviceSecondArrivalLive.setText("");
//                serviceSecondArrivalLive.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
                            } else if (serviceInStopDetails.getSecondArrival().charAt(0) == '-') {
                                serviceSecondArrival.setText("< LAST BUS");
                                serviceSecondArrival.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                                serviceSecondArrival.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
                                serviceSecondArrival.setTextSize(14);
                                serviceSecondArrivalLive.setVisibility(ImageView.INVISIBLE);
                            } else {
                                serviceSecondArrival.setText(serviceInStopDetails.getSecondArrival());
                                if (serviceInStopDetails.getSecondArrivalLive().length() == 0) {
                                    serviceSecondArrival.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                                    serviceSecondArrival.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                                    serviceSecondArrivalLive.setVisibility(ImageView.INVISIBLE);
//                    serviceSecondArrivalLive.setText("");
//                    serviceSecondArrivalLive.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
                                } else {
                                    if ((serviceInStopDetails.getSecondArrival().length() == 1 && serviceInStopDetails.getSecondArrival().contains("1")) || serviceInStopDetails.getSecondArrival().contains("Arr")) {
                                        serviceSecondArrival.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.NUS_Blue));
                                        serviceSecondArrival.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                    } else {
                                        serviceSecondArrival.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                                        serviceSecondArrival.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                                    }
                                    serviceSecondArrivalLive.setVisibility(ImageView.VISIBLE);
//                    serviceSecondArrivalLive.setText("LIVE");
//                    serviceSecondArrivalLive.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
//                    serviceSecondArrivalLive.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
                                }
                            }
                        }
                        serviceFirstArrival.setVisibility(View.VISIBLE);
                        serviceSecondArrival.setVisibility(View.VISIBLE);
                        loadingStopInfoProgressBar.setVisibility(View.GONE);
                        break;
                    }
                }
            }

            @Override
            public void onFailureSingleStop() {

            }
        });
        
    }

    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        map = googleMap;
        CameraPosition googlePlex = CameraPosition.builder()
                .target(new LatLng(busStop.getStopLatitude(), busStop.getStopLongitude()))
                .zoom((float) 14.9999998)
                .bearing(0)
                .tilt(0)
                .build();

//        map.addMarker(new MarkerOptions()
//                        .position(new LatLng(1.3840, 103.7470))
//                        .title("TEST"));

        map.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1, null);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("handler", "running");
                NextbusAPIs.callActiveBuses(serviceNumToCheck, getActivity(), getContext(), new NextbusAPIs.VolleyCallBackActiveBusList() {
                    @Override
                    public void onSuccessActiveBus(List<BusLocationInfo> busLocationInfoList) {
                        map.clear();
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(busStop.getStopLatitude(), busStop.getStopLongitude()))
                                .title(busStop.getStopName()));
                        if (busLocationInfoList.size() > 0) {
                            for (int i = 0; i < busLocationInfoList.size(); i++) {
                                map.addMarker(new MarkerOptions()
                                        .position(busLocationInfoList.get(i).getBusLocation())
                                        .title(busLocationInfoList.get(i).getServicePlate())
                                        .icon(generateBitmapDescriptorFromRes(
                                                getContext(), R.drawable.ic_baseline_directions_bus_36_large))); // add bus markers to map
                            }
                        }
                    }

                    @Override
                    public void onFailureActiveBus() {
                        //TODO; display failure message
                    }
                });
                handler.postDelayed(this, 5000); //refresh every 5sec
            }
        }, 0);

    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        handler.removeCallbacksAndMessages(null);
        newHandler.removeCallbacksAndMessages(null);
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(@NonNull @NotNull DialogInterface dialog) {
        handler.removeCallbacksAndMessages(null);
        newHandler.removeCallbacksAndMessages(null);
        super.onCancel(dialog);
    }

    public static BusLocationDisplayDialogFragment newInstance(String serviceNum, String stopName, StopList busStop) {
        BusLocationDisplayDialogFragment dialogFragment = new BusLocationDisplayDialogFragment();
        Bundle args = new Bundle();
        dialogFragment.setArguments(args);
        if (serviceNum.contains("D1")) {
            dialogFragment.setServiceNumToCheck("D1");
        } else if (serviceNum.contains("C")) {
            dialogFragment.setServiceNumToCheck("C");
        } else {
            dialogFragment.setServiceNumToCheck(serviceNum);
        }
        dialogFragment.setFullServiceNumToCheck(serviceNum);
        dialogFragment.setStopNameString(stopName);
        dialogFragment.setBusStop(busStop);
        return dialogFragment;
    }
    
    public void setFullServiceNumToCheck(String fullServiceNumToCheck) {
        this.fullServiceNumToCheck = fullServiceNumToCheck;
    }

    public void setBusStop(StopList busStop) {
        this.busStop = busStop;
    }

    public void setServiceNumToCheck(String serviceNumToCheck) {
        this.serviceNumToCheck = serviceNumToCheck;
    }

    public void setStopNameString(String stopNameString) {
        this.stopNameString = stopNameString;
    }

    //helper method to add marker as image onto map
    public static BitmapDescriptor generateBitmapDescriptorFromRes(
            Context context, int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable.setBounds(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
