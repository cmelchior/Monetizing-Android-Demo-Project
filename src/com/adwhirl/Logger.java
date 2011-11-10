package com.adwhirl;

import android.util.Log;

public class Logger {

	public static void e(String tag,String msg){
		if( isLoggingEnabled()){
			Log.e(tag, (msg != null) ? msg : "null");
		}
	}

	public static void e(String tag,String msg, Throwable t){
		if( isLoggingEnabled()){
			Log.e(tag, (msg != null) ? msg : "null", t);
		}
	}
	
	public static void w(String tag,String msg){
		if( isLoggingEnabled()){
			Log.w(tag, (msg != null) ? msg : "null");
		}
	}

	public static void w(String tag,String msg, Throwable t){
		if( isLoggingEnabled()){
			Log.w(tag, (msg != null) ? msg : "null", t);
		}
	}

	
	public static void i(String tag,String msg){
		if( isLoggingEnabled()){
			Log.i(tag, (msg != null) ? msg : "null");
		}
	}

	public static void i(String tag,String msg, Throwable t){
		if( isLoggingEnabled()){
			Log.i(tag, (msg != null) ? msg : "null", t);
		}
	}

	public static void d(String tag,String msg){
		if( isLoggingEnabled()){
			Log.d(tag, (msg != null) ? msg : "null");
		}
	}

	public static void d(String tag,String msg, Throwable t){
		if( isLoggingEnabled()){
			Log.d(tag, (msg != null) ? msg : "null", t);
		}
	}

	
	/**
	 * We only want logging i debug mode
	 */
	private static boolean isLoggingEnabled() {
		return Const.DEBUG;
	}
}
