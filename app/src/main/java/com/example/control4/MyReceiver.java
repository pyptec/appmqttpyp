package com.example.control4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, MyService.class);
            pushIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startService(pushIntent);
            } else {
                context.startService(pushIntent);
            }
        }
        // TODO: This method is called when the BroadcastReceiver is receiving
    }

}
