package com.example.myapptest.ui.stops_services;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapptest.MainActivity;
import com.example.myapptest.R;
import com.example.myapptest.data.busrouteinformation.ServiceInfo;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.naviagationdata.NavigationNodes;
import com.example.myapptest.data.naviagationdata.NavigationPartialResults;
import com.example.myapptest.data.naviagationdata.NavigationResults;
import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SingleServiceCustomAdapterRecyclerView extends RecyclerView.Adapter<SingleServiceCustomAdapterRecyclerView.MyViewHolder> {

    Context context;
    List<ServiceInfo> servicesList;

    NavController navController;

    public SingleServiceCustomAdapterRecyclerView(Context context, List<ServiceInfo> servicesList, NavController navController) {
        this.context = context;
        this.servicesList = servicesList;
        this.navController = navController;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.singleserviceview, parent, false);
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // set the data in item

        ServiceInfo currentService = servicesList.get(position);

        holder.serviceNum.setText(currentService.getServiceNum());
        holder.serviceDesc.setText(currentService.getServiceDesc());

        if (currentService.getServiceStatus() == 1) {
            holder.serviceBusIcon.setImageResource(R.drawable.ic_baseline_service_disruption_24);
        }

        // implement setOnClickListener event on item view.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Navigation.createNavigateOnClickListener(R.id.action_navigation_stops_services_master_to_stopsServicesSingleServiceSelectedFragment);
                StopsServicesMasterFragmentDirections.ActionNavigationStopsServicesMasterToStopsServicesSingleServiceSelectedFragment action =
                        StopsServicesMasterFragmentDirections.actionNavigationStopsServicesMasterToStopsServicesSingleServiceSelectedFragment(
                                currentService.getServiceNum(), currentService.getServiceDesc(), currentService.getServiceStatus(), currentService.getServiceFullRoute());
                navController.navigate(action);

                //TODO:  onclick stuff for recyclerview
                AppCompatActivity activity = (AppCompatActivity) view.getContext();

//                navController.navigate(R.id.);

            }
        });

    }


    @Override
    public int getItemCount() {
        return servicesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView serviceNum, serviceDesc;
        ImageView serviceBusIcon;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's

            serviceNum = itemView.findViewById(R.id.serviceNum);
            serviceDesc = itemView.findViewById(R.id.serviceDesc);
            serviceBusIcon = itemView.findViewById(R.id.imageViewServiceBus);

        }
    }



    public interface VolleyCallBack {
        void onSuccess(List<ServiceInStopDetails> busStopArrivalInfo);
        void onFailure();
    }
}