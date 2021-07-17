package com.doublefree.navigateus.ui.stops_services;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.doublefree.navigateus.R;
import com.doublefree.navigateus.data.busrouteinformation.ServiceInfo;
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;

import java.util.List;


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