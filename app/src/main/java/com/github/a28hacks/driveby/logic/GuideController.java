package com.github.a28hacks.driveby.logic;


import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.github.a28hacks.driveby.database.RealmProvider;
import com.github.a28hacks.driveby.location.DbLocationAdapter;
import com.github.a28hacks.driveby.model.wiki_api.GeoSearchResult;
import com.github.a28hacks.driveby.model.wiki_api.QueryResult;
import com.github.a28hacks.driveby.model.wiki_api.WikipediaResult;
import com.github.a28hacks.driveby.network.WikipediaService;

import java.util.List;
import java.util.Map;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GuideController implements Callback<WikipediaResult>, DbLocationAdapter.LocationChangedListener {

    private static final String TAG = "GuideController";

    private final WikipediaService mWikipediaService;
    private final Realm mRealm;

    public GuideController(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/w/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRealm = RealmProvider.createRealmInstance(context);
        mWikipediaService = retrofit.create(WikipediaService.class);
    }

    public void processLocation(Location location) {
        int radius = calculateRadius(location.getSpeed());
        String formatedCoords = WikipediaService.Util
                .formatCoordinates(location.getLatitude(), location.getLongitude());
        mWikipediaService.getItemForLocation(radius, formatedCoords).enqueue(this);
    }

    private int calculateRadius(float speed) {
        // TODO: calculate search radius with given speed
        // higher speed should search for larger radius
        return 10 * 1000;
    }

    @Override
    public void onResponse(Call<WikipediaResult> call, Response<WikipediaResult> response) {
        QueryResult queryResult = response.body().getQuery();
        if (queryResult.getItems() != null) {
            processSearchResult(queryResult.getItems());
        } else if (queryResult.getPages() != null) {
            processPageResult(queryResult.getPages());
        }
    }

    private void processPageResult(Map<String, GeoSearchResult> pages) {
        for (String s : pages.keySet()) {
            Log.d(TAG, "processPageResult: " + s + " - " + pages.get(s).getTitle());
        }
    }

    private void processSearchResult(List<GeoSearchResult> items) {
        // TODO: save current item, compare ids and don't do anything if nearest item didn't change
        StringBuilder ids = new StringBuilder();
        for (GeoSearchResult geoSearchResult : items) {
            ids.append(geoSearchResult.getPageId()).append("|");
        }
        mWikipediaService.getExtractText(ids.toString()).enqueue(this);
    }

    @Override
    public void onFailure(Call<WikipediaResult> call, Throwable t) {

    }

    @Override
    public void onLocationChanged(Location newLocation) {
        // TODO: check if no audio is playing right now and some time passed since
        processLocation(newLocation);
    }
}
