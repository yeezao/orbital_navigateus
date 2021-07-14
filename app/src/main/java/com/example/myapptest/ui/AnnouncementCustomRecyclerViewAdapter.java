package com.example.myapptest.ui;

import android.app.Activity;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapptest.R;
import com.example.myapptest.data.busnetworkinformation.NetworkTickerTapesAnnouncements;
import com.example.myapptest.data.busstopinformation.ServiceInStopDetails;
import com.example.myapptest.data.busstopinformation.StopArrivalInfoForDirections;
import com.example.myapptest.data.naviagationdata.NavigationNodes;
import com.example.myapptest.data.naviagationdata.NavigationPartialResults;
import com.example.myapptest.ui.directions.SingleStopBusArrivalCustomAdapterRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AnnouncementCustomRecyclerViewAdapter extends RecyclerView.Adapter<AnnouncementCustomRecyclerViewAdapter.MyViewHolder> {

    Context context;
    Activity activity;

    List<NetworkTickerTapesAnnouncements> list;

    View view;

    public AnnouncementCustomRecyclerViewAdapter(Activity activity, Context context, List<NetworkTickerTapesAnnouncements> list) {
        this.context = context;
        this.activity = activity;
        this.list = list;
    }

    @NotNull
    @Override
    public AnnouncementCustomRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rowlayoutannoucement, parent, false);
        view = v;
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AnnouncementCustomRecyclerViewAdapter.MyViewHolder holder, final int position) {
        // set the data in items

        if (list.size() == 0) {
            holder.noAnnouncements.setVisibility(View.VISIBLE);
            holder.mainMsg.setVisibility(View.GONE);
            holder.subMsg.setVisibility(View.GONE);
            return;
        }

        Log.e("position", position + " " + list.get(position).getMessage());
        NetworkTickerTapesAnnouncements item = list.get(position);
        holder.mainMsg.setText(item.getMessage());
        Linkify.addLinks(holder.mainMsg, Linkify.ALL);
        holder.mainMsg.setMovementMethod(LinkMovementMethod.getInstance());
        if (item.getServicesAffected() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Services Affected: ").append(item.getServicesAffected());
            holder.subMsg.setText(stringBuilder.toString());
        } else {
            holder.subMsg.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return (list.size() > 0 ? list.size() : 1);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        //        TextView topText, mainText, bottomText, bottomText2;
        TextView mainMsg, subMsg, noAnnouncements;


        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            mainMsg = itemView.findViewById(R.id.textViewAnnouncement);
            subMsg = itemView.findViewById(R.id.textViewAnnouncementSub);
            noAnnouncements = itemView.findViewById(R.id.textViewNoAnnouncements);

        }
    }

}
