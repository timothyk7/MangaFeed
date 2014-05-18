package com.manga.feed.browser;

import java.util.ArrayList;
import java.util.TreeSet;

import com.manga.feed.MainActivity;
import com.manga.feed.MangaInfoHolder;
import com.manga.feed.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class Browser_BaseAdapter extends BaseAdapter {
	private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
    private ArrayList<MangaInfoHolder> mData;
    private Context c;
    
    private TreeSet<Integer> mSeparatorsSet;

    public Browser_BaseAdapter(Context c, ArrayList<MangaInfoHolder> mData){
    	this.c = c;
    	this.mData = mData;
    	mSeparatorsSet = new TreeSet<Integer>();
    	autoSeperator();
    }
	
    public void addItem(MangaInfoHolder item) {
        mData.add(item);
        notifyDataSetChanged();
    }
    
    public void addAllItem(ArrayList<MangaInfoHolder> item) {
        mData= item;
        mSeparatorsSet = new TreeSet<Integer>();
    	autoSeperator();
        notifyDataSetChanged();
    }
    
/*Separator Stuff*****************************************************************************************************/
    public void addSeparator(int pos,MangaInfoHolder sep) {
    	mData.add(pos,sep);
        // save separator position
        mSeparatorsSet.add(pos);
        notifyDataSetChanged();
    }
    
    public void autoSeperator(){
    	String temp ="";
    	String first ="";
    	if(mData == null)
    		return; //make sure not null
    	for (int x=0; x<mData.size();x++){
    		first = mData.get(x).getTitle().substring(0, 1);
    		if(!first.toLowerCase().equals(temp.toLowerCase())){
    			mData.add(x,new MangaInfoHolder("  "+first.toUpperCase(),"","","","","",'\0',null,""));
                // save separator position
                mSeparatorsSet.add(x);
                temp = first;
                x++;
                
    		}
    	}
    	notifyDataSetChanged();
    }
    
    public ArrayList<MangaInfoHolder> getData(){
    	return mData;
    }
    
    /*Holder*************************************************************************************************************/  
    private static class MangaHolder{
    	private TextView title, status;
		public MangaHolder(TextView title, TextView status){
			this.title = title;
			this.status = status;			
		}
		public TextView getTitle() {
			return title;
		}
		public TextView getStatus() {
			return status;
		}
		
	}
    
    private static class SeperatorHolder{
    	TextView text;
    	public SeperatorHolder(TextView text){
    		this.text = text;
    	}
		public TextView getText() {
			return text;
		}
    }

    /*BaseAdapter********************************************************************************************************/
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public int getItemViewType(int position) {
        return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }
    
    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView title=null, status=null, seperator=null;
        int type = getItemViewType(position);
        if(convertView ==null){
        	LayoutInflater inflater = (LayoutInflater) c
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	switch(type){
        	 case TYPE_ITEM:
                 convertView = inflater.inflate(R.layout.fragment_browser_listrow,parent, false);
                 title = (TextView)convertView.findViewById(R.id.browser_listrow_title);
                 status = (TextView)convertView.findViewById(R.id.browser_listrow_status);
                 title.setTextSize(MainActivity.screenW/60+3);
                 status.setTextSize(MainActivity.screenW/60);
                 convertView.setTag(new MangaHolder(title,status));
                 break;
             case TYPE_SEPARATOR:
                 convertView = inflater.inflate(R.layout.fragment_browser_sep,parent, false);
                 seperator = (TextView) convertView.findViewById(R.id.browser_sep);
                 seperator.setTextSize(MainActivity.screenW/60);
                 convertView.setTag(new SeperatorHolder(seperator));
                 break;
        	}
        }else{
        	switch(type){
           	 case TYPE_ITEM:
                    MangaHolder holder = (MangaHolder) convertView.getTag();
                    title = holder.getTitle();
                    status = holder.getStatus();
                    break;
                case TYPE_SEPARATOR:
                	SeperatorHolder holder2 = (SeperatorHolder) convertView.getTag();
                	seperator = holder2.getText();
                    break;
           	}
        }
        switch(type){
       	   case TYPE_ITEM:
                title.setText(mData.get(position).getTitle());
                status.setText(mData.get(position).getStatus().toUpperCase());
                break;
            case TYPE_SEPARATOR:
                seperator.setText(mData.get(position).getTitle());
                break;
       	}
        return convertView;
	}

}
