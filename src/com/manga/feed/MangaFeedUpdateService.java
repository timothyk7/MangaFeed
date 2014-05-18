package com.manga.feed;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.widget.Toast;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class MangaFeedUpdateService extends WakefulIntentService {

	private ArrayList<MangaInfoHolder> mangas;
	private File sdCard;
	
	public MangaFeedUpdateService() {
		super("MangaFeedUpdateService");
		sdCard= new File(Environment.getExternalStorageDirectory()+"/Manga Feed"); //folder in sd card
		mangas = new ArrayList<MangaInfoHolder>();
	}

	@Override
	protected void doWakefulWork(Intent arg0) {
		// check if there are any updates
		int count = 0;
		mangas.removeAll(mangas);
		load();
		
		//check for conectivity
		int retry = 1;
		while(MethodHelper.isOnline(getApplicationContext()) && retry <=10)
		{
			SystemClock.sleep(1000);
			retry++;
		}
		//update manga
		for(MangaInfoHolder manga : mangas)
			count += update(manga);
		MainActivity.logger("dowakefulwork", "Updated manga: "+count);
		if(count >0)
			notification(count);

	}
	
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
	
	/*
	 * Used to connect to the internet and check if the manga updated
	 */
	private int update(MangaInfoHolder manga)
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
			  if(!MethodHelper.isOnline(this) ||( status != HttpURLConnection.HTTP_ACCEPTED && status != HttpURLConnection.HTTP_OK))
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
	
	private void notification(int number)
	{
		//no number yet...
		String body = "Manga"+(number>1 ? "s": "")+" updated";
		String title = "Manga Feed";
		
		// Instantiate a Builder object.
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
			.setContentTitle(title)
	    	.setContentText(body)
	    	.setSmallIcon(R.drawable.ic_launcher);
		//set notification defaults
		builder.setDefaults(Notification.DEFAULT_ALL);
		// Creates an Intent for the Activity
		Intent notifyIntent =new Intent(this, MainActivity.class);
		// Sets the Activity to start in a new, empty task
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		// Creates the PendingIntent
		PendingIntent pendingIntent =
		        PendingIntent.getActivity(
		        this,
		        0,
		        notifyIntent,
		        PendingIntent.FLAG_UPDATE_CURRENT
		);

		// Puts the PendingIntent into the notification builder
		builder.setContentIntent(pendingIntent);
		MainActivity.nm =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE); //get notification manager so not null
		MainActivity.nm.notify(MainActivity.getUniID(), builder.build());
	}

}
