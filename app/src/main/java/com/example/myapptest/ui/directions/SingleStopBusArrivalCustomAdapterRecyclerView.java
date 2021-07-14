package com.example.myapptest.ui.directions;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapptest.R;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopArrivalInfoForDirections;
import com.example.myapptest.data.naviagationdata.NavigationNodes;
import com.example.myapptest.data.naviagationdata.NavigationPartialResults;
import com.example.myapptest.data.naviagationdata.NavigationResults;
import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleStopBusArrivalCustomAdapterRecyclerView extends RecyclerView.Adapter<SingleStopBusArrivalCustomAdapterRecyclerView.MyViewHolder> {

    Context context;
    Activity activity;
    NavigationNodes node;
    List<NavigationPartialResults> navResultInSegments;
    String origin, dest;

    List<StopArrivalInfoForDirections> stopArrivalInfoForDirections;

    View view;

    FragmentManager childFragmentManager;
    NavigationPartialResults currentSegment;
    boolean checkViableServices1 = true;

    public SingleStopBusArrivalCustomAdapterRecyclerView(Activity activity, Context context, NavigationNodes node, List<StopArrivalInfoForDirections> stopArrivalInfoForDirections) {
        this.context = context;
        this.activity = activity;
        this.node = node;
        this.stopArrivalInfoForDirections = stopArrivalInfoForDirections;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.singlebusarrivinginrecyclerview, parent, false);
        view = v;
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // set the data in items

        if (stopArrivalInfoForDirections.size() == 0) {
            holder.noService.setVisibility(View.VISIBLE);
            holder.serviceNum.setVisibility(View.GONE);
            holder.serviceDesc.setVisibility(View.GONE);
            holder.arrivalTime.setVisibility(View.GONE);
            return;
        }

        StopArrivalInfoForDirections serviceToDisplay = stopArrivalInfoForDirections.get(position);

        holder.serviceNum.setText(serviceToDisplay.getService());
        holder.serviceDesc.setText(serviceToDisplay.getServiceDesc());
        StringBuilder stringBuilder = new StringBuilder();
        if (serviceToDisplay.getArrivalTime() == 0) {
            stringBuilder.append("Arr");
        } else {
            stringBuilder.append(serviceToDisplay.getArrivalTime()).append(" min");
        }
        holder.arrivalTime.setText(stringBuilder.toString());
        if (serviceToDisplay.isLive()) {
            holder.liveTiming.setVisibility(View.VISIBLE);
        }
//        if (!serviceToDisplay.isCanCatch()) {
//            int grey = ContextCompat.getColor(context, R.color.grey);
//            holder.serviceNum.setBackgroundColor(grey);
//            holder.serviceDesc.setTextColor(grey);
//            holder.arrivalTime.setTextColor(grey);
//            holder.liveTiming.setImageResource(R.drawable.ic_wifi_signal_grey);
//        }

    }

    @Override
    public int getItemCount() {
        return (stopArrivalInfoForDirections.size() > 0 ? stopArrivalInfoForDirections.size() : 1);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

//        TextView topText, mainText, bottomText, bottomText2;
        TextView serviceNum, serviceDesc, arrivalTime, noService;
        ImageView liveTiming;


        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            serviceNum = itemView.findViewById(R.id.textViewServiceNum);
            serviceDesc = itemView.findViewById(R.id.textViewServiceInfo);
            arrivalTime = itemView.findViewById(R.id.textViewArrivalTime);
            noService = itemView.findViewById(R.id.textViewNoServicesAvailable);
            liveTiming = itemView.findViewById(R.id.imageViewArrivalLive);
        }
    }

    public interface VolleyCallBack {
        void onSuccess(List<ServiceInStopDetails> busStopArrivalInfo);
    }

}