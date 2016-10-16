package com.github.a28hacks.driveby.usecase.history;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.a28hacks.driveby.R;
import com.github.a28hacks.driveby.model.database.GeoItem;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    private RealmResults<GeoItem> mGeoItems;

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_card, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        GeoItem item = mGeoItems.get(position);
        holder.onBind(item);
    }

    @Override
    public int getItemCount() {
        return mGeoItems.size();
    }

    public void setGeoItems(RealmResults<GeoItem> geoItems) {
        mGeoItems = geoItems;
        mGeoItems.addChangeListener(new RealmChangeListener<RealmResults<GeoItem>>() {
            @Override
            public void onChange(RealmResults<GeoItem> element) {
                //todo: animate
                notifyDataSetChanged();
            }
        });
    }
}
