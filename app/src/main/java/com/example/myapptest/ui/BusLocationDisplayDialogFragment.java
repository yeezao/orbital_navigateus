package com.example.myapptest.ui;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.myapptest.R;
import com.example.myapptest.data.NextbusAPIs;
import com.example.myapptest.data.busrouteinformation.BusLocationInfo;
import com.example.myapptest.data.busstopinformation.ArrivalNotifications;
import com.example.myapptest.ui.stops_services.SetArrivalNotificationsDialogFragment;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BusLocationDisplayDialogFragment extends DialogFragment implements OnMapReadyCallback {

    String serviceNumToCheck, stopNameString;
    public static String TAG = "BusLocationDisplayDialogFragment";

    private GoogleMap map;

    private final Handler handler = new Handler();

    private MapView mapView;

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

//
//        if (mapFragment == null) {
//            mapFragment = SupportMapFragment.newInstance();
//            mapFragment.getMapAsync(new OnMapReadyCallback() {
//                @Override
//                public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
//
//                    Log.e("entered", "onMapReady");
//
//                    mMap=googleMap;
//                    LatLng marker = new LatLng(1.289545, 103.849972);
//
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 13));
//
//                    mMap.addMarker(new MarkerOptions().title("Hello Google Maps!").position(marker));

//                    mMap = googleMap;
//                    CameraPosition googlePlex = CameraPosition.builder()
//                            .target(new LatLng(1.3840, 103.7470))
//                            .zoom(7)
//                            .bearing(0)
//                            .tilt(45)
//                            .build();
//
//                    LatLng latLng = new LatLng(1.289545, 103.849972);
//                    googleMap.addMarker(new MarkerOptions().position(latLng)
//                            .title("Singapore"));
//
//                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 500, null);
//
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            NextbusAPIs.callActiveBuses(serviceNumToCheck, getActivity(), getContext(), new NextbusAPIs.VolleyCallBackActiveBusList() {
//                                @Override
//                                public void onSuccessActiveBus(List<BusLocationInfo> busLocationInfoList) {
//                                    if (busLocationInfoList.size() > 0) {
//                                        for (int i = 0; i < busLocationInfoList.size(); i++) {
//                                            mMap.addMarker(new MarkerOptions()
//                                                    .position(busLocationInfoList.get(i).getBusLocation())
//                                                    .title(busLocationInfoList.get(i).getServicePlate())
//                                                    .icon(generateBitmapDescriptorFromRes(getContext(), R.drawable.ic_baseline_directions_bus_24))); // add the marker to Map
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onFailureActiveBus() {
//                                    //TODO; display failure message
//                                }
//                            });
//                            handler.postDelayed(this, 5000);
//                        }
//                    }, 0);
//                }
//            });
//        }

//
//        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapNearBy);
//        mapFragment.getMapAsync(BusLocationDisplayDialogFragment.this);


        return builder.create();

    }

    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        map = googleMap;
        CameraPosition googlePlex = CameraPosition.builder()
                .target(new LatLng(1.3840, 103.7470))
                .zoom(7)
                .bearing(0)
                .tilt(45)
                .build();

        map.addMarker(new MarkerOptions()
                        .position(new LatLng(1.3840, 103.7470))
                        .title("TEST"));

        map.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 500, null);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                NextbusAPIs.callActiveBuses(serviceNumToCheck, getActivity(), getContext(), new NextbusAPIs.VolleyCallBackActiveBusList() {
                    @Override
                    public void onSuccessActiveBus(List<BusLocationInfo> busLocationInfoList) {
                        if (busLocationInfoList.size() > 0) {
                            for (int i = 0; i < busLocationInfoList.size(); i++) {
                                map.addMarker(new MarkerOptions()
                                        .position(busLocationInfoList.get(i).getBusLocation())
                                        .title(busLocationInfoList.get(i).getServicePlate())
                                        .icon(generateBitmapDescriptorFromRes(getContext(), R.drawable.ic_baseline_directions_bus_24))); // add the marker to Map
                            }
                        }
                    }

                    @Override
                    public void onFailureActiveBus() {
                        //TODO; display failure message
                    }
                });
                handler.postDelayed(this, 5000);
            }
        }, 0);

    }

    private void pullBusLocationsOnline() {

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
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(@NonNull @NotNull DialogInterface dialog) {
        handler.removeCallbacksAndMessages(null);
        super.onCancel(dialog);
    }

    public static BusLocationDisplayDialogFragment newInstance(String serviceNum, String stopName) {
        BusLocationDisplayDialogFragment dialogFragment = new BusLocationDisplayDialogFragment();
        Bundle args = new Bundle();
        dialogFragment.setArguments(args);
        dialogFragment.setServiceNumToCheck(serviceNum);
        dialogFragment.setStopNameString(stopName);
        return dialogFragment;
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
