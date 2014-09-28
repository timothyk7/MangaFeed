package com.manga.feed;

import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListArrayAdapter extends ArrayAdapter<MangaInfoHolder> {
	private Context context;
	private  List<MangaInfoHolder> mangas;
	
	public ListArrayAdapter(Context context, int resource,
			List<MangaInfoHolder> objects) {
		super(context, resource, objects);
		this.context = context;
		this.mangas = objects;
	}
	
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView;
		ImageView imageView;
		
		//if null, create the view and create a holder else get the holder in memory 
		if(convertView ==null){
            LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.home_listrow, parent, false);
            textView = (TextView) convertView.findViewById(R.id.info);
            textView.setTextSize(MainActivity.screenW/40);
            imageView = (ImageView) convertView.findViewById(R.id.mangaCover);
            imageView.setMaxHeight(MainActivity.screenW/4);
            imageView.setMaxWidth(MainActivity.screenW/4);
            convertView.setTag(new Holder(imageView, textView));
		}else{
			Holder holder = (Holder) convertView.getTag();
			imageView = holder.getImage();
			textView = holder.getText();
		}
		textView.setText("Manga: "+fit(mangas.get(position).getTitle())+
				"\nGenre(s): "+fit(mangas.get(position).getGenre()));
		
		Bitmap bmp = mangas.get(position).getCover();
		bmp = mangas.get(position).getUpdate() == '1' ?
				overlay(bmp,MethodHelper.decodeBitmapFromResource(MainActivity.res, R.drawable.update,
						MainActivity.screenW/4, MainActivity.screenW/10)):
				bmp;	
						
		imageView.setImageBitmap(bmp);
		return convertView;
	}

	/* Fix
	 * Used to overlay the updated photo if needed
	 */
	private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        Matrix translate = new Matrix();
        translate.setTranslate(0, bmp1.getHeight()-bmp2.getHeight()); //translate overlay to bottom of image
        canvas.drawBitmap(bmp2, translate, null);
        return bmOverlay;
    }
	
	private String fit(String temp){
		String fit = "";
		if(temp.length() >MainActivity.screenW/40)
			fit = temp.substring(0, MainActivity.screenW/40-1)+"...";
		else
		    fit=temp;
		return fit;
	}
	
	/********************************************************************************************************************************/
	private static class Holder{
		ImageView image;
		TextView text;
		public Holder (ImageView image, TextView text){
			this.image = image;
			this.text = text;
		}
		public ImageView getImage() {
			return image;
		}
		public TextView getText() {
			return text;
		}
	}

}
