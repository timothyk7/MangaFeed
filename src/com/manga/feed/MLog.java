package com.manga.feed;

import android.util.Log;
/**
 * Created by Tim on 9/10/14.
 */
public class MLog{

    /***Logging level
     1 - verbose
     2 - debug
     3 - info
     4 - warn
     5 - error
     ***/
    private static final int V = 1;
    private static final int D = 2;
    private static final int I = 3;
    private static final int W = 4;
    private static final int E = 5;

    private static final int LOG = 3;

    public static void v(String tag, String msg){
        if(LOG <= V)
            Log.v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr){
        if(LOG <= V)
            Log.v(tag, msg, tr);
    }

    public static void d(String tag, String msg){
        if(LOG <= D)
            Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr){
        if(LOG <= D)
            Log.d(tag, msg, tr);
    }

    public static void i(String tag, String msg){
        if(LOG <= I)
            Log.i(tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr){
        if(LOG <= I)
            Log.i(tag, msg, tr);
    }

    public static void w(String tag, String msg){
        if(LOG <= W)
            Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr){
        if(LOG <= W)
            Log.w(tag, msg, tr);
    }

    public static void e(String tag, String msg){
        if(LOG <= E)
            Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr){
        if(LOG <= E)
            Log.e(tag, msg, tr);
    }
}
