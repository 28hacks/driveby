package com.github.a28hacks.driveby;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.github.a28hacks.driveby.audio.TextToSpeechService;
import com.github.a28hacks.driveby.location.DrivebyService;
import com.github.a28hacks.driveby.usecase.settings.UserSettings;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * @author Jonas Gerdes <dev@jonasgerdes.com>
 * @since 17-Nov-17
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        JodaTimeAndroid.init(this);

        setLocale(new UserSettings(this).getTTSLanguage());
    }

    private void setLocale(Locale locale) {
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        conf.setLocale(locale);
        res.updateConfiguration(conf, res.getDisplayMetrics());
    }

    public void stopServices() {
        stopService(new Intent(this, DrivebyService.class));
        stopService(new Intent(this, TextToSpeechService.class));
    }
}
