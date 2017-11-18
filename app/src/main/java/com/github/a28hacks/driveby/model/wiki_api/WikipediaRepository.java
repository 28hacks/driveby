package com.github.a28hacks.driveby.model.wiki_api;

import android.location.Location;

import com.github.a28hacks.driveby.model.database.GeoItem;
import com.github.a28hacks.driveby.model.database.InfoChunk;
import com.github.a28hacks.driveby.network.WikipediaService;
import com.github.a28hacks.driveby.text.TextUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Jonas Gerdes <dev@jonasgerdes.com>
 * @since 17-Nov-17
 */

public class WikipediaRepository {
    private static final String TAG = "WikipediaRepository";

    private final WikipediaService mWikipediaService;
    private final Realm mRealm;

    public WikipediaRepository(String language) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://" + language + ".wikipedia.org/w/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mWikipediaService = retrofit.create(WikipediaService.class);
        mRealm = Realm.getDefaultInstance();
    }

    public static WikipediaRepository forLanguage(Locale ttsLanguage) {
        return new WikipediaRepository(ttsLanguage.getLanguage());
    }

    public void fetchItemsForLocation(Location location, int radius) {
        String formatedCoords = WikipediaService.Util
                .formatCoordinates(location.getLatitude(), location.getLongitude());
        mWikipediaService.getItemForLocation(formatedCoords, radius)
                .subscribeOn(Schedulers.io())
                .map(WikipediaResult::getQuery)
                .map(QueryResult::getItems)
                .map(geoSearchResults -> {
                    StringBuilder ids = new StringBuilder();
                    for (GeoSearchResult geoSearchResult : geoSearchResults) {
                        ids.append(geoSearchResult.getPageId()).append("|");
                    }
                    return ids.toString();
                })
                .flatMap(mWikipediaService::getExtractText)
                .map(WikipediaResult::getQuery)
                .map(QueryResult::getPages)
                .map(Map::entrySet)
                .flatMap(Observable::fromIterable)
                .map(Map.Entry::getValue)
                .map(this::createGeoItem)
                .toList()
                .subscribe(this::persistItems);

    }

    private GeoItem createGeoItem(GeoSearchResult result) {
        GeoItem item = new GeoItem(result);
        RealmList<InfoChunk> infoChunks;
        if ((item.getInfoChunks() == null || item.getInfoChunks().isEmpty()) &&
                result.getExtract() != null &&
                !result.getExtract().isEmpty()) {
            infoChunks = new RealmList<>();
            String text = TextUtils.beautify(result.getExtract());
            List<String> sentences = TextUtils.splitSentences(text);
            for (String sentence : sentences) {
                if (sentence.trim().length() == 0) continue;
                infoChunks.add(new InfoChunk(sentence, false));
            }
            item.setInfoChunks(infoChunks);
        }
        return item;
    }

    private void persistItems(List<GeoItem> items) {
        Realm.getDefaultInstance().executeTransaction(realm -> {
            for (GeoItem item : items) {
                //only save when we it wasn't before
                if (realm.where(GeoItem.class)
                        .equalTo("id", item.getId())
                        .count() == 0) {
                    realm.copyToRealm(item);
                }
            }
        });
    }
}
