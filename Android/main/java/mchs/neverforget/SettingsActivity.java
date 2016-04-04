/*      Copyright 2016 Marcello de Paula Ferreira Costa

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License. */
package mchs.neverforget;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import mchs.neverforget.fragments.SettingsFragment;
import mchs.neverforget.services.NotificationIntentService;
import mchs.neverforget.services.UpdateIntentService;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.settings_fragment_frame, new SettingsFragment())
                .commit();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "sync_frequency":
                String syncFrequencyString = PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(key, "-1");
                System.out.println(syncFrequencyString);
                if (Integer.parseInt(syncFrequencyString) == -1) {
                    UpdateIntentService.startActionCancelRecurrentUpdateAlarm(this);
                } else {
                    UpdateIntentService.startActionCancelRecurrentUpdateAlarm(this);
                    UpdateIntentService.startActionRecurrentUpdateDatabase(this);
                }
                break;
            case "notification_time_picker1":
                NotificationIntentService.cancelCustomRecurrentNotificationAlarm(this, 1);
                NotificationIntentService.startCustomRecurrentNotificationAlarm(this, 1);
                break;
            case "notification_time_picker2":
                NotificationIntentService.cancelCustomRecurrentNotificationAlarm(this, 2);
                NotificationIntentService.startCustomRecurrentNotificationAlarm(this, 2);
                break;
            case "notification_time_picker3":
                NotificationIntentService.cancelCustomRecurrentNotificationAlarm(this, 3);
                NotificationIntentService.startCustomRecurrentNotificationAlarm(this, 3);
                break;
            case "enable_custom_timer1": {
                boolean isCustomNotificationsEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(key, true);
                if (isCustomNotificationsEnabled) {
                    NotificationIntentService.startCustomRecurrentNotificationAlarm(this, 1);
                } else {
                    NotificationIntentService.cancelCustomRecurrentNotificationAlarm(this, 1);
                }
                break;
            }
            case "enable_custom_timer2": {
                boolean isCustomNotificationsEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(key, false);
                if (isCustomNotificationsEnabled) {
                    NotificationIntentService.startCustomRecurrentNotificationAlarm(this, 2);
                } else {
                    NotificationIntentService.cancelCustomRecurrentNotificationAlarm(this, 2);
                }
                break;
            }
            case "enable_custom_timer3": {
                boolean isCustomNotificationsEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(key, false);
                if (isCustomNotificationsEnabled) {
                    NotificationIntentService.startCustomRecurrentNotificationAlarm(this, 3);
                } else {
                    NotificationIntentService.cancelCustomRecurrentNotificationAlarm(this, 3);
                }
                break;
            }
            case "enable_notifications":
                boolean isNotificationsEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(key, true);
                boolean isCustomNotificationsEnabled1 = PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean("enable_custom_timer1", true);
                boolean isCustomNotificationsEnabled2 = PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean("enable_custom_timer2", true);
                boolean isCustomNotificationsEnabled3 = PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean("enable_custom_timer3", true);

                if (isNotificationsEnabled) {
                    if (isCustomNotificationsEnabled1) {
                        NotificationIntentService.startCustomRecurrentNotificationAlarm(this, 1);
                    }
                    if (isCustomNotificationsEnabled2) {
                        NotificationIntentService.startCustomRecurrentNotificationAlarm(this, 2);
                    }
                    if (isCustomNotificationsEnabled3) {
                        NotificationIntentService.startCustomRecurrentNotificationAlarm(this, 3);
                    }
                } else {
                    if (isCustomNotificationsEnabled1) {
                        NotificationIntentService.cancelCustomRecurrentNotificationAlarm(this, 1);
                    }
                    if (isCustomNotificationsEnabled2) {
                        NotificationIntentService.cancelCustomRecurrentNotificationAlarm(this, 2);
                    }
                    if (isCustomNotificationsEnabled3) {
                        NotificationIntentService.cancelCustomRecurrentNotificationAlarm(this, 3);
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
