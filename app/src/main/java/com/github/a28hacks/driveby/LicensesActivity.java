package com.github.a28hacks.driveby;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by stefan on 16.10.16.
 */

public class LicensesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

         ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setTitle("Licenses");
        }
    }
}
