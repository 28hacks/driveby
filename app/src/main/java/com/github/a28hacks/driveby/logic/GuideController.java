package com.github.a28hacks.driveby.logic;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.github.a28hacks.driveby.R;
import com.github.a28hacks.driveby.audio.TextToSpeechService;
import com.github.a28hacks.driveby.location.DbLocationAdapter;
import com.github.a28hacks.driveby.model.database.GeoItem;
import com.github.a28hacks.driveby.model.database.InfoChunk;
import com.github.a28hacks.driveby.model.wiki_api.WikipediaRepository;
import com.github.a28hacks.driveby.model.wiki_api.WikipediaResult;
import com.github.a28hacks.driveby.ui.NotificationController;
import com.github.a28hacks.driveby.usecase.settings.UserSettings;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import retrofit2.Call;

public class GuideController implements DbLocationAdapter.LocationChangedListener {

    private static final String TAG = "GuideController";

    private final Realm mRealm;
    private final UserSettings settings;
    private final WikipediaRepository mWikipediaRepository;
    private GeoItem mCurrentItem;
    private Context mContext;
    private NotificationController mNotificationController;
    private TextToSpeechService mTTSService;
    private Call<WikipediaResult> pendingCall;
    private boolean boundToSpeechService;
    private boolean currentlySpeaking;

    public GuideController(Context context) {
        this.settings = new UserSettings(context);
        this.mContext = context;
        mWikipediaRepository = WikipediaRepository.forLanguage(settings.getTTSLanguage());
        IntentFilter filter = new IntentFilter();
        filter.addAction(TextToSpeechService.ACTION_SPEECH_DONE);
        context.registerReceiver(mBroadcastReceiver, filter);

        mRealm = Realm.getDefaultInstance();
        mNotificationController = new NotificationController(context);
    }

    public void processLocation(Location location) {
        int radius = calculateRadius(location.getSpeed());
        mWikipediaRepository.fetchItemsForLocation(location, radius);
        findNextItemAndSpeak(mRealm.where(GeoItem.class).findAll());
    }

    private int calculateRadius(float speed) {
        // TODO: calculate search radius with given speed
        // higher speed should search for larger radius
        return 10 * 1000;
    }

    private void findNextItemAndSpeak(List<GeoItem> items) {
        //try to find a city in currentItems first
        mCurrentItem = findBestItem(items, "city");

        //if there is no city or all chunks of city were already told, choose something else
        if (mCurrentItem == null) {
            mCurrentItem = findBestItem(items, null);
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
            if (type != null && !type.equalsIgnoreCase(item.getType())) {
                continue;
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
                outputText = mContext.getString(R.string.beautify_text_welcome_in)
                        + " " + item.getTitle() + ". ";
                if (!infoChunks.get(i).wasTold()) {
                    outputText += infoChunks.get(i).getSentence();
                    infoChunks.get(i).setTold(true);
                    item.setFirstToldAbout(new Date());
                    break;
                }
            }
        } else {
            for (int i = 0; i < infoChunks.size(); i++) {
                if (!infoChunks.get(i).wasTold()) {
                    outputText += infoChunks.get(i).getSentence();
                    infoChunks.get(i).setTold(true);
                    item.setFirstToldAbout(new Date());
                    break;
                }
            }
        }
        mRealm.commitTransaction();
        Log.e(TAG, "getOutputFrom: Item = " + mCurrentItem.getTitle() + ", value = " + outputText);
        return outputText;
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
