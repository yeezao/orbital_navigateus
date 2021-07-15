package com.doublefree.navigateus.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.doublefree.navigateus.R;
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;
import com.doublefree.navigateus.data.busstopinformation.StopList;

import java.util.HashMap;
import java.util.List;

public class StopsMainAdapter extends BaseExpandableListAdapter {

    Context context;
    List<StopList> listGroup;
    HashMap<StopList, List<ServiceInStopDetails>> listItem;

    public StopsMainAdapter(Context context, List<StopList> listGroup, HashMap<StopList, List<ServiceInStopDetails>>
            listItem){
        this.context = context;
        this.listGroup = listGroup;
        this.listItem = listItem;
    }
    @Override
    public int getGroupCount() {
        return listGroup.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {

//        if (this.listItem.get(this.listGroup.get(groupPosition)) == null) {
//            return getChildrenCountOnline(groupPosition);
//        }
//        return 10;\
        try {
            return this.listItem.get(this.listGroup.get(groupPosition)).size();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public StopList getGroup(int groupPosition) {
        return this.listGroup.get(groupPosition);
    }

    @Override
    public ServiceInStopDetails getChild(int groupPosition, int childPosition) {
        return this.listItem.get(this.listGroup.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        StopList group = getGroup(groupPosition);
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (group.getStopDescription() == null) {
                convertView = layoutInflater.inflate(R.layout.list_group_nus,null);
            } else {
                convertView = layoutInflater.inflate(R.layout.list_group_lta,null);
            }
        }
        TextView textViewParent = convertView.findViewById(R.id.list_parent);
        textViewParent.setText(group.getStopName());
        if (group.getStopDescription() != null) { //for LTA only
            TextView textViewSubparent = convertView.findViewById(R.id.list_subparent);
            textViewSubparent.setText(group.getStopDescription() + " (" + group.getStopId() + ")");
            textViewSubparent.setTextColor(ContextCompat.getColor(context, R.color.greydarker1));
        } else { //for NUS only

        }

//        ProgressBar onClickProgressBar = convertView.findViewById(R.id.onGroupClickProgressBar);

//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onClickProgressBar.setVisibility(View.VISIBLE);
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (isExpanded == true) {
//                            onClickProgressBar.setVisibility(View.INVISIBLE);
//                            handler.removeCallbacksAndMessages(null);
//                        }
//                        handler.postDelayed(this, 200);
//                    }
//                }, 200);
//            }
//        });

//        TextView textViewSubParent = convertView.findViewById(R.id.list_subparent);
//        textViewSubParent.setText(group);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            ServiceInStopDetails child = getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (child.getFirstArrivalLive() == null) {
                    convertView = layoutInflater.inflate(R.layout.list_item_lta, null);
                } else {
                    convertView = layoutInflater.inflate(R.layout.list_item_nus, null);
                }
            }

            TextView textViewService = convertView.findViewById(R.id.list_child);
            textViewService.setText(child.getServiceNum());


            //for list_item_nus (NUS only)
            if (child.getFirstArrivalLive() != null) {
                TextView textViewTime1 = convertView.findViewById(R.id.list_child_timing1);
                TextView textViewTime2 = convertView.findViewById(R.id.list_child_timing2);
                ImageView imageViewLive1Time = convertView.findViewById(R.id.live_timing_imageview);
                ImageView imageViewLive2Time = convertView.findViewById(R.id.live_timing_imageview_2);
//            TextView textViewTime1Live = convertView.findViewById(R.id.list_child_timing1_live);
                if (child.getFirstArrival().charAt(0) == '-') {
                    textViewTime1.setText("No Service");
                    textViewTime1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                    textViewTime1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    textViewTime1.setTextColor(ContextCompat.getColor(context, R.color.grey));
                    imageViewLive1Time.setVisibility(ImageView.INVISIBLE);
//                textViewTime1Live.setText("");
//                textViewTime1Live.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                } else {
                    textViewTime1.setText(child.getFirstArrival());
                    textViewTime1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    if (child.getFirstArrivalLive().length() == 0) {
                        textViewTime1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        textViewTime1.setTextColor(ContextCompat.getColor(context, R.color.black));
                        imageViewLive1Time.setVisibility(ImageView.INVISIBLE);
//                    textViewTime1Live.setText("");
//                    textViewTime1Live.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                    } else {
                        if ((child.getFirstArrival().length() == 1 && child.getFirstArrival().contains("1")) || child.getFirstArrival().contains("Arr")) {
                            textViewTime1.setBackgroundColor(ContextCompat.getColor(context, R.color.NUS_Blue));
                            textViewTime1.setTextColor(ContextCompat.getColor(context, R.color.white));
                        } else {
                            textViewTime1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                            textViewTime1.setTextColor(ContextCompat.getColor(context, R.color.black));
                        }
                        imageViewLive1Time.setVisibility(ImageView.VISIBLE);
//                    textViewTime1Live.setText("LIVE");
//                    textViewTime1Live.setTextColor(ContextCompat.getColor(context, R.color.white));
//                    textViewTime1Live.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                    }
                }



//            TextView textViewTime2Live = convertView.findViewById(R.id.list_child_timing2_live);
                if (child.getFirstArrival().charAt(0) == '-') {
                    textViewTime2.setText("");
                    textViewTime2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    textViewTime2.setTextColor(ContextCompat.getColor(context, R.color.black));
                    imageViewLive2Time.setVisibility(ImageView.INVISIBLE);
//                textViewTime2Live.setText("");
//                textViewTime2Live.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                } else if (child.getSecondArrival().charAt(0) == '-') {
                    textViewTime2.setText("< LAST BUS");
                    textViewTime2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    textViewTime2.setTextColor(ContextCompat.getColor(context, R.color.grey));
                    textViewTime2.setTextSize(14);
                    imageViewLive2Time.setVisibility(ImageView.INVISIBLE);
                } else {
                    textViewTime2.setText(child.getSecondArrival());
                    if (child.getSecondArrivalLive().length() == 0) {
                        textViewTime2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        textViewTime2.setTextColor(ContextCompat.getColor(context, R.color.black));
                        imageViewLive2Time.setVisibility(ImageView.INVISIBLE);
//                    textViewTime2Live.setText("");
//                    textViewTime2Live.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                    } else {
                        if ((child.getSecondArrival().length() == 1 && child.getSecondArrival().contains("1")) || child.getSecondArrival().contains("Arr")) {
                            textViewTime2.setBackgroundColor(ContextCompat.getColor(context, R.color.NUS_Blue));
                            textViewTime2.setTextColor(ContextCompat.getColor(context, R.color.white));
                        } else {
                            textViewTime2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                            textViewTime2.setTextColor(ContextCompat.getColor(context, R.color.black));
                        }
                        imageViewLive2Time.setVisibility(ImageView.VISIBLE);
//                    textViewTime2Live.setText("LIVE");
//                    textViewTime2Live.setTextColor(ContextCompat.getColor(context, R.color.white));
//                    textViewTime2Live.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                    }
                }
            }
            //for list_item_lta (LTA only)
            else {
                TextView textViewArrival1 = convertView.findViewById(R.id.list_child_timing1);
                textViewArrival1.setText(child.getFirstArrival());
                TextView textViewArrival2 = convertView.findViewById(R.id.list_child_timing2);
                textViewArrival2.setText(child.getSecondArrival());
            }




        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }



}
