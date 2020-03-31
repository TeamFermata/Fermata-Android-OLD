package site.fermata.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import static site.fermata.app.Constants.ACTION_RERUN;

public class ScanReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ||
                intent.getAction().equals(ACTION_RERUN)) {
            // Set the alarm here.

            boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AdvertiserService.class),
                    PendingIntent.FLAG_NO_CREATE) != null);
            if(alarmUp) return;

            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pendingIntent = PendingIntent.getForegroundService(context, 0 ,new Intent(context, AdvertiserService.class),0);
                context.startForegroundService(new Intent(context, AdvertiserService.class));
            } else {
                pendingIntent = PendingIntent.getService(context, 0 ,new Intent(context, AdvertiserService.class),0);
                context.startService(new Intent(context, AdvertiserService.class));

            }


            AlarmManager alarmManager = (AlarmManager)  context.getSystemService(context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                    AlarmManager.INTERVAL_HOUR, pendingIntent);

        }
    }
}
