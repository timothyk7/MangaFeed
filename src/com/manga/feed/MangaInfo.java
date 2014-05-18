package com.manga.feed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class MangaInfo extends Activity{
	 //changed in MainActivity/ Browser
	public static MangaInfoHolder manga;
	public static boolean showButton = true;
	
	private ArrayList<TextView> info; //holds all the textviews that will be displayed
	private final String[] optionsS = new String[] {"o","om","c", "u"};
	private final String[] options = new String[] {"Ongoing","Ongoing (Monthly)" ,"Completed", "Unknown"};
	private Spinner status;
	private Button button;
	private ImageView mangaCover;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.manga_info);
		
		//textviews that will be displayed
		info = new ArrayList<TextView>();
		info.add((TextView) findViewById(R.id.infoName));
		info.add((TextView) findViewById(R.id.infoArthur));
		info.add((TextView) findViewById(R.id.infoGenre));
		info.add((TextView) findViewById(R.id.infoChapters));
		info.add((TextView) findViewById(R.id.infoSummary));
		info.add((TextView) findViewById(R.id.info0));
		info.add((TextView) findViewById(R.id.info1));
		info.add((TextView) findViewById(R.id.info2));
		info.add((TextView) findViewById(R.id.info3));
		info.add((TextView) findViewById(R.id.info4));
		info.add((TextView) findViewById(R.id.info5));
		
		//status
		status = (Spinner)findViewById(R.id.infoStatus);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	            android.R.layout.simple_spinner_item, options);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		status.setAdapter(adapter);
		status.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			 public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
				//write to file if user changes status
				if(!manga.getStatus().equals(optionsS[position]))
				{
					manga.setStatus(optionsS[position]);
					MainActivity.adapter.notifyDataSetChanged();
					MethodHelper.writeFile(MainActivity.sdCard, manga,false);
				}
				
			 }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//imageview
		mangaCover = (ImageView) findViewById(R.id.mangaMenuCover);
		
		//Button
		button = (Button)findViewById(R.id.infoButton);
		button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				MethodHelper.writeFile(MainActivity.sdCard, manga,true);
				
				//change adapter's data (mangas) 
				MainActivity.mangas.add(manga);
				MainActivity.adapter.notifyDataSetChanged();
				
				//make button invisible so can't add again
				button.setClickable(false);
				button.setTextColor(getResources().getColor(R.color.Gray));
			}
			
		});
		
		//initialize
		ini(); //textViews
		//spinner
		if(manga.getStatus().equals("o")){
			status.setSelection(0);
		}else if(manga.getStatus().equals("om")){
			status.setSelection(1);
		}else if(manga.getStatus().equals("c")){
			status.setSelection(2);
		}else{
			status.setSelection(3);
		}
		mangaCover.setImageBitmap(manga.getCover()); //imageview
		if(!showButton)
			button.setVisibility(View.GONE);
	}
	
	protected void ini(){
		info.get(0).setText(manga.getTitle());
		info.get(1).setText(manga.getAuthor());
		info.get(2).setText(manga.getGenre());
		info.get(3).setText(manga.getChapter());
		info.get(4).setText(manga.getSummary());
		for(TextView i : info)
			i.setTextSize(MainActivity.screenW/40);
	}

}
