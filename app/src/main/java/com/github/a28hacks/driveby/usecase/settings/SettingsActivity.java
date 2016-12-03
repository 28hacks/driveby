package com.github.a28hacks.driveby.usecase.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.github.a28hacks.driveby.LicensesActivity;
import com.github.a28hacks.driveby.R;
import com.github.a28hacks.driveby.database.RealmProvider;
import com.github.a28hacks.driveby.model.database.GeoItem;
import com.github.a28hacks.driveby.model.database.InfoChunk;

import java.util.Locale;

import io.realm.Realm;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();

        setTitle(getString(R.string.activity_settings_title));
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    public static class SettingsFragment extends PreferenceFragment {

        private Realm mRealm;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            mRealm = RealmProvider.createRealmInstance(getActivity());

            addPrefClickListener(R.string.pref_key_history_action_delete,
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            showResetHistoryDialog();
                            return true;
                        }
                    });
            addPrefClickListener(R.string.pref_key_show_license_action,
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            showLicenses();
                            return true;
                        }
                    });

            addPrefChangeListener(R.string.pref_key_tss_language, new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(newValue instanceof String) {
                        setApplicationLocale((String) newValue);
                        return true;
                    }
                    return false;
                }
            });

        }

        public void setApplicationLocale(String lang) {
            Locale myLocale = new Locale(lang);
            Resources res = getResources();
            Configuration conf = res.getConfiguration();
            conf.setLocale(myLocale);
            res.updateConfiguration(conf, res.getDisplayMetrics());
            Intent refresh = new Intent(getActivity(), SettingsActivity.class);
            startActivity(refresh);
            getActivity().finish();
        }

        private void addPrefClickListener(int key, Preference.OnPreferenceClickListener listener) {
            findPreference(getString(key)).setOnPreferenceClickListener(listener);
        }

        private void showLicenses() {
            startActivity(new Intent(getActivity(), LicensesActivity.class));
        }

        private void addPrefChangeListener(int key, Preference.OnPreferenceChangeListener listener) {
            findPreference(getString(key)).setOnPreferenceChangeListener(listener);
        }

        private void showResetHistoryDialog() {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_reset_history_title)
                    .setMessage(R.string.dialog_reset_history_message)
                    .setPositiveButton(R.string.dialog_reset_history_keep, null)
                    .setNegativeButton(R.string.dialog_reset_history_reset,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    resetHistory();
                                }
                            })
                    .show();
        }

        private void resetHistory() {
            mRealm.beginTransaction();
            for (GeoItem item : mRealm.where(GeoItem.class).findAll()) {
                for (InfoChunk ic : item.getInfoChunks()) {
                    ic.setTold(false);
                }
                item.setFirstToldAbout(null);
            }
            mRealm.commitTransaction();
        }

    }
}
