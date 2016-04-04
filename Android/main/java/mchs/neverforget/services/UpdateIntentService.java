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
package mchs.neverforget.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import mchs.neverforget.LoginScreenActivity;
import mchs.neverforget.model.Book;
import mchs.neverforget.util.Crawler;
import mchs.neverforget.util.NeverForgetDatabaseAdapter;

/* This service might be started both when device completes boot
 * and periodically due to update preferences.
 * Both situations it is interesting to update crawler's username and password field
 * and reset all DOMs*/
public class UpdateIntentService extends IntentService {
    private static final String TAG = "UpdateIntentService";
    public static final String ACTION_UPDATE_DATABASE =
            "mchs.neverforget.services.action.UPDATE_DATABASE";
    private static final String SYNC_FREQUENCY_SHARED_PREFS_KEY = "sync_frequency";
    public static final String ACTION_CANCEL_RECURRENT_ALARM = "mchs.neverforget.action.CANCEL_ALARMS";
    private static final int NEXT_DAY_6 = -1;
    private static final int NEXT_DAY_12 = -2;
    private static final int NEXT_DAY_24 = -3;
    private NeverForgetDatabaseAdapter databaseAdapter;
    private Crawler crawler;

    public UpdateIntentService() {
        super("UpdateIntentService");
        databaseAdapter = new NeverForgetDatabaseAdapter(this);
        crawler = Crawler.getInstance();
    }

    // Schedules database updates based on user's sync_frequency preference
    public static void startActionRecurrentUpdateDatabase(Context context) {
        //Toast.makeText(context, "startActionRecurrentUpdateDatabase", Toast.LENGTH_LONG).show();
        String syncFrequencyString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SYNC_FREQUENCY_SHARED_PREFS_KEY, "-1");
        int syncFrequency = Integer.parseInt(syncFrequencyString);
        if (syncFrequency != -1) {
            Intent intent = new Intent(context, UpdateIntentService.class);
            intent.setAction(ACTION_UPDATE_DATABASE);
            PendingIntent pendingIntent = PendingIntent.getService(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            Calendar calendar = Calendar.getInstance();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            hourOfDay = defineTrigger(hourOfDay, syncFrequency);
            switch (hourOfDay) {
                case NEXT_DAY_6:
                    hourOfDay = 1;
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                    break;
                case NEXT_DAY_12:
                    hourOfDay = 7;
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                    break;
                case NEXT_DAY_24:
                    hourOfDay = 7;
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                    break;
            }
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, 0);
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR * syncFrequency,
                    pendingIntent);
        }
    }

    private static int defineTrigger(int hourOfDay, int mSyncFrequency) {
        switch (mSyncFrequency) {
            case 6:
                // 7 - 13 - 19 - 1 - 7
                if (hourOfDay < 1) {
                    return 1;
                } else if (hourOfDay < 7) {
                    return 7;
                } else if (hourOfDay < 13) {
                    return 13;
                } else if (hourOfDay < 19) {
                    return 19;
                }
                return NEXT_DAY_6;
            case 12:
                // 7 - 19 - 7
                if (hourOfDay < 7) {
                    return 7;
                } else if (hourOfDay < 19) {
                    return 19;
                } else return NEXT_DAY_12;
            case 24:
                // 7
                if (hourOfDay < 7) {
                    return 7;
                } else return NEXT_DAY_24;
        }
        return 7;
    }

    public static void startActionCancelRecurrentUpdateAlarm(Context context) {
        Intent intent = new Intent(context, UpdateIntentService.class);
        intent.setAction(ACTION_CANCEL_RECURRENT_ALARM);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_DATABASE.equals(action)) {
                handleActionUpdateDatabase();
            } else if (ACTION_CANCEL_RECURRENT_ALARM.equals(action)) {
                handleActionCancelAlarm();
            }
        }
    }

    private void handleActionUpdateDatabase() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        String mSyncFrequencyString = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SYNC_FREQUENCY_SHARED_PREFS_KEY, "-1");

        int mSyncFrequency = Integer.parseInt(mSyncFrequencyString);
        // Should check null because in air plan mode it will be null
        if (mSyncFrequency != -1 && netInfo != null && netInfo.isConnected()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String username = sharedPreferences.getString(LoginScreenActivity.USERNAME_PREFS, "");
            String password = sharedPreferences.getString(LoginScreenActivity.PASSWORD_PREFS, "");

            if (!(password.equals("") || password.equals(""))) {
                crawler.setPassword(password);
                crawler.setUsername(username);
                try {
                    ArrayList<Book> bookArrayList = new ArrayList<>(crawler.updateBooksList());
                    Log.v(TAG,"Book list was updated successfully");
                    databaseAdapter.clearDatabase();
                    for (Book book : bookArrayList) {
                        if (databaseAdapter.insertBook(book) <= 0)
                            Log.d(TAG,"Failed to insert book to database");
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG, "Failed to get updated list", e);
                }
            } else {
                Log.d(TAG,"Password and Username are not saved in shared preferences");
            }
        }
        Log.i(TAG,"Database was update successfully");
    }

    private void handleActionCancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, UpdateIntentService.class);
        intent.setAction(UpdateIntentService.ACTION_UPDATE_DATABASE);
        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.cancel(pendingIntent);
        Log.i(TAG,"Update recurrent alarm was cancelled");
    }
}