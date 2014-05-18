package com.manga.feed.browser;

import com.manga.feed.MangaInfoHolder;

import android.content.Context;
import android.view.View;

public interface mangasite {
	
	public void getManga(Context c, Browser_BaseAdapter adapter);
	public void load(Context c, Browser_BaseAdapter adapter);
	public void searchDialog(final View v);
	public void gotoSite(Context c, MangaInfoHolder manga);

}
