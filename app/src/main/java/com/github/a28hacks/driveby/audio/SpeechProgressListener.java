package com.github.a28hacks.driveby.audio;

import android.speech.tts.UtteranceProgressListener;

/**
 * Created by stefan on 15.10.16.
 */

public class SpeechProgressListener extends UtteranceProgressListener {

    private OnSpeechDoneListener listener;

    public SpeechProgressListener(OnSpeechDoneListener listener) {
        super();
        this.listener = listener;
    }

    @Override
    public void onError(String utteranceId, int errorCode) {
        super.onError(utteranceId, errorCode);
        listener.onSpeechDone(false);
    }

    @Override
    public void onStop(String utteranceId, boolean interrupted) {
        super.onStop(utteranceId, interrupted);
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

    }

    @Override
    public void onDone(String utteranceId) {
        listener.onSpeechDone(true);
    }

    @Override
    public void onError(String utteranceId) {
        listener.onSpeechDone(false);
    }

    interface OnSpeechDoneListener{
        void onSpeechDone(boolean success);
    }
}
