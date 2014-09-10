package com.manga.feed;

import java.io.File;
import java.util.*;

import com.manga.feed.browser.Browser;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static int screenW, screenH;
	public static AssetManager mngr;
	public static SharedPreferences prefs;
	public static Resources res;
	public static File sdCard;
	public static ArrayList<MangaInfoHolder> mangas;
	public static HashMap<String, ArrayList<MangaInfoHolder>> browser; //holds the mangas so don't have to reload
	public static ListArrayAdapter adapter;
	public static final boolean DEVELOPMENT = false;
	private static final boolean TEST1 = false;
	
	private TextView text;
	private ListView listView;
	
	//notification manager
	public static NotificationManager nm;
	private static int uniqueID = 548156;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
        //if sd card isn't mounted, then quit application
        if (!(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState ()))) {
        	MethodHelper.popUp("Please mount sdcard before opening this app", getBaseContext(),Gravity.CENTER,Toast.LENGTH_LONG);
    		finish();
        	return;
        }
        
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.main_activity);
		
		//initializations
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenW = dm.widthPixels;
        screenH= dm.heightPixels;
        mngr = getAssets();
        prefs = PreferenceManager.getDefaultSharedPreferences(this); //get preferences
        res = getResources(); //get resources such as pictures
        sdCard= new File(Environment.getExternalStorageDirectory()+"/Manga Feed"); //folder in sd card
        mangas = new ArrayList<MangaInfoHolder>();
        browser = new HashMap<String, ArrayList<MangaInfoHolder>>();
        
        //create folders in sd card if doesn't exist
        if(!sdCard.exists())
        	sdCard.mkdir();

        if(TEST1)
        	testini();
        
        load();
         
        //Shows how many mangas there are
        text = (TextView)findViewById(R.id.homeTextView);
        if(mangas.size() <= 1)
        	text.setText(mangas.size()+" manga");
        else 
        	text.setText(mangas.size()+" mangas");
        
        
        //Button to access more mangas
        ((Button)findViewById(R.id.browseButton)).setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent myIntent = new Intent(MainActivity.this, Browser.class);
				MainActivity.this.startActivity(myIntent);
			}
        	
        });
        
        //list of mangas
		listView = (ListView)findViewById(R.id.homeListView);
		listView.setTextFilterEnabled(true);
		listView.setFastScrollEnabled(true);
		adapter = new ListArrayAdapter(this,1 ,mangas);
		listView.setAdapter(adapter);
		
		//set click listeners
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent myIntent = new Intent(MainActivity.this, MangaInfo.class);
				//if selected, remove updated flag and update file
				if(mangas.get(arg2).getUpdate() == '1')
				{
					mangas.get(arg2).setUpdate('0');
					MethodHelper.writeFile(sdCard, mangas.get(arg2),false);
					adapter.notifyDataSetChanged();
				}
				
				MangaInfo.manga = mangas.get(arg2);
				MangaInfo.showButton = false;
				MainActivity.this.startActivity(myIntent);
				
			}

		});
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				deletePopUp(arg2);
				return false;
			}
			
		});
          
		//notification
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(uniqueID);

		//start updater in background
        startService(new Intent(this,MangaFeedUpdateService.class));
	}
	
	/*
	 * Loads the data from the sdcard of the manga you're reading 
	 */
	protected void load()
	{
		File[] files = sdCard.listFiles();
        int completed = 0; //check where to put mangas in between
		for(File file: files)
		{
			if(file.isFile()) //check if it's a file
			{
				MangaInfoHolder manga = MethodHelper.readFile(file,res);
				if(manga.getUpdate() == '1') //put updated manga in front
					mangas.add(0, manga);
				else if(manga.getStatus().equals("c")) { //add completed mangas at end
                    mangas.add(manga);
                    completed++;
                }else
					mangas.add(mangas.size() == 0 ? 0 :
                            mangas.size() == 1 ? 1:
                            mangas.size()-completed
                            ,manga);
			}
		}
//        Collections.sort(mangas);
    }
	

	public static int getUniID()
	{
		int temp = uniqueID; // so doesn't change real ID
		return temp;
	}
	
	public static void logger(String tag, String msg)
	{
		if(DEVELOPMENT)
			Log.i("dev: "+tag, msg);
	}
	
	
	private void deletePopUp(final int get){
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    	builder.setMessage("Do you wish to delete this manga?")
    	       .setCancelable(false)
    	       .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	    String delete = MethodHelper.fixName(mangas.get(get).getTitle());
    	        	    MethodHelper.delete(new File(sdCard+"/"+delete)); //delete from sdCard
    	        	    mangas.remove(get); //delete from adapter
    	        	    adapter.notifyDataSetChanged();
    	        	    if(mangas.size() <= 1) //change number of manga
    	                	text.setText(mangas.size()+" manga");
    	                else 
    	                	text.setText(mangas.size()+" mangas");
    	                dialog.dismiss();
    	           }
    	       })
    	       .setPositiveButton("No", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.dismiss();
    	           }
    	       });
    	builder.show();
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mangas.size() <= 1)
        	text.setText(mangas.size()+" manga");
        else 
        	text.setText(mangas.size()+" mangas");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		MethodHelper.endPopUp();
		super.onBackPressed();
	}

/***************************************************************************************************************************************/
	
	/*
	 * Will translate different cases into binary for testload to read 
	 * 1. regular file
	 * 2. no cover art
	 * 3. no author
	 * 4. decimal chapter (27.5)
	 * 5. odd manga title (unique char)
	 * 6. no status
	 * 7. 2-6
	 * 8. 1-7 but w/ updated flag
	 * 9. title too long
	 * 10. genre too long
	 * title, author, genre, chapter, status, summary, char update, Bitmap cover
	 */
	private void testini()
	{
		//get test pic
		File pic = new File(sdCard+"/test/cover.jpg");
		if(!pic.isFile())
		{
			Log.e("Error", "No such file exist");
			return;
		}
		Bitmap cover = MethodHelper.decodeBitmapFromFile(pic.toString(), 200, 200);
		
		//no update flag
		mangas.add(new MangaInfoHolder("Test 1", "me", "shounen, hi, gender", "10", "c","none",'0',cover,"www.hi.com"));
		mangas.add(new MangaInfoHolder("Test 2", "me", "shounen, hi, gender", "10", "om","none",'0',null,"www.hi.com"));
		mangas.add(new MangaInfoHolder("Test 3", "", "shounen, hi, gender", "10", "o","none",'0',cover,"www.hi.com"));
		mangas.add(new MangaInfoHolder("Test 4", "me", "shounen, hi, gender", "10.5", "u","none",'0',cover,"www.hi.com"));
		mangas.add(new MangaInfoHolder("� Test 5", "me", "shounen, hi, gender", "10", "c","none",'0',cover,"www.hi.com"));
		mangas.add(new MangaInfoHolder("Test 6", "me", "shounen, hi, gender", "10", "","none",'0',cover,"www.hi.com"));
		mangas.add(new MangaInfoHolder("� Test 7", "", "shounen, hi, gender", "10.5", "","none",'0',null,"www.hi.com"));
		
		//update flag
		mangas.add(new MangaInfoHolder("Test 1.1", "me", "shounen, hi, gender", "10", "c","none",'1',cover,"www.hi.com"));
		mangas.add(new MangaInfoHolder("Test 2.1", "me", "shounen, hi, gender", "10", "om","none",'1',null,"www.hi.com"));
		mangas.add(new MangaInfoHolder("Test 3.1", "", "shounen, hi, gender", "10", "o","none",'1',cover,"www.hi.com"));
		mangas.add(new MangaInfoHolder("Test 4.1", "me", "shounen, hi, gender", "10.5", "u","none",'1',cover,"www.hi.com"));
		mangas.add(new MangaInfoHolder("� Test 5.1", "me", "shounen, hi, gender", "10", "c","none",'1',cover,"www.hi.com"));
		mangas.add(new MangaInfoHolder("Test 6.1", "me", "shounen, hi, gender", "10", "","none",'1',cover,"www.hi.com"));
		mangas.add(new MangaInfoHolder("Test 7.1 �", "", "shounen, hi, gender", "10.5", "","none",'1',null,"www.hi.com"));
		
		//other cases
		mangas.add(new MangaInfoHolder("Test 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9 9", 
				"me", "shounen, hi, gender", "10", "c","none",'1',cover,"www.hi.com"));
		mangas.add(new MangaInfoHolder("Test 10", "me", "shounen, hi, gender,shounen, hi, gender,shounen, hi, gender,shounen, hi, gender"
				, "10", "c","none",'1',cover,"www.hi.com"));
		
		for(MangaInfoHolder m: mangas)
			MethodHelper.writeFile(sdCard, m,false);
		
		//reset arraylist so no duplicates
		mangas.removeAll(mangas);
		
	}
	
}
