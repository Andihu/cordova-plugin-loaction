package com.microfountain.location;

import android.content.Context;
import android.content.res.Resources;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.search.core.PoiInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2015-2021
 * FileName: LoactionListAdapter
 * Author: hujian
 * Date: 2021/1/13 10:58
 * History:
 * <author> <time> <version> <desc>
 */
public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationItemHolder> {

    List<PoiInfo> poiInfoList = new ArrayList<>();

    private static int id_name;
    private static int id_desc;
    private static int id_check;
    private static int id_distance;

    private PoiInfo mCurrentPoiInfo;

    private OnItemClickListener itemClickListener;

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setPoiInfoList(List<PoiInfo> poiInfoList) {
        this.poiInfoList = poiInfoList;
        if (poiInfoList.size() > 0) mCurrentPoiInfo = poiInfoList.get(0);
        notifyDataSetChanged();
    }

    public PoiInfo getCurrentPoiInfo() {
        return mCurrentPoiInfo;
    }

    @NonNull
    @Override
    public LocationItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context appContext = viewGroup.getContext();
        Resources resource = appContext.getResources();
        String pkgName = appContext.getPackageName();
        int layoutID = resource.getIdentifier("location_poi_search_item", "layout", pkgName);

        id_name = resource.getIdentifier("name", "id", pkgName);
        id_desc = resource.getIdentifier("desc", "id", pkgName);
        id_check = resource.getIdentifier("ib_current", "id", pkgName);
        id_distance = resource.getIdentifier("distance", "id", pkgName);

        return new LocationItemHolder(LayoutInflater.from(appContext).inflate(layoutID, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LocationItemHolder locationItemHolder, int position) {
        PoiInfo poiInfo = poiInfoList.get(position);
        locationItemHolder.tvName.setText(poiInfo.name);
        locationItemHolder.tvAddress.setText(poiInfo.address);

        if (poiInfo.getDistance() > 0) {
            locationItemHolder.tvDistance.setText(String.format(poiInfo.getDistance() + "%s", "米内"));
            locationItemHolder.tvDistance.setVisibility(View.VISIBLE);
        } else {
            locationItemHolder.tvDistance.setVisibility(View.GONE);
        }
        if (poiInfo.getLocation() == mCurrentPoiInfo.getLocation()) {
            locationItemHolder.checked.setVisibility(View.VISIBLE);
        } else {
            locationItemHolder.checked.setVisibility(View.GONE);
        }
        locationItemHolder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(poiInfo);
            }
            mCurrentPoiInfo = poiInfo;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return poiInfoList.size();
    }

    static class LocationItemHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAddress;
        ImageView checked;
        TextView tvDistance;

        public LocationItemHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(id_name);
            tvAddress = itemView.findViewById(id_desc);
            tvDistance = itemView.findViewById(id_distance);
            checked = itemView.findViewById(id_check);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(PoiInfo poiInfo);
    }
}
