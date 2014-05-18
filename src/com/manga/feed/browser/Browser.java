package com.manga.feed.browser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import com.manga.feed.MainActivity;
import com.manga.feed.MangaInfoHolder;
import com.manga.feed.MethodHelper;
import com.manga.feed.R;

import android.app.*;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Browser extends FragmentActivity implements ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	
	//private String key = "MANGAPANDA"; //used to get correct manga ************************** change

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browser);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Removes the title on top
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	/*
	 * Used to search SQLite
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.browser, menu);
	    MenuItem searchItem = menu.findItem(R.id.action_search);
	    SearchView searchView = (SearchView) searchItem.getActionView();
	    /*
	    //get data from the query
	    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener( ) {

			@Override
			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				// TODO Auto-generated method stub
				//add search manga in listview
				if(query.length() <=4) //done to prevent long search
					return false;
				ArrayList<MangaInfoHolder> mangas = MainActivity.browser.get(key.toUpperCase());
				
				Log.i("hello", mangas+ " ");
				ArrayList<MangaInfoHolder> search = new ArrayList<MangaInfoHolder>();
				for(int x =0; mangas != null && x<mangas.size(); x++) //add mangas based on search
				{
					if(!mangas.get(x).getSite().equals("") 
							&& mangas.get(x).getTitle().toLowerCase().indexOf(query.toLowerCase()) != -1)
						search.add(mangas.get(x));
				}
				//Unique dialog generated every time you search
				final Dialog dialog= new Dialog(Browser.this);
		        LayoutInflater inflater = getLayoutInflater();
		        View convertView = (View) inflater.inflate(R.layout.browser_listview, null);
		        dialog.setContentView(convertView);
		        dialog.setTitle("Search");
		        
		        //reuse browser code of main listview
		        ListView lv = (ListView) convertView.findViewById(R.id.search_listview);
		        final Browser_BaseAdapter searchAdapter = new Browser_BaseAdapter(lv.getContext(),mangas);
		        lv.setAdapter(searchAdapter);
		        lv.setFastScrollAlwaysVisible(true);
		        lv.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
							long arg3) {
						   //Log.e("r",adapter.getData().get(arg2).getSite());
						   MangaInfoHolder manga= searchAdapter.getData().get(arg2);
						   mangasite searchSite = new mangapanda(); //NEED TO CHANGE********************
						   searchSite.gotoSite(arg1.getContext(), manga);
						   dialog.cancel();
					}
				});
		        
		        //button for close dialog
		        Button button = (Button) convertView.findViewById(R.id.search_button);
		        button.setText("Close");
		        button.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
		        dialog.show();
				return false;
			}
	        
	    });
	    */
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
	@Override
	public void finish() {
		Set<String> keys = MainActivity.browser.keySet();
		Iterator<String> iter = keys.iterator();
		//removing separators
		while(iter.hasNext())
		{
			String key = iter.next();
			for(int i=0; i<MainActivity.browser.get(key).size(); i++){
				if(MainActivity.browser.get(key).get(i).getSite().equals(""))
				{
					MainActivity.browser.get(key).remove(i); //remove separator so no duplicates
					i--;
				}
			}
		}
		super.finish();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		MethodHelper.endPopUp();
		super.onBackPressed();
	}

	/*************************************************************************************************************************************/
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	private class SectionsPagerAdapter extends FragmentPagerAdapter {
		private ArrayList<String> sites;
		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			sites = new ArrayList<String>();
			ini();
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new SectionFragment();
			Bundle args = new Bundle();
			args.putInt(SectionFragment.ARG_SECTION_NUMBER, position);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show total sites
			return sites.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if(!sites.isEmpty())
			{
				//key = sites.get(position); //used to find info for manga
				return sites.get(position);
			}
			return null;
		}
		
		/*
		 * Adds sites for the application
		 */
		private void ini()
		{
			sites.add("MangaPanda");
			//sites.add("MangaHere");
		}
	}

	/*************************************************************************************************************************
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class SectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		private ListView list;
		private Browser_BaseAdapter adapter;
		private mangasite site;
		
		//popUp
		private String sepIndicator="";
		private TextView text;
		private Toast popUp;
		public SectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			//initialization
			View rootView = inflater.inflate(R.layout.fragment_browser,
					container, false);
			final int pos = getArguments().getInt(ARG_SECTION_NUMBER); //get which fragment it's on
			site = pos ==0 ? new mangapanda() : null;
			if(site == null){return null;}
			
			//separator popup initialization
			text = new TextView(rootView.getContext());
			text.setTextSize(40);
			text.setTextColor(Color.WHITE);
			text.setBackgroundColor(Color.BLACK);
			
			//listview
			list = (ListView)rootView.findViewById(R.id.browser_listview);
			adapter = new Browser_BaseAdapter(rootView.getContext(),new ArrayList<MangaInfoHolder>()/*holder*/);
			site.getManga(rootView.getContext(), adapter); //load the data into the adapter
			list.setAdapter(adapter);
			
			//separator popup
			list.setFastScrollEnabled(true);
			list.setVerticalScrollBarEnabled(false);
			list.setOnScrollListener(new AbsListView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					// TODO Auto-generated method stub
					//Log.e(" ",firstVisibleItem+" "+visibleItemCount+" "+sepIndicator);
					if(adapter != null && adapter.getData() !=null && adapter.getData().size()>0){
						String t;
						if(adapter.getData().get(firstVisibleItem).getGenre().equals("") && adapter.getData().get(firstVisibleItem).getTitle().length()==3)
							   t = adapter.getData().get(firstVisibleItem).getTitle().substring("  ".length()
									, adapter.getData().get(firstVisibleItem).getTitle().length());
							else
							   t = adapter.getData().get(firstVisibleItem).getTitle().substring(0,1).toUpperCase();
						if(!t.equals(sepIndicator)){
							if(popUp ==null){
								popUp = new Toast(view.getContext());
								popUp.setDuration(Toast.LENGTH_SHORT);
								popUp.setView(text);
								popUp.setGravity(Gravity.CENTER, 0, 0);
							}
							sepIndicator=t;
							text.setText("  "+sepIndicator+"  ");
							popUp.show();
						}
						
					}
				}
			});
			
			//selecting in the listview
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					   //Log.e("r",adapter.getData().get(arg2).getSite());
					   if(popUp != null)
						   popUp.cancel();
					   MangaInfoHolder manga= adapter.getData().get(arg2);
					   site.gotoSite(getActivity(), manga);
					
				}
			});
			return rootView;
		}
	}

}

