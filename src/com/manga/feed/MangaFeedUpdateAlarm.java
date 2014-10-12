package com.manga.feed;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/*
 * Class used as bridge between the delay and the actual update check
 */
public class MangaFeedUpdateAlarm extends BroadcastReceiver {
    private ArrayList<MangaInfoHolder> mangas;
    private File sdCard;

    public MangaFeedUpdateAlarm() {
        sdCard= new File(Environment.getExternalStorageDirectory()+"/Manga Feed"); //folder in sd card
        mangas = new ArrayList<MangaInfoHolder>();
    }

    //start update sequence of every hour
	@Override
	public void onReceive(Context context, Intent i) {
		// TODO Auto-generated method stub
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        //start check for updates timed
        mangas.removeAll(mangas); //make sure mangas is empty
        load();
        new RetrieveFeedTask(context).execute(mangas.toArray(new MangaInfoHolder[mangas.size()]));

        wl.release();
	}

    /*Create initial alarm to start interval update*/
    public void setAlarm(Context context)
    {
        Intent update = new Intent(context, MangaFeedUpdateAlarm.class);
        update.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, update, 0);
        mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                AlarmManager.INTERVAL_HOUR,
                pi);
    }
/*Private Methods******************************************************************************************************/
    /*
	 * Loads the data from the sdcard of the manga you're reading
	 */
    protected void load()
    {
        File[] files = sdCard.listFiles();
        for(File file: files)
        {
            if(file.isFile()) //check if it's a file
            {
                MangaInfoHolder manga = MethodHelper.readFile(file,null);
                mangas.add(manga);
            }
        }
    }

    /*Done to run update*/
    private class RetrieveFeedTask extends AsyncTask<MangaInfoHolder, Void, Integer> {
        private Context context;

        private RetrieveFeedTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            //check for conectivity
            int retry = 1;
            while(!MethodHelper.isOnline(context) && retry <=5)
            {
                SystemClock.sleep(100);
                retry++;
            }
            if(retry >= 5){
                MLog.d("MangaFeedUpdateAlarm", "No Connection");
                return;
            }
            MLog.i("MangaFeedUpdateAlarm", "checking for updates");
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(MangaInfoHolder... manga) {
            int count = 0;
            //update manga
            for(int x=0; x<manga.length;x++)
                count += MethodHelper.update(sdCard,manga[x],context);
            return count;
        }

        @Override
        protected void onPostExecute(Integer count) {
            if(count >0)
                notification(count,context);
        }
    }


    private void notification(int number, Context context)
    {
        //no number yet...
        String body = "Manga"+(number>1 ? "s": "")+" updated";
        String title = "Manga Feed";

        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_launcher);
        //set notification defaults
        builder.setDefaults(Notification.DEFAULT_ALL);
        // Creates an Intent for the Activity
        Intent notifyIntent =new Intent(context, MainActivity.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Puts the PendingIntent into the notification builder
        builder.setContentIntent(pendingIntent);
        MainActivity.nm =  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); //get notification manager so not null
        MainActivity.nm.notify(MainActivity.getUniID(), builder.build());
    }

}
