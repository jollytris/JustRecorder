package com.jollytris.simplerecorder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import rx.functions.Action2;

/**
 * Created by zic325 on 2016. 10. 4..
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    private Action2<Integer, RecyclerViewData> action;
    private ArrayList<RecyclerViewData> items;
    private RecyclerViewData selectedItem;

    public RecyclerViewAdapter(Action2<Integer, RecyclerViewData> action) {
        this.action = action;
        this.items = new ArrayList<>();
    }

    public void clear() {
        items.clear();
    }

    public void add(RecyclerViewData item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void add(int index, RecyclerViewData item) {
        items.add(index, item);
        notifyDataSetChanged();
    }

    public void remove(RecyclerViewData item) {
        items.remove(item);
        notifyDataSetChanged();
    }

    public RecyclerViewData getSelectedItem() {
        return selectedItem;
    }

    public void setSelected(RecyclerViewData item) {
        boolean isSelected = item.isSelected();
        for (RecyclerViewData i : items) {
            i.setSelected(false);
        }
        if (isSelected) {
            item.setSelected(false);
            selectedItem = null;
        } else {
            item.setSelected(true);
            selectedItem = item;
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_listitem, parent, false);
        RecyclerViewHolder holder = new RecyclerViewHolder(v, action);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        RecyclerViewData item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
