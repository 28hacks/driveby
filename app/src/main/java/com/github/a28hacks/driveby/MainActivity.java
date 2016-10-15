package com.github.a28hacks.driveby;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.a28hacks.driveby.model.GeoItem;
import com.github.a28hacks.driveby.model.WikipediaResult;
import com.github.a28hacks.driveby.network.WikipediaService;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQ_CODE = 1337;

    @BindView(R.id.btn_start_services)
    protected Button toggleServicesBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermissions();

        if (isMyServiceRunning(DrivebyService.class)) {
            toggleServicesBtn.setText("Stop Driveby");
        }

        toggleServicesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleServices();
            }
        });

        testApi();

    }

    private void testApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/w/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final WikipediaService service = retrofit.create(WikipediaService.class);

        service.getItemForLocation(10000, "53.080014|8.790364").enqueue(new Callback<WikipediaResult>() {
            @Override
            public void onResponse(Call<WikipediaResult> call, Response<WikipediaResult> response) {
                StringBuilder ids = new StringBuilder();
                for (GeoItem geoItem : response.body().getQuery().getItems()) {
                    Log.d(TAG, "onResponse: " + geoItem.getPageId() + " - "
                            + geoItem.getTitle() + ", " + geoItem.getDistance()
                            + ": " + geoItem.getExtract());
                    ids.append(geoItem.getPageId()).append("|");
                }

                service.getExtractText(ids.toString()).enqueue(new Callback<WikipediaResult>() {
                    @Override
                    public void onResponse(Call<WikipediaResult> call, Response<WikipediaResult> response) {
                        Log.d(TAG, "onFailure: " + call.request().url().toString());
                        Map<String, GeoItem> pages = response.body().getQuery().getPages();
                        for (String key : pages.keySet()) {
                            GeoItem item = pages.get(key);
                            Log.d(TAG, "onResponse: " + item.getTitle() + ": " + item.getExtract());
                        }
                    }

                    @Override
                    public void onFailure(Call<WikipediaResult> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + call.request().url().toString());
                        Log.e(TAG, "onFailure: ", t);
                    }
                });
            }

            @Override
            public void onFailure(Call<WikipediaResult> call, Throwable t) {

            }
        });
    }


    void toggleServices() {
        if (isMyServiceRunning(DrivebyService.class)) {
            stopService(new Intent(this, DrivebyService.class));
            toggleServicesBtn.setText("Start Driveby");
        } else {
            startService(new Intent(this, DrivebyService.class));
            toggleServicesBtn.setText("Stop Driveby");
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
}
