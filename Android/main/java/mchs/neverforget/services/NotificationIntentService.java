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

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import mchs.neverforget.NeverForgetActivity;
import mchs.neverforget.R;
import mchs.neverforget.model.Book;
import mchs.neverforget.util.NeverForgetDatabaseAdapter;

public class NotificationIntentService extends IntentService {
    private static final int NOTIFICATION_ID = 1;
    private static final String ACTION_NOTIFY_CUSTOM1 =
            "mchs.neverforget.services.action.NOTIFY_CUSTOM1";
    private static final String ACTION_NOTIFY_CUSTOM2 =
            "mchs.neverforget.services.action.NOTIFY_CUSTOM2";
    private static final String ACTION_NOTIFY_CUSTOM3 =
            "mchs.neverforget.services.action.NOTIFY_CUSTOM3";

    private ArrayList<Book> bookArrayList;

    public NotificationIntentService() {
        super("NotificationIntentService");
    }

    public static void startCustomRecurrentNotificationAlarm(Context context,int customId) {
        //Toast.makeText(context, "startCustomRecurrentNotificationAlarm"+customId, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, NotificationIntentService.class);
        if (customId == 1){
            intent.setAction(ACTION_NOTIFY_CUSTOM1);
        } else if (customId == 2){
            intent.setAction(ACTION_NOTIFY_CUSTOM2);
        } else if (customId == 3){
            intent.setAction(ACTION_NOTIFY_CUSTOM3);
        }

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        String[] hourOfDay = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("notification_time_picker"+customId, "12:00").split(":");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourOfDay[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(hourOfDay[1]));

        if (calendar.getTimeInMillis() < System.currentTimeMillis()){
            calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+1);
        }
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);

    }

    public static void cancelCustomRecurrentNotificationAlarm(Context context, int customId) {
        //Toast.makeText(context, "cancelCustomRecurrentNotificationAlarm"+customId, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, NotificationIntentService.class);
        if (customId == 1){
            intent.setAction(ACTION_NOTIFY_CUSTOM1);
        } else if (customId == 2){
            intent.setAction(ACTION_NOTIFY_CUSTOM2);
        } else if (customId == 3){
            intent.setAction(ACTION_NOTIFY_CUSTOM3);
        }

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_NOTIFY_CUSTOM1.equals(action)
                    || ACTION_NOTIFY_CUSTOM2.equals(action)
                    || ACTION_NOTIFY_CUSTOM3.equals(action)) {
                handleActionNotify();
            }
        }
    }

    private void handleActionNotify() {
        //Toast.makeText(this, "handleActionNotify", Toast.LENGTH_LONG).show();
        bookArrayList = new ArrayList<>();
        NeverForgetDatabaseAdapter databaseAdapter = new NeverForgetDatabaseAdapter(this);
        bookArrayList.addAll(databaseAdapter.getBooks());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        int expiredCounter = 0;
        int aboutToExpireCounter = 0;
        int i = 0;
        boolean[] expiredFlags = new boolean[bookArrayList.size()];
        String[] titles = new String[bookArrayList.size()];
        for (Book b : bookArrayList) {
            Calendar returnDate = new GregorianCalendar();
            Calendar currentDate = new GregorianCalendar();
            try {
                returnDate.setTime(sdf.parse(b.getReturnDate()));
                currentDate.setTime(sdf.parse(sdf.format(System.currentTimeMillis())));
                if (returnDate.before(currentDate)) {
                    expiredCounter++;
                    expiredFlags[i] = true;
                    titles[i] = b.getTitle();
                } else {
                    int days = NeverForgetActivity.daysBetween(returnDate, currentDate);
                    if (days < 5) {
                        aboutToExpireCounter++;
                        titles[i] = b.getTitle();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            i++;
        }

        if (aboutToExpireCounter > 0 || expiredCounter > 0) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            Intent subActivityIntent = new Intent(this, NeverForgetActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack
            stackBuilder.addParentStack(NeverForgetActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(subActivityIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent pendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            String contentText = null;
            if (expiredCounter + aboutToExpireCounter == 1) {
                int index = 0;
                for (int j = 0; j < titles.length; j++) {
                    if (titles[j] != null) {
                        contentText = titles[j];
                        index = j;
                    }
                }
                if (expiredCounter == 1) {
                    contentText = "(EXPIRADO)" + contentText;
                } else {
                    contentText = bookArrayList.get(index).getReturnDate()
                            + " - "
                            + contentText;
                }
            } else {
                contentText = (expiredCounter + aboutToExpireCounter) + " livros requerem sua atenção";
                notificationBuilder.setStyle(setupInboxStyle(titles, expiredFlags));
            }
            String ringtonePreferenceString = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("notification_ringtone", "DEFAULT_SOUND");
            notificationBuilder
                    .setTicker("Hey. Você precisa dar uma espiada nos seus empréstimos")
                    .setAutoCancel(true)
                    .setContentTitle("Never Forget")
                    .setContentText(contentText)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_stat_item_book)
                    .setSound(Uri.parse(ringtonePreferenceString));

            boolean shouldVibrate = PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean("notifications_vibrate", true);

            if (shouldVibrate) {
                notificationBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000});
            }
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private android.support.v4.app.NotificationCompat.Style setupInboxStyle(
            String[] titles, boolean[] expiredFlags) {

        android.support.v4.app.NotificationCompat.InboxStyle inboxStyle
                = new android.support.v4.app.NotificationCompat.InboxStyle();
        for (int i = 0; i < titles.length; i++) {
            if (titles[i] != null) {
                if (expiredFlags[i]) {
                    inboxStyle.addLine("(EXPIRADO) - " + titles[i]);
                } else {
                    inboxStyle.addLine(bookArrayList.get(i).getReturnDate() + " - " + titles[i]);
                }
            }
        }
        return inboxStyle;
    }

}