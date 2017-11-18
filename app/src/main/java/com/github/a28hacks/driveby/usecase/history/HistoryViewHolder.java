package com.github.a28hacks.driveby.usecase.history;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.a28hacks.driveby.R;
import com.github.a28hacks.driveby.model.database.GeoItem;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HistoryViewHolder extends RecyclerView.ViewHolder {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.first_chunk)
    TextView firstChunk;

    @BindView(R.id.date)
    TextView date;

    @BindView(R.id.thumbnail)
    ImageView image;

    public HistoryViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void onBind(final GeoItem geoItem) {
        title.setText(geoItem.getTitle());

        if (!geoItem.getInfoChunks().isEmpty()) {
            firstChunk.setText(geoItem.getInfoChunks().get(0).getSentence());
        }

        String formatedDate = DATE_FORMAT.format(geoItem.getFirstToldAbout());
        date.setText(formatedDate);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://" + geoItem.getLanguageCode() + ".m.wikipedia.org/?curid=" + geoItem.getId();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                itemView.getContext().startActivity(i);
            }
        });

        if(geoItem.getThumbnail()!= null && geoItem.getThumbnail().getSource() != null){
            image.setVisibility(View.VISIBLE);
            image.post(() -> {

                Glide.with(image)
                        .load(geoItem.getThumbnail().getSourceWithWidth(image.getMeasuredWidth()))
                        .apply(new RequestOptions().centerCrop())
                        .into(image);
            });
        }else{
            image.setVisibility(View.GONE);
        }

    }
}
