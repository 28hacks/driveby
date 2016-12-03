package com.github.a28hacks.driveby.audio;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Created by stefan on 15.10.16.
 */

public class TextToSpeechService extends Service implements TextToSpeech.OnInitListener,
        SpeechProgressListener.OnSpeechDoneListener {
    public static final String ACTION_SPEECH_DONE = "com.github.a28hacks.SPEECH_DONE";
    private static final String TAG = "TextToSpeechService";
    static final String UTTERANCE_ID = "UTTERANCE_ID";

    // Binder given to clients
    private final IBinder mBinder = new TTSBinder();

    private TextToSpeech tts;
    private boolean isInit;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
        tts = new TextToSpeech(getApplicationContext(), this);
        tts.setOnUtteranceProgressListener(new SpeechProgressListener(this));
        //tts.setSpeechRate(0.80f);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.GERMAN);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInit = true;
            }
        }
    }

    /* method which is called from activity / other services */
    public void speak(String text) {
        if (tts != null && isInit) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, UTTERANCE_ID);
        }
    }

    @Override
    public void onSpeechDone(boolean success) {
        sendBroadcast(new Intent(ACTION_SPEECH_DONE));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return mBinder;
    }


    public class TTSBinder extends Binder {
        public TextToSpeechService getService() {
            return TextToSpeechService.this;
        }
    }

}
