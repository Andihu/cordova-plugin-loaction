package com.microfountain.location;

import android.content.Context;
import android.content.res.Resources;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.search.sug.SuggestionResult;

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
public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.LocationItemHolder> {

    List<SuggestionResult.SuggestionInfo> suggestions = new ArrayList<>();

    private static int id_name;
    private static int id_desc;
    private static int id_check;

    private SuggestionResult.SuggestionInfo mCurrentSuggestion;

    private String keyword;

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    private OnItemClickListener itemClickListener;

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setSuggestionsList(List<SuggestionResult.SuggestionInfo> SuggestionInfo) {
        this.suggestions = SuggestionInfo;
        notifyDataSetChanged();
    }

    public void clear() {
        this.suggestions.clear();
        keyword = "";
        mCurrentSuggestion = null;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context appContext = viewGroup.getContext();
        Resources resource = appContext.getResources();
        String pkgName = appContext.getPackageName();
        int layoutID = resource.getIdentifier("location_suggestion_search_item", "layout", pkgName);

        id_name = resource.getIdentifier("name", "id", pkgName);
        id_desc = resource.getIdentifier("desc", "id", pkgName);
        id_check = resource.getIdentifier("ib_current", "id", pkgName);

        return new LocationItemHolder(LayoutInflater.from(appContext).inflate(layoutID, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LocationItemHolder locationItemHolder, int position) {
        SuggestionResult.SuggestionInfo SuggestionInfo = suggestions.get(position);
        locationItemHolder.tvName.setText(checkByKeyword(SuggestionInfo.key));
        locationItemHolder.tvAddress.setText(checkByKeyword(SuggestionInfo.address));
        if (mCurrentSuggestion != null) {
            if (TextUtils.equals(SuggestionInfo.getKey(), mCurrentSuggestion.getKey())) {
                locationItemHolder.checked.setVisibility(View.VISIBLE);
            } else {
                locationItemHolder.checked.setVisibility(View.GONE);
            }
        } else {
            locationItemHolder.checked.setVisibility(View.GONE);
        }
        locationItemHolder.itemView.setOnClickListener(v -> {
            if (SuggestionInfo.getPt() == null) {
                Toast.makeText(locationItemHolder.itemView.getContext(), "信息不足！", Toast.LENGTH_SHORT).show();
            } else {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(SuggestionInfo);
                }
                mCurrentSuggestion = SuggestionInfo;
            }
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class LocationItemHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAddress;
        ImageView checked;

        public LocationItemHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(id_name);
            tvAddress = itemView.findViewById(id_desc);
            checked = itemView.findViewById(id_check);

        }
    }

    public interface OnItemClickListener {
        void onItemClick(SuggestionResult.SuggestionInfo suggestionInfo);
    }

    private Spanned checkByKeyword(String content) {
        if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(keyword) && content.contains(keyword)) {
            String replaceKeyWord = "<font color=#3D61CC>" + keyword + "</font>";
            return Html.fromHtml(content.replace(keyword, replaceKeyWord));
        }
        return Html.fromHtml(content);
    }

}
