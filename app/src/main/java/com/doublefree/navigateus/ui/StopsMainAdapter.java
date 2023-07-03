package com.doublefree.navigateus.ui;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.doublefree.navigateus.ExpandableListViewStandardCode;
import com.doublefree.navigateus.R;
import com.doublefree.navigateus.data.busstopinformation.ServiceInStopDetails;
import com.doublefree.navigateus.data.busstopinformation.StopList;

import java.util.HashMap;
import java.util.List;

public class StopsMainAdapter extends BaseExpandableListAdapter {

    Context context;
    Activity activity;
    List<StopList> listGroup;
    HashMap<StopList, List<ServiceInStopDetails>> listItem;
    ExpandableListView expandableListView;
    FragmentManager childFragmentManager;


    public StopsMainAdapter(Activity activity, Context context, List<StopList> listGroup, HashMap<StopList, List<ServiceInStopDetails>>
            listItem, ExpandableListView expandableListView, FragmentManager childFragmentManager){
        this.context = context;
        this.listGroup = listGroup;
        this.listItem = listItem;
        this.expandableListView = expandableListView;
        this.activity = activity;
        this.childFragmentManager = childFragmentManager;
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
        textViewParent.setText(group.getDisplayStopName().isEmpty() ? group.getStopName() : group.getDisplayStopName());
        if (group.getStopDescription() != null) { //for LTA only
            TextView textViewSubparent = convertView.findViewById(R.id.list_subparent);
            textViewSubparent.setText(group.getStopDescription() + " (" + group.getStopId() + ")");
            textViewSubparent.setTextColor(ContextCompat.getColor(context, R.color.greydarker1));
        } else { //for NUS only

        }

        //clicking the 3 dots opens the alertfav dialog
        ImageView showDialog = convertView.findViewById(R.id.showDialogFromBusStopImageView);
        showDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpandableListViewStandardCode.showAlertFavouriteDialog(true, groupPosition, activity, context, listGroup, childFragmentManager);
            }
        });

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
                imageViewLive1Time.setVisibility(ImageView.INVISIBLE);
                imageViewLive2Time.setVisibility(ImageView.INVISIBLE);
//            TextView textViewTime1Live = convertView.findViewById(R.id.list_child_timing1_live);
                if (child.getFirstArrival().charAt(0) == '-') {  //no service
                    textViewTime1.setText("No Service");
                    textViewTime1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                    textViewTime1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    textViewTime1.setTextColor(ContextCompat.getColor(context, R.color.grey));
//                    imageViewLive1Time.setVisibility(ImageView.INVISIBLE);
//                textViewTime1Live.setText("");
//                textViewTime1Live.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                } else {

                    textViewTime1.setText(child.getFirstArrival());
                    textViewTime1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

                    if ((child.getFirstArrival().length() == 1 && child.getFirstArrival().contains("1")) || child.getFirstArrival().contains("Arr")) {
                        textViewTime1.setBackgroundColor(ContextCompat.getColor(context, R.color.NUS_Blue));
                        textViewTime1.setTextColor(ContextCompat.getColor(context, R.color.white));
                    } else {
                        textViewTime1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        textViewTime1.setTextColor(ContextCompat.getColor(context, R.color.black));
                    }
//                    imageViewLive1Time.setVisibility(ImageView.VISIBLE);

                }



//            TextView textViewTime2Live = convertView.findViewById(R.id.list_child_timing2_live);
                if (child.getFirstArrival().charAt(0) == '-') {
                    textViewTime2.setText("");
                    textViewTime2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    textViewTime2.setTextColor(ContextCompat.getColor(context, R.color.black));
//                    imageViewLive2Time.setVisibility(ImageView.INVISIBLE);
//                textViewTime2Live.setText("");
//                textViewTime2Live.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                } else if (child.getSecondArrival().charAt(0) == '-') {
                    textViewTime2.setText("");
                    textViewTime2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    textViewTime2.setTextColor(ContextCompat.getColor(context, R.color.grey));
                    textViewTime2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
//                    imageViewLive2Time.setVisibility(ImageView.INVISIBLE);
                } else {
                    textViewTime2.setText(child.getSecondArrival());
                    textViewTime2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

                    if ((child.getSecondArrival().length() == 1 && child.getSecondArrival().contains("1")) || child.getSecondArrival().contains("Arr")) {
                        textViewTime2.setBackgroundColor(ContextCompat.getColor(context, R.color.NUS_Blue));
                        textViewTime2.setTextColor(ContextCompat.getColor(context, R.color.white));
                    } else {
                        textViewTime2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        textViewTime2.setTextColor(ContextCompat.getColor(context, R.color.black));
                    }
//                    imageViewLive2Time.setVisibility(ImageView.VISIBLE);

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
