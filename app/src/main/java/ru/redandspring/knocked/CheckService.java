package ru.redandspring.knocked;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import ru.redandspring.libs.MakeAsyncRequest;

public class CheckService extends Service {

    private final static String LOG_TAG = "TAG-CheckService";
    private final static String ENABLE_NOTIFICATIONS = "enable_notifications";
    private final static String INTERVAL_TIME = "interval_time";

    public enum ResultMode {
        BACKGROUND,
        BUTTON,
        CREATE_ACTIVITY
    }

    public CheckService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        MakeAsyncRequest request = new MakeAsyncRequest(this);
        String onlineService = "api-opencart-orders";
        String mode = intent.getAction();
        if (ResultMode.valueOf(mode) == ResultMode.BUTTON){
            //PendingIntent pi = intent.getParcelableExtra(MainActivity.PARAM_PINTENT);
        }

        request.execute(onlineService, mode);

        startAlarm();
        stopSelf();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startAlarm() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableNotifications = prefs.getBoolean(ENABLE_NOTIFICATIONS, false);

        if ( ! enableNotifications) return;

        String intervalTime = prefs.getString(INTERVAL_TIME, "15");

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, BootCompletedReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT );

        am.cancel(pendingIntent);
        // Устанавливаем разовое напоминание
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60L * 1000 * Long.parseLong(intervalTime, 10), pendingIntent);
    }
}
