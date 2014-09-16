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
                count += update(manga[x],context);
            return count;
        }

        @Override
        protected void onPostExecute(Integer count) {
            if(count >0)
                notification(count,context);
        }
    }

    /*
     * Used to connect to the internet and check if the manga updated
     */
    private int update(MangaInfoHolder manga, Context context)
    {
        int count = 0;
        //Used to store chapter number
        int chap = -1;

        // First set the default cookie manager.
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        URL url = null; //url that will be read from
        HttpURLConnection urlConnection =null; //connection to the url
        try {
            //url initialization
            url = new URL(manga.getSite());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            // Starts the query
            urlConnection.connect();
            int status = urlConnection.getResponseCode();
            //if not online or bad url
            if(!MethodHelper.isOnline(context) ||( status != HttpURLConnection.HTTP_ACCEPTED && status != HttpURLConnection.HTTP_OK))
                return 0;

            //inputstream with the html, then bufferreader to read data
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader read = new BufferedReader(new InputStreamReader(in, "iso-8859-1"), 8);
            String line;

            while((line=read.readLine()) != null)
            {
                Document doc = Jsoup.parse(line);
                Element info1 = doc.select("a[href]").first(); //get data for genre

                //chapter
                if (info1 != null)
                {
                    if(line.contains(manga.getTitle()))
                    {
                        //parsing for chapter number
                        String temp = doc.body().text().substring(manga.getTitle().length() +1); //remove title
                        temp = temp.substring(0,temp.indexOf(" ")); //remove anything behind the chapter number
                        int curr = Integer.parseInt(temp);
                        if(curr > chap)
                            chap = curr;
                    }
                }


                //break if all data is found to end loop
                //also use "Chapter" as an indicator to end (unique to mangapanda)
                if(line.contains("Chapter"))
                    break;

            }
            read.close();
            in.close();
        }catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            urlConnection.disconnect();
        }

        //write to file if there was an update
        if(Integer.parseInt(manga.getChapter()) < chap)
        {
            count++;
            MangaInfoHolder temp = manga;
            temp.setChapter(chap+"");
            temp.setUpdate('1');
            MethodHelper.writeFile(sdCard, temp,false);
        }else if(manga.getUpdate() == '1'){ //there was an update but haven't read it yet
            count++;
        }

        return count;
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
