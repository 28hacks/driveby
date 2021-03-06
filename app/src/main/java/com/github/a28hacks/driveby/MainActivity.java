package com.github.a28hacks.driveby;

import android.Manifest;
import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.github.a28hacks.driveby.audio.TextToSpeechService;
import com.github.a28hacks.driveby.location.DrivebyService;
import com.github.a28hacks.driveby.model.database.GeoItem;
import com.github.a28hacks.driveby.ui.NotificationController;
import com.github.a28hacks.driveby.ui.widget.DriveByWidgetProvider;
import com.github.a28hacks.driveby.usecase.history.HistoryAdapter;
import com.github.a28hacks.driveby.usecase.history.SectionedRecyclerViewAdapter;
import com.github.a28hacks.driveby.usecase.settings.SettingsActivity;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQ_CODE = 1337;
    private static final int TTS_CHECK_CODE = 1338;

    private final DateTimeFormatter dateFormat = DateTimeFormat.longDate()
                                                        .withLocale(Locale.getDefault());
    private Realm mRealm;
    private TextToSpeechService ttsService;
    private boolean bound;

    private HistoryAdapter mHistoryAdapter;

    @BindView(R.id.history_list)
    RecyclerView mHistoryList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermissions();
        checkTTSVoices();

        mRealm = Realm.getDefaultInstance();

        mHistoryAdapter = new HistoryAdapter();
        mHistoryList.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );

        //Add history adapter to the sectionAdapter
        final SectionedRecyclerViewAdapter mSectionedAdapter = new
                SectionedRecyclerViewAdapter(this,R.layout.history_section,R.id.section_text,mHistoryAdapter);

        //Apply this adapter to the RecyclerView
        mHistoryList.setAdapter(mSectionedAdapter);

        RealmResults<GeoItem> geoItems = mRealm.where(GeoItem.class)
                .isNotNull("firstToldAbout")
                .findAll()
                .sort("firstToldAbout", Sort.DESCENDING);

        mSectionedAdapter.setSections(calculateDailySections(geoItems));

        geoItems.addChangeListener(new RealmChangeListener<RealmResults<GeoItem>>() {
            @Override
            public void onChange(RealmResults<GeoItem> elements) {
                mSectionedAdapter.setSections(calculateDailySections(elements));
            }
        });

        mHistoryAdapter.setGeoItems(geoItems);
    }

    private SectionedRecyclerViewAdapter.Section[] calculateDailySections(RealmResults<GeoItem> items) {
        List<SectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();

        if(items.size() > 0) {
            DateTime currentDate = new DateTime(items.get(0).getFirstToldAbout());
            sections.add(new SectionedRecyclerViewAdapter.Section(0, dateFormat.print(currentDate)));

            DateTime comparisonDate;

            for (int i = 1; i < items.size(); i++) {
                GeoItem item = items.get(i);
                comparisonDate = new DateTime(item.getFirstToldAbout());

                if(!currentDate.withTimeAtStartOfDay()
                        .equals(comparisonDate.withTimeAtStartOfDay())) {
                    currentDate = comparisonDate;
                    sections.add(new SectionedRecyclerViewAdapter.Section(i, dateFormat.print(currentDate)));
                }
            }
        }

        return sections.toArray(new SectionedRecyclerViewAdapter.Section[0]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (isMyServiceRunning(DrivebyService.class)) {
            menu.findItem(R.id.toggle).setTitle("Stop");
            menu.findItem(R.id.toggle).setIcon(R.drawable.ic_hearing_white_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle:
                toggleServices();
                invalidateOptionsMenu();
                break;
            case R.id.settings:
                openSettings();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        Intent settingsIntent = SettingsActivity.createIntent(this);
        startActivity(settingsIntent);
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

    void toggleServices() {
        if (isMyServiceRunning(DrivebyService.class)) {
            // Unbind from the service
            if (bound) {
                unbindService(mConnection);
                bound = false;
            }
            stopService(new Intent(this, DrivebyService.class));
            stopService(new Intent(this, TextToSpeechService.class));
            new NotificationController(getApplicationContext()).dismissNotification();
            updateWidgets();
        } else {
            startService(new Intent(this, DrivebyService.class));
            startService(new Intent(this, TextToSpeechService.class));
            updateWidgets();

            // Bind to TTSService
            Intent intent = new Intent(this, TextToSpeechService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void updateWidgets() {
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), DriveByWidgetProvider.class));
        DriveByWidgetProvider myWidget = new DriveByWidgetProvider();
        myWidget.onUpdate(this, AppWidgetManager.getInstance(this), ids);
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
                //nice
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

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
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
