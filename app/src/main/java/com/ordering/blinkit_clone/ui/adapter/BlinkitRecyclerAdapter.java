package com.ordering.blinkit_clone.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BlinkitRecyclerAdapter<T> extends RecyclerView.Adapter<BlinkitRecyclerAdapter.BlinkitViewHolder> {

    private List<T> items; // The data list
    private final int layoutId; // The layout for each item
    private final OnBindRowListener<T> onBindRowListener; // Callback for onBindViewHolder

    // Constructor
    public BlinkitRecyclerAdapter(List<T> items, int layoutId, OnBindRowListener<T> onBindRowListener) {
        this.items = items;
        this.layoutId = layoutId;
        this.onBindRowListener = onBindRowListener;
    }

    @NonNull
    @Override
    public BlinkitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new BlinkitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlinkitViewHolder holder, int position) {
        T item = items.get(position);
        // Notify the callback with the current view, item, and position
        if (onBindRowListener != null) {
            onBindRowListener.onBind(holder.itemView, item, position);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<T> Items) {
        this.items = Items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class BlinkitViewHolder extends RecyclerView.ViewHolder {
        public BlinkitViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // Callback interface for binding the row
    public interface OnBindRowListener<T> {
        void onBind(View view, T item, int position);
    }
}
