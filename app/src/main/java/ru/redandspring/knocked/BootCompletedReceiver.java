package ru.redandspring.knocked;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {
    public BootCompletedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context, CheckService.class);
        serviceIntent.setAction(String.valueOf(CheckService.ResultMode.BACKGROUND));
        context.startService(serviceIntent);
    }
}
