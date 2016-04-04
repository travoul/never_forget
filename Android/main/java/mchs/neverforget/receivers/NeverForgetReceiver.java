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
package mchs.neverforget.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import mchs.neverforget.services.NotificationIntentService;
import mchs.neverforget.services.UpdateIntentService;

public class NeverForgetReceiver extends BroadcastReceiver {
    private static final String ACTION_FIRST_LOGIN = "FIRST_LOGIN";

    public NeverForgetReceiver() {
    }

    // helper method
    public static void broadcastFirstLogin(Context context) {
        Intent intent = new Intent(context, NeverForgetReceiver.class);
        intent.setAction(ACTION_FIRST_LOGIN);
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action){
            case Intent.ACTION_BOOT_COMPLETED:
                handleActionBootCompleted(context);
                break;
            case ACTION_FIRST_LOGIN:
                handleActionFirstLogin(context);
                break;
        }
    }

    private void handleActionBootCompleted(Context context) {
        boolean isNotificationsEnabled = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("enable_notifications", true);
        int syncFreq = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("sync_frequency", 24);
        boolean shouldSync = false;
        if (syncFreq != -1) {
            shouldSync = true;
        }
        if (isNotificationsEnabled) {
            boolean isCustomNotificationsEnabled1 = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("enable_custom_timer1", true);
            boolean isCustomNotificationsEnabled2 = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("enable_custom_timer2", false);
            boolean isCustomNotificationsEnabled3 = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("enable_custom_timer3", false);
            if (isCustomNotificationsEnabled1) {
                NotificationIntentService.startCustomRecurrentNotificationAlarm(context, 1);
            }
            if (isCustomNotificationsEnabled2) {
                NotificationIntentService.startCustomRecurrentNotificationAlarm(context, 2);
            }
            if (isCustomNotificationsEnabled3) {
                NotificationIntentService.startCustomRecurrentNotificationAlarm(context, 3);
            }
        }
        if (shouldSync) {
            UpdateIntentService.startActionRecurrentUpdateDatabase(context);
        }
    }

    private void handleActionFirstLogin(Context context) {
        boolean isNotificationsEnabled = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("enable_notifications", true);
        boolean isCustomNotificationsEnabled1 = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("enable_custom_timer1", true);
        boolean isCustomNotificationsEnabled2 = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("enable_custom_timer2", false);
        boolean isCustomNotificationsEnabled3 = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("enable_custom_timer3", false);
        int syncFreq = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString("sync_frequency", "24"));

        boolean shouldSync = false;
        if (syncFreq != -1) {
            shouldSync = true;
        }

        if (isNotificationsEnabled) {
            if (isCustomNotificationsEnabled1) {
                NotificationIntentService.startCustomRecurrentNotificationAlarm(context, 1);
            }
            if (isCustomNotificationsEnabled2) {
                NotificationIntentService.startCustomRecurrentNotificationAlarm(context, 2);
            }
            if (isCustomNotificationsEnabled3) {
                NotificationIntentService.startCustomRecurrentNotificationAlarm(context, 3);
            }
        } else {
            if (isCustomNotificationsEnabled1) {
                NotificationIntentService.cancelCustomRecurrentNotificationAlarm(context, 1);
            }
            if (isCustomNotificationsEnabled2) {
                NotificationIntentService.cancelCustomRecurrentNotificationAlarm(context, 2);
            }
            if (isCustomNotificationsEnabled3) {
                NotificationIntentService.cancelCustomRecurrentNotificationAlarm(context, 3);
            }
        }
        if (shouldSync) {
            UpdateIntentService.startActionRecurrentUpdateDatabase(context);
        }
    }
}
