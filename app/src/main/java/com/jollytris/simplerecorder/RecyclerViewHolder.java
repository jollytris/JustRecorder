package com.jollytris.simplerecorder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import rx.functions.Action2;

/**
 * Created by zic325 on 2016. 10. 4..
 */

public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    public View vContainer;
    public TextView tvTitle;
    public View vCheckCont;
    public Button btDelete;

    private Action2<Integer, RecyclerViewData> action;

    public RecyclerViewHolder(View itemView, Action2<Integer, RecyclerViewData> action) {
        super(itemView);
        this.action = action;
        vContainer = itemView.findViewById(R.id.itemContainer);
        tvTitle = (TextView) itemView.findViewById(R.id.title);
        vCheckCont = itemView.findViewById(R.id.checkContainer);
        btDelete = (Button) itemView.findViewById(R.id.deleteFile);
    }

    public void bind(RecyclerViewData data) {
        vContainer.setOnClickListener(v -> {
            action.call(0, data);
        });
        btDelete.setOnClickListener(v -> {
            action.call(1, data);
        });
        tvTitle.setText(data.getTitle());
        vCheckCont.setVisibility(data.isSelected() ? View.VISIBLE : View.GONE);
    }
}
