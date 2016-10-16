package com.github.a28hacks.driveby.usecase.history;

import android.content.Intent;
import android.net.Uri;
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

    public void onBind(final GeoItem geoItem) {
        title.setText(geoItem.getTitle());

        for (InfoChunk infoChunk : geoItem.getInfoChunks()) {
            if (infoChunk.wasTold()) {

            }
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://en.m.wikipedia.org/?curid=" + geoItem.getId();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                itemView.getContext().startActivity(i);
            }
        });

    }
}
