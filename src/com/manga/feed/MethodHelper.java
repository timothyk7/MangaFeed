package com.manga.feed;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MethodHelper {
	private static Toast popUp = null;
	private static final int COMPRESS_QUALITY = 100;
	private static final int BEST_QUALITY = 75;
	private static final int COVER_SIZE = 400;

	/*
	 * Helper method used to display a pop-up
	 * Parameters:
	 * 	String message
	 *  Context of the activity where it's popping up
	 *  where it should appear on the screen (center, bottom, etc.)
	 *  how long the pop-up should last
	 */
    public static void popUp(String toast, Context context, int gravity, int length) {

		TextView text = new TextView(context);
		text.setText(toast);
		popUp = Toast.makeText(context, toast, length);
		popUp.setGravity(gravity, 0, 0);
		popUp.show();
	}

    public static void endPopUp()
    {
    	if(popUp != null)
    		popUp.cancel();
    }

    /*
     * Helper method to recursively delete files (works on folders too)
     */
	public static void delete(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            delete(child);
	    fileOrDirectory.delete();
	}

	/*
	 * Used to shrink consecutive chapters if they exist
	 * Ex: 1,2,3,4,7,8,9 -> 1-4,7-9
	 */
	public static String chapterShrink(ArrayList<Integer> nums){
		int largest=-1, smallest=Integer.MAX_VALUE; //used to store consecutive numbers
		String shrink = "";
		boolean ran = false;
		for (int x=0;x<nums.size();x++){
			//checking if the numbers are consecutive based on largest
			if(nums.get(x)>=largest && (x==0 || (x>0 && (nums.get(x-1)+1==nums.get(x)||nums.get(x-1)-1==nums.get(x)) || ran))){
				largest = nums.get(x);
				ran = false;
			}else if (x>0 && !(nums.get(x-1)+1==nums.get(x)||nums.get(x-1)-1==nums.get(x))){
				if(smallest ==largest)
					shrink = shrink+smallest+", "; //if not consecutive
				else
				    shrink = shrink+smallest+"-"+largest+", "; //if consecutive
			   largest=nums.get(x);
			   smallest=nums.get(x);
			   ran = true;
			}

			//checking if the numbers are consecutive based on smallest
			if(nums.get(x)<=smallest&& (x==0 || (x>0 && (nums.get(x-1)+1==nums.get(x)||nums.get(x-1)-1==nums.get(x))) || ran)){
				smallest = nums.get(x);
				ran = false;
			}else if (x>0 && !(nums.get(x-1)+1==nums.get(x)||nums.get(x-1)-1==nums.get(x))){
				if(smallest ==largest)
					shrink = shrink+smallest+", "; //if not consecutive
				else
				    shrink = shrink+smallest+"-"+largest+", "; //if consecutive
				largest=nums.get(x);
				smallest=nums.get(x);
				ran = true;
			}

			//before ending the loop, wrap up the shrink string
			if(x==nums.size()-1){
				if(smallest ==largest)
					shrink = shrink+smallest;
				else
				    shrink = shrink+smallest+"-"+largest;
			}

		}
		return shrink;
	}

	/*
	 * Convert the string to the start of the string being uppercase then the rest lowercase
	 * Ex: the great gatsby -> The Great Gatsby
	 */
	public static String convert(String orig){
		String convert = "";
		String temp = orig;
		while (temp.length() >0){
			int end = temp.indexOf(" ");
			if(end != -1){
			   convert = convert+temp.substring(0, 1)+temp.substring(1, end).toLowerCase()+" ";
			   temp = temp.substring(end+1);
			}else{
				convert = convert+temp.substring(0, 1)+temp.substring(1).toLowerCase();
				temp = "";
			}
		}
		return convert;
	}

	/*
	 * Used to fix a title if there is an illegal character
	 */
	public static String fixName(String orig){
		String temp = orig;
		int num =0;
		while(temp.indexOf("*") !=-1 || temp.indexOf("\"")!=-1|| temp.indexOf(":")!=-1|| temp.indexOf("?")!=-1
				|| temp.indexOf("\\")!=-1|| temp.indexOf("/")!=-1|| temp.indexOf("|")!=-1|| temp.indexOf("<")!=-1
				|| temp.indexOf(">")!=-1 || temp.indexOf(".") !=-1){

			num = temp.indexOf("*") !=-1 ? temp.indexOf("*")
			: temp.indexOf("\"")!=-1 ? temp.indexOf("\"")
			: temp.indexOf(":")!=-1 ? temp.indexOf(":")
			: temp.indexOf("?")!=-1 ? temp.indexOf("?")
			: temp.indexOf("\\")!=-1 ? temp.indexOf("\\")
			: temp.indexOf("/")!=-1 ? temp.indexOf("/")
			: temp.indexOf("|")!=-1 ? temp.indexOf("|")
			: temp.indexOf("<")!=-1 ? temp.indexOf("<")
			: temp.indexOf(">")!=-1 ? temp.indexOf(">")
			: temp.indexOf(".") !=-1 ? temp.indexOf(".")
			:0;

			if(num ==0){
			    temp = "_"+temp.substring(1);
			}else if(num==temp.length()){
				temp = temp.substring(num)+"_";
			}else{
				temp = temp.substring(0,num)+"_"+temp.substring(num+1);
			}

		}
		return temp;
	}

	/*
	 * Check if connected to the internet
	 */
	public static boolean isOnline(Context context) {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

	    return cm.getActiveNetworkInfo() != null &&
	       cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
/*********************************************************************************************************************/
    /*
     * Used to connect to the internet and check if the manga updated
     * Currently only for mangapanda
     */
    public static int update(File sdCard, MangaInfoHolder manga, Context context)
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
/*********************************************************************************************************************/
	/*
	 * Write to a file
	 * -updated, cover flag, size of info, information, size of cover, cover 
	 */
	public static void writeFile(File path, MangaInfoHolder manga, boolean compress)
	{
		//create file (fix name just in case)
		File file = new File(path.toString()+"/"+fixName(manga.getTitle()));
		if (!file.exists()) {
	        try {
	            file.createNewFile();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}

		//add data into the file
		OutputStream out = null;
	    ByteBuffer buffer; //used to write size of information and jpeg
	    try {
	      out = new BufferedOutputStream(new FileOutputStream(file));
	      String info = manga.getTitle()+"|*|"+manga.getAuthor()+"|*|"+manga.getGenre()+"|*|"+manga.getChapter()+"|*|"+
	    		  		manga.getStatus()+"|*|"+manga.getSummary()+"|*|"+manga.getSite()+"|*|";

	      //write whether there is an update
	      out.write(manga.getUpdate());

	      //whether a cover exists
	      if(manga.getCover() != null)
	    	  out.write(1);
	      else
	    	  out.write(0);

	      //information
	      byte [] information = info.getBytes();
	      buffer = ByteBuffer.allocate(4);
	      buffer.putInt(information.length);
	      out.write(buffer.array(), 0, 4); //# of bytes for information
	      out.write(information); //information

	      //picture
	      if(manga.getCover() != null)
	      {
	    	  //compress cover to jpeg and put into byte array
	    	  ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
              //whether to shrink or keep same quality
	    	  if(compress)
	    		  manga.getCover().compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, byteArrayBitmapStream);
	    	  else
	    		  manga.getCover().compress(Bitmap.CompressFormat.PNG, BEST_QUALITY, byteArrayBitmapStream);
	    	  byte[] b = byteArrayBitmapStream.toByteArray();

	    	  buffer = ByteBuffer.allocate(4);
	    	  buffer.putInt(b.length);
		      out.write(buffer.array(), 0, 4); //# of bytes for image
	    	  out.write(b); //write image into file
	      }

	      out.flush();
	      out.close();
	    }catch(IOException e){
		    e.printStackTrace();
	    }
        MLog.d("MethodHelper writeFile", "writing complete");
	}

	/*
	 * Read a file
	 * -updated, cover flag, size of info, information, size of cover, cover 
	 */
	public static MangaInfoHolder readFile(File path, Resources res)
	{
		//variables needed for MangaInfoHolder
		String title ="";
	    String author="";
		String genre="";
		String chapter="";
		String status = "u"; //o = ongoing | om = ongoing (monthly) | c = complete | u = unknown -> default u
		String summary="";
		char update='0';
	    Bitmap cover=null;
	    String site ="";

	    int coverFlag =-1; //flag whether there is a cover
	    int sizeI = -1; //size of the info
	    int sizeC =-1; //size of the cover art

	    //create file (fix name just in case)
  		File file = new File(path.toString());
  		if (!file.exists()) {
  			MLog.e("Error", "No such file exist");
			return null;
  		}

  		InputStream in = null;
  		byte [] num = null;
  		byte [] info = null;
  		byte [] pic = null;

  		//get all the information
  		try {
  			in = new BufferedInputStream(new FileInputStream(file));

  			update = (char)in.read(); //update flag

  			coverFlag = in.read(); //cover flag

  			num = new byte[4];
  			in.read(num, 0,4);
  			sizeI = byteArrayToInt(num); //size of the info string

  			//information
  			info = new byte[sizeI];
  			in.read(info, 0, sizeI);

  			if(coverFlag == 1)
  			{
  				num = new byte[4];
  				in.read(num,0,4);
  				sizeC = byteArrayToInt(num); //size of the cover

  			    //cover
  	  			pic = new byte[sizeC];
  	  			in.read(pic, 0, sizeC);
  			}

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

  		//translate all the information
  		if(info != null)
  		{
	  		try {
				String rawInfo = new String(info,"UTF-8");
				for(int x=0; x<7; x++)
				{
					int i = rawInfo.indexOf("|*|");
					if(x==0)
						title = rawInfo.substring(0,i);
					else if(x==1)
						author = rawInfo.substring(0,i);
					else if(x==2)
						 genre = rawInfo.substring(0,i);
					else if(x==3)
						chapter = rawInfo.substring(0,i);
					else if(x==4)
						status = rawInfo.substring(0,i);
					else if(x==5)
						summary = rawInfo.substring(0,i);
					else
						site = rawInfo.substring(0,i);

					rawInfo = rawInfo.substring(i+3); //update

				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  		}

  		cover = coverFlag == 1 ? decodeBitmapFromByteArray(pic,COVER_SIZE,COVER_SIZE) : res == null ? null:
  			decodeBitmapFromResource(res, R.drawable.noimage, COVER_SIZE,COVER_SIZE); //decode picture


  		MLog.v("1 MethodHelper readFile", "Reading complete- title: "+title+
  				"| author: "+author);
        MLog.v("2 MethodHelper readFile","genre: "+genre);
        MLog.v("3 MethodHelper readFile","chapter: "+chapter+"| status: "+status+
  				"| summary exists: "+(summary.length()!=0));
        MLog.v("4 MethodHelper readFile","updated: "+(update=='1')+"| cover exists: "+(cover != null));
        MLog.v("5 MethodHelper readFile","site: "+site);
        MLog.v("...","...");//space
		return new MangaInfoHolder(title, author,genre,chapter,status,summary,update,cover,site);
	}

	/*
	 * Used to get images stored on the app, like no cover art
	 */
	public static Bitmap decodeBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}

	/*
	 * Used to get images stored in a byte array
	 */
	public static Bitmap decodeBitmapFromByteArray(byte [] pic,int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeByteArray(pic, 0, pic.length,options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeByteArray(pic, 0, pic.length,options);
	}

	/*
	 * Helper for testing, decodes an image file
	 */
	public static Bitmap decodeBitmapFromFile(String file, int width, int height){
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(file);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, width, height);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(file,options);
	}

	/*
	 * Used for calculating how to scale image
	 */
	private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
        if (width > height) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        } else {
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }
    }
    return inSampleSize;
   }

   /*
    * conversion used in readfile
    */
   private static int byteArrayToInt(byte[] b)
   {
	    int value = 0;
	    for (int i = 0; i < 4; i++) {
	        int shift = (4 - 1 - i) * 8;
	        value += (b[i] & 0x000000FF) << shift;
	    }
	    return value;
   }


}
