package com.manga.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * Class used as bridge between the delay and the actual update check
 */
public class MangaFeedUpdateAlarm extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent i) {
		// TODO Auto-generated method stub
		context.startService(new Intent(context, MangaFeedUpdateService.class));
	}

}
