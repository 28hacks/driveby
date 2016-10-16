package com.github.a28hacks.driveby.usecase.history;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.a28hacks.driveby.R;
import com.github.a28hacks.driveby.model.database.GeoItem;
import com.github.a28hacks.driveby.model.database.InfoChunk;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HistoryViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.title)
    TextView title;

    public HistoryViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void onBind(GeoItem geoItem) {
        title.setText(geoItem.getTitle());

        for (InfoChunk infoChunk : geoItem.getInfoChunks()) {
            if (infoChunk.wasTold()) {

            }
        }

    }
}
