package com.manga.feed.browser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
import java.util.Collections;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.manga.feed.MainActivity;
import com.manga.feed.MangaInfo;
import com.manga.feed.MangaInfoHolder;
import com.manga.feed.MethodHelper;
import com.manga.feed.R;
import com.manga.feed.browser.Browser.SectionFragment;

public class mangapanda implements mangasite{
	private final String KEY = "MANGAPANDA";
	private final String SITE = "http://www.mangapanda.com";
	private final String[] VARS ={"Status", "Author"};
	private final String[] GENRE = new String[]{"Action", "Adventure","Comedy","Demons","Drama","Echi"
		,"Fantasy","Gender Bender","Harem","Historical","Horror","Josei","Magic"
		,"Martial Arts","Mature","Mecha","Military","Mystery","One Shot","Psychological"
		,"Romance","School Life","Sci-Fi","Seinen","Shoujo","Shoujoai","Shounen","Shounenai"
		,"Slice of Life","Smut","Sports","Super Power","Supernatural","Tragedy","Vampire"
		,"Yaoi","Yuri"};
	
	//database
	private mangaDatabase data;

	@Override
	public mangaDatabase getManga(Context c, Browser_BaseAdapter adapter) {
		data= new mangaDatabase(c,KEY);
		data.open();
		ArrayList<MangaInfoHolder> mangas = data.getAllMangas(); // MainActivity.browser.get(KEY);
		if(mangas == null || mangas.size() == 0)
			load(c,adapter); 
		else //there is stuff inside
		{
			MainActivity.browser.put(KEY, mangas); //put into hashmap for storage
			adapter.addAllItem(mangas);
		}
		return data;
	}

	@Override
	public void load(Context c, Browser_BaseAdapter adapter) {
		new RetrieveAllManga(c,adapter).execute(new String[]{"http://www.mangapanda.com/alphabetical"});
	}
	
	@Override
	public void searchDialog(View v) {
		MethodHelper.popUp("Search", v.getContext(), Gravity.CENTER, Toast.LENGTH_SHORT);
	}

	@Override
	public void gotoSite(Context c, MangaInfoHolder manga) {
		if(!manga.getSite().equals("")) //check if valid manga
			new GetManga(c,manga).execute(new String[]{manga.getSite()});
	}
	
/***************************************************************************************************************************************/
	/*
	 * Will get all manga from mangapanda
	 *  -will only load the title of the manga, the status, and the site
	 */
	private class RetrieveAllManga extends AsyncTask<String, Void, ArrayList<MangaInfoHolder>> {
		private ProgressDialog progress;
	    private Context c;
	    private Browser_BaseAdapter adapter;
	    private int status=-1; //whether it's a good connection
		private long start, end =0; //for debugging
	    
	    public RetrieveAllManga(Context c, Browser_BaseAdapter adapter){
	    	this.c = c;
	    	this.adapter = adapter;
	    	progress = null;
	    }
		  
		@Override
		protected void onPreExecute() {
			//debugging
			if(MainActivity.DEVELOPMENT)
				start = System.nanoTime();
			
			progress = ProgressDialog.show(c, "","Loading...", true);
			super.onPreExecute();
		}
		
		@Override
		protected ArrayList<MangaInfoHolder> doInBackground(String... params) {
			// First set the default cookie manager.
			CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
			URL url = null; //url that will be read from
			HttpURLConnection urlConnection =null; //connection to the url
			ArrayList<MangaInfoHolder> mangas = new ArrayList<MangaInfoHolder>(); //hold the mangas
			
			try {
				  //url initialization
			      url = new URL(params[0]);
			      urlConnection = (HttpURLConnection) url.openConnection();
			      urlConnection.setReadTimeout(10000 /* milliseconds */);
			      urlConnection.setConnectTimeout(15000 /* milliseconds */);
			      urlConnection.setRequestMethod("GET");
			      // Starts the query
			      urlConnection.connect();
			      status = urlConnection.getResponseCode();
			      
			      //inputstream with the html, then bufferreader to read data
				  InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				  BufferedReader read = new BufferedReader(new InputStreamReader(in, "iso-8859-1"), 8);
				  String line;
				  
				  //variables to store MangaInfoHolder
				  String title=null,site=null,status="O";
				  //store previous string
				  String prev = "";
				  /*Extracting data*/
				  while((line=read.readLine()) != null)
				  {
					  Document doc = Jsoup.parse(line);
					  Element info1 = doc.select("a[href]").first(); //get data based on href tag
					  if(info1 != null) //tag exists
					  {
						  //valid site check
						  site =info1.attr("href").indexOf("/") != -1 && !info1.attr("href").equals("/") 
								  && !info1.attr("href").equals("/alphabetical") 
								  && !info1.attr("href").equals("/random") 
								  && !info1.attr("href").equals("/latest") 
								  && !info1.attr("href").equals("/popular") 
								  && !info1.attr("href").equals("/search") 
								  && !info1.attr("href").equals("http://www.memecenter.com") 
								  && !info1.attr("href").equals("/privacy") 
								  ? info1.attr("href") : null;
						  if(!doc.body().text().equals(prev)) //check for no duplicates
						  {
							  title = doc.body().text();
							  prev = title;
						  }
						  else
							  title = null;
						  status = title != null && title.contains("Completed") ? "C" : "O";
						  title = title != null && title.contains("Completed") ? title.substring(0, title.indexOf("[Com")): title;
					  }else{
						  //reset variables since href tag doesn't exist
						  title = null;
						  site = null;
					  }
					  if(site != null && title != null)
					  {
						  mangas.add(new MangaInfoHolder(title,"","","",status,"",'0',null,SITE+site));
						  data.createManga(title, status, SITE+site);
					  }
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
				
			return mangas;
		}

		@Override
		protected void onPostExecute(ArrayList<MangaInfoHolder> result) {
			//debugging
			if(MainActivity.DEVELOPMENT)
			{
				end = System.nanoTime();
				MainActivity.logger("mangapanda load", "sec: "+(end-start)/1000000000.0);
			}
			
			//if not online or bad url
			if(!MethodHelper.isOnline(c) ||( status != HttpURLConnection.HTTP_ACCEPTED && status != HttpURLConnection.HTTP_OK))
				MethodHelper.popUp("Connection Error", c, Gravity.CENTER, Toast.LENGTH_SHORT);
			
			//adding into the adapter
			Collections.sort(result);
			adapter.addAllItem(result);
			MainActivity.browser.put(KEY, result); //put into hashmap for storage
			progress.cancel();
		}
		
		
	
	}

/***************************************************************************************************************************************/
	/*
	 * Will go to the designated site and get all the data
	 *  -will only load the title of the manga, the status, and the site
	 */
	private class GetManga extends AsyncTask<String, Void, Void> {
		private ProgressDialog progress;
	    private Context c;
	    private MangaInfoHolder manga;
	    private int status=-1; //whether it's a good connection
		private long start, end =0; //for debugging
	    
	    public GetManga(Context c, MangaInfoHolder manga){
	    	this.c = c;
	    	this.manga = manga;
	    	progress = null;
	    }
		  
		@Override
		protected void onPreExecute() {
			//debugging
			if(MainActivity.DEVELOPMENT)
				start = System.nanoTime();
			
			progress = ProgressDialog.show(c, "","Loading...", true);
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(String... params) {	
			// First set the default cookie manager.
			CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
			URL url = null; //url that will be read from
			HttpURLConnection urlConnection =null; //connection to the url
			try {
				  //url initialization
			      url = new URL(params[0]);
			      urlConnection = (HttpURLConnection) url.openConnection();
			      urlConnection.setReadTimeout(10000 /* milliseconds */);
			      urlConnection.setConnectTimeout(15000 /* milliseconds */);
			      urlConnection.setRequestMethod("GET");
			      // Starts the query
			      urlConnection.connect();
			      status = urlConnection.getResponseCode();
			      
			      //inputstream with the html, then bufferreader to read data
				  InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				  BufferedReader read = new BufferedReader(new InputStreamReader(in, "iso-8859-1"), 8);
				  String line;
				  
				  //variables that need to be changed in the current manga
				  String author =null;
				  String status = null;
				  String genre =null;
				  String chapter =null;
				  String summary =null;
				  Bitmap cover =null;
				  String coverSite =null;
				  
				  //used to get status and author
				  int run = -1; //-1 = no existent
				  
				  //Used to store chapter number
				  int chap = -1;
				  
				  while((line=read.readLine()) != null)
				  {
					  Document doc = Jsoup.parse(line);
					  Element info1 = doc.select("a[href]").first(); //get data for genre
					  Element info2 = doc.select("img[src$=.jpg]").first(); //specific for coverart
					  Element info3 = doc.select("p").first(); //specific for summary
					  
					  //genre
					  if(genre == null  && info1 != null && checkGenre(line))
						  genre = doc.body().text();
					  
					  //cover
					  if(coverSite == null && info2 != null)
					  {
						  coverSite = info2.attr("src");
						  URL c = new URL(coverSite);
						  try
						  {
							  InputStream i = c.openConnection().getInputStream();
							  cover = BitmapFactory.decodeStream(i);
							  i.close();
						  }
						  catch(IOException ie) //if connection error, means no cover art
						  {
							  cover =  MethodHelper.decodeBitmapFromResource(MainActivity.res, R.drawable.noimage, MainActivity.screenW
									  , MainActivity.screenH);
						  }
					  }
					  
					  //summary
					  if(info3 != null)
						  summary = summary == null ? doc.body().text(): summary + doc.body().text();
					  
					  //author and status
					  if(run != -1 && line.contains("</td>") && (line.length() > "</td>".length()))
					  {
						  if(status == null && run ==0)
							  status = doc.body().text().equals("Ongoing") ? "o": 
								  doc.body().text().equals("Completed") ? "c": "u";
						  else if (author == null && run ==1)
							  author = doc.body().text();
						  run = -1;
					  }  
					  else if(line.contains("</td>") && (line.length() > "</td>".length()) )
						  run = line.contains(VARS[0]) ? 0: line.contains(VARS[1]) ? 1: -1;
					  
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
							  else
								  chapter = chap+"";
						  }
					  }
					  
					  
					  //break if all data is found to end loop
					  //also use "Chapter" as an indicator to end (unique to mangapanda)
					  if(author !=null && status != null && genre !=null && chapter !=null && summary !=null && cover !=null 
							  && coverSite !=null && line.contains("Chapter"))
						  break;
					  
				  }
				  //DEBUG
				  MainActivity.logger("GetManga 1", "Author: "+author+"| status: "+status);
				  MainActivity.logger("GetManga 2", "Genre: "+genre+"| Chapter: "+chapter+"| cover exist: "+(cover != null));
				  MainActivity.logger("GetManga 3", "Summary: "+summary);
				  
				  //set remaining variables
				  manga.setAuthor(author);
				  manga.setChapter(chapter);
				  manga.setStatus(status);
				  manga.setGenre(genre);
				  manga.setCover(cover);
				  manga.setSummary(summary);
				  
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
				
			return null;
		}
		
		private boolean checkGenre(String genre)
		{
			for(String g: GENRE)
			{
				if(genre.contains(g))
					return true;
			}
			return false;
		}
		
		private boolean checkMangaExist(String title)
		{
			for(MangaInfoHolder m: MainActivity.mangas)
			{
				if(m.getTitle().equals(title))
					return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Void result) {
			//debugging
			if(MainActivity.DEVELOPMENT)
			{
				end = System.nanoTime();
				MainActivity.logger("mangapanda getManga", "sec: "+(end-start)/1000000000.0);
			}
			
			//if not online or bad url
			if(!MethodHelper.isOnline(c) ||( status != HttpURLConnection.HTTP_ACCEPTED && status != HttpURLConnection.HTTP_OK))
				MethodHelper.popUp("Connection Error", c, Gravity.CENTER, Toast.LENGTH_SHORT);
			
			//change MangaInfo data if all valid fields
			if(manga.getAuthor() != null && manga.getChapter() != null && manga.getCover() != null && manga.getGenre() != null
					&& manga.getStatus() != null && manga.getSummary() != null)
			{
				Intent myIntent = new Intent(c, MangaInfo.class);
				MangaInfo.manga = manga;
				MangaInfo.showButton = checkMangaExist(manga.getTitle()) ? false: true;
				c.startActivity(myIntent);
			}
			
			progress.cancel();
		}
		
		
	
	}
}
