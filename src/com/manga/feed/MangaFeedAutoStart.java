package com.manga.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;

/**
 * Created by Timothy on 5/19/2014.
 */
public class MangaFeedAutoStart  extends BroadcastReceiver {
    private MangaFeedUpdateAlarm alarm= new MangaFeedUpdateAlarm();

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent startServiceIntent = new Intent(context, MangaFeedUpdateService.class);
            context.startService(startServiceIntent);
            Time time = new Time();
            time.setToNow();
            Log.i("MangaFeedAutoStart", "started: "+ time.toString());
        }
    }
}
