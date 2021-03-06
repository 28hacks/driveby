package com.github.a28hacks.driveby.audio;

import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

/**
 * Created by stefan on 15.10.16.
 */

public class SpeechProgressListener extends UtteranceProgressListener {

    private static final String TAG = "SpeechProgressListener";
    private OnSpeechDoneListener listener;

    public SpeechProgressListener(OnSpeechDoneListener listener) {
        super();
        this.listener = listener;
    }

    @Override
    public void onError(String utteranceId, int errorCode) {
        Log.e(TAG, "onError");
        super.onError(utteranceId, errorCode);
        listener.onSpeechDone(false);
    }

    @Override
    public void onStop(String utteranceId, boolean interrupted) {
        super.onStop(utteranceId, interrupted);
        Log.e(TAG, "onStop");
        //Interrupted or cut off via FLUSH
    }

    @Override
    public void onBeginSynthesis(String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
        super.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
    }

    @Override
    public void onAudioAvailable(String utteranceId, byte[] audio) {
        super.onAudioAvailable(utteranceId, audio);
    }

    @Override
    public void onStart(String utteranceId) {
        Log.e(TAG, "onStart");
    }

    @Override
    public void onDone(String utteranceId) {
        listener.onSpeechDone(true);
        Log.e(TAG, "onDone");
    }

    @Override
    public void onError(String utteranceId) {
        listener.onSpeechDone(false);
        Log.e(TAG, "onError");
    }

    interface OnSpeechDoneListener{
        void onSpeechDone(boolean success);
    }
}
