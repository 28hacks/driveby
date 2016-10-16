package com.github.a28hacks.driveby;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.a28hacks.driveby.audio.TextToSpeechService;
import com.github.a28hacks.driveby.database.RealmProvider;
import com.github.a28hacks.driveby.location.DrivebyService;
import com.github.a28hacks.driveby.model.database.GeoItem;
import com.github.a28hacks.driveby.model.database.InfoChunk;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQ_CODE = 1337;
    private static final int TTS_CHECK_CODE = 1338;

    private Realm mRealm;
    private TextToSpeechService ttsService;
    private boolean bound;


    @BindView(R.id.btn_start_services)
    protected Button toggleServicesBtn;
    @BindView(R.id.et_input)
    protected EditText etInput;
    @BindView(R.id.btn_speak)
    protected Button speakBtn;
    @BindView(R.id.btn_reset)
    protected Button resetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermissions();
        checkTTSVoices();

        mRealm = RealmProvider.createRealmInstance(this);

        if (isMyServiceRunning(DrivebyService.class)) {
            toggleServicesBtn.setText("Stop Driveby");
        }

        toggleServicesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleServices();
            }
        });
        speakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bound) {
                    speakText();
                }
            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetHistory();
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }


    private void checkTTSVoices() {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_CHECK_CODE);
    }

    private void resetHistory(){
        mRealm.beginTransaction();
        for(GeoItem item : mRealm.where(GeoItem.class).findAll()) {
            for(InfoChunk ic : item.getInfoChunks()) {
                ic.setTold(false);
            }
        }
        mRealm.commitTransaction();
    }

    void toggleServices(){
        if(isMyServiceRunning(DrivebyService.class)){
            // Unbind from the service
            if (bound) {
                unbindService(mConnection);
                bound = false;
            }
            stopService(new Intent(this, DrivebyService.class));
            stopService(new Intent(this, TextToSpeechService.class));
            toggleServicesBtn.setText("Start Driveby");
        } else {
            startService(new Intent(this, DrivebyService.class));
            startService(new Intent(this, TextToSpeechService.class));
            toggleServicesBtn.setText("Stop Driveby");

            // Bind to TTSService
            Intent intent = new Intent(this, TextToSpeechService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    void speakText() {
        String input = etInput.getText().toString();
        if(!input.isEmpty()){
            ttsService.speak(input);
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //always ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQ_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(this, DrivebyService.class));
            } else {
                //boo
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CHECK_CODE) {
            if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to TTSService, cast the IBinder and get TTSService instance
            TextToSpeechService.TTSBinder binder = (TextToSpeechService.TTSBinder) service;
            ttsService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
}
