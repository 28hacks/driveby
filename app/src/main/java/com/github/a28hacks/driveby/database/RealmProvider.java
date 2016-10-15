package com.github.a28hacks.driveby.database;


import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmProvider {

    public static Realm createRealmInstance(Context context) {
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        return Realm.getInstance(config);
    }
}
