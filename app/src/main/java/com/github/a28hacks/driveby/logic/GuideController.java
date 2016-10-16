package com.github.a28hacks.driveby.logic;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.icu.text.IDNA;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.github.a28hacks.driveby.audio.TextToSpeechService;
import com.github.a28hacks.driveby.database.RealmProvider;
import com.github.a28hacks.driveby.location.DbLocationAdapter;
import com.github.a28hacks.driveby.model.database.GeoItem;
import com.github.a28hacks.driveby.model.database.InfoChunk;
import com.github.a28hacks.driveby.model.wiki_api.GeoSearchResult;
import com.github.a28hacks.driveby.model.wiki_api.QueryResult;
import com.github.a28hacks.driveby.model.wiki_api.WikipediaResult;
import com.github.a28hacks.driveby.network.WikipediaService;
import com.github.a28hacks.driveby.text.TextUtils;
import com.github.a28hacks.driveby.ui.NotificationController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GuideController implements Callback<WikipediaResult>, DbLocationAdapter.LocationChangedListener {

    private static final String TAG = "GuideController";

    private final WikipediaService mWikipediaService;
    private final Realm mRealm;
    private GeoItem mCurrentItem;
    private Context mContext;
    private NotificationController mNotificationController;
    private TextToSpeechService mTTSService;
    private Call<WikipediaResult> pendingCall;
    private boolean boundToSpeechService;
    private boolean currentlySpeaking;

    public GuideController(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/w/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.mContext = context;

        IntentFilter filter = new IntentFilter();
        filter.addAction(TextToSpeechService.ACTION_SPEECH_DONE);
        context.registerReceiver(mBroadcastReceiver, filter);

        mRealm = RealmProvider.createRealmInstance(context);
        mWikipediaService = retrofit.create(WikipediaService.class);
        mNotificationController = new NotificationController(context);
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
        //updating results - every result should be in db already
        List<GeoItem> currentItems = new ArrayList<>();
        mRealm.beginTransaction();
        for (String s : pages.keySet()) {
            GeoSearchResult searchResult = pages.get(s);
            if (searchResult.getPageId() == 0) {
                continue;
            }
            Log.d(TAG, "processPageResult: " + s + " - " + searchResult.getExtract());

            RealmQuery<GeoItem> query = mRealm.where(GeoItem.class);
            query.equalTo("id", searchResult.getPageId());
            GeoItem item = query.findFirst();
            if (item != null) {
                Log.e(TAG, "processPageResult: Query Result = " + item.getId());
                RealmList<InfoChunk> infoChunks;
                if ((item.getInfoChunks() == null || item.getInfoChunks().isEmpty()) &&
                        searchResult.getExtract() != null &&
                        !searchResult.getExtract().isEmpty()) {
                    infoChunks = new RealmList<>();
                    String text = TextUtils.beautify(searchResult.getExtract());
                    List<String> sentences = TextUtils.splitSentences(text);
                    for (String sentence : sentences) {
                        if(sentence.trim().length() == 0) continue;
                        InfoChunk managedChunk = mRealm.copyToRealm(new InfoChunk(sentence, false));
                        infoChunks.add(managedChunk);
                    }
                    item.setInfoChunks(infoChunks);
                }
                currentItems.add(item);
            } else {
                Log.e(TAG, "processPageResult: No Result for Query");
                //nothing - should not happen
            }
        }
        mRealm.commitTransaction();

        //try to find a city in currentItems first
        mCurrentItem = findBestItem(currentItems, "city");

        //if there is no city or all chunks of city were already told, choose something else
        if (mCurrentItem == null) {
            mCurrentItem = findBestItem(currentItems, null);
        }

        //bind to speech service if needed
        if (!boundToSpeechService) {
            // Bind to TTSService
            Intent intent = new Intent(mContext, TextToSpeechService.class);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else {
            executeSpeak();
        }

    }

    private GeoItem findBestItem(List<GeoItem> currentItems, String type) {
        for (GeoItem item : currentItems) {
            if (type != null && !item.getType().equalsIgnoreCase(type)) {
                break;
            }
            boolean hasUntoldEntries = !item.getInfoChunks().isEmpty();
            for (InfoChunk infoChunk : item.getInfoChunks()) {
                hasUntoldEntries = hasUntoldEntries && !infoChunk.wasTold();
            }
            if (hasUntoldEntries) {
                return item;
            }
        }
        return null;
    }

    private void executeSpeak() {
        //currently no item? skip speak
        if (mCurrentItem == null) {
            return;
        }
        //get output text from currentItem
        mTTSService.speak(getOutputFrom(mCurrentItem));
        currentlySpeaking = true;
        mNotificationController.displayGeoItem(mCurrentItem);
    }

    public String getOutputFrom(GeoItem item) {
        mRealm.beginTransaction();
        String outputText = "";
        RealmList<InfoChunk> infoChunks = item.getInfoChunks();
        if (item.getType() != null
                && item.getType().equalsIgnoreCase("city")) {
            int startpos = infoChunks.size() > 2 ? 1 : 0;
            for (int i = startpos; i < infoChunks.size(); i++) {
                outputText = "Welcome to " + item.getTitle() + ".";
                if (!infoChunks.get(i).wasTold()) {
                    outputText += infoChunks.get(i).getSentence();
                    infoChunks.get(i).setTold(true);
                    break;
                }
            }
        } else {
            for (int i = 0; i < infoChunks.size(); i++) {
                if (!infoChunks.get(i).wasTold()) {
                    outputText += infoChunks.get(i).getSentence();
                    infoChunks.get(i).setTold(true);
                    break;
                }
            }
        }
        mRealm.commitTransaction();
        Log.e(TAG, "getOutputFrom: Item = " + mCurrentItem.getTitle() + ", value = " + outputText);
        return outputText;
    }


    private void processSearchResult(List<GeoSearchResult> items) {
        // TODO: compare ids and don't do anything if nearest item didn't change

        //create entry in db only when it's a new datapoint
        mRealm.beginTransaction();
        for (GeoSearchResult result : items) {
            Log.d(TAG, "processSearchResult: " + result.getPageId() + result.getTitle());
            RealmQuery<GeoItem> query = mRealm.where(GeoItem.class);
            query.equalTo("id", result.getPageId());
            GeoItem item = query.findFirst();
            if (item == null) {
                item = new GeoItem(result);
                mRealm.copyToRealmOrUpdate(item);
            }
        }
        mRealm.commitTransaction();


        StringBuilder ids = new StringBuilder();
        for (GeoSearchResult geoSearchResult : items) {
            ids.append(geoSearchResult.getPageId()).append("|");
        }
        if (currentlySpeaking) {
            pendingCall = mWikipediaService.getExtractText(ids.toString());
        } else {
            mWikipediaService.getExtractText(ids.toString()).enqueue(this);
        }
    }

    public void destroy() {
        // Unbind from the service
        if (boundToSpeechService) {
            mContext.unbindService(mConnection);
            boundToSpeechService = false;
        }
        //unregister broadcastreceiver
        if (mBroadcastReceiver != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    @Override
    public void onFailure(Call<WikipediaResult> call, Throwable t) {

    }

    @Override
    public void onLocationChanged(Location newLocation) {
        // TODO: check if some time passed since last speek
        processLocation(newLocation);
    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: " + intent.getAction());
            if (intent.getAction().equals(TextToSpeechService.ACTION_SPEECH_DONE)) {
                currentlySpeaking = false;
                mNotificationController.dismissNotification();
                if (pendingCall != null) {
                    pendingCall.enqueue(GuideController.this);
                    pendingCall = null;
                }
            }
        }
    };

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to TTSService, cast the IBinder and get TTSService instance
            TextToSpeechService.TTSBinder binder = (TextToSpeechService.TTSBinder) service;
            mTTSService = binder.getService();

            if (!boundToSpeechService) {
                boundToSpeechService = true;
                executeSpeak();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundToSpeechService = false;
        }
    };
}
