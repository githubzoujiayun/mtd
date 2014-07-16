package cn.hugo.android.mtd.toolbox;

import android.util.Log;

public class MTDLog {
	private static String TAG = "MTD";

	public static boolean DEBUG = Log.isLoggable("MTD", Log.VERBOSE);

	/**
	 * 自定义日志的TAG标签
	 * 
	 * @param tag
	 */
	public static void setTag(String tag) {
		d("Changing log tag to %s", tag);
		TAG = tag;

		DEBUG = Log.isLoggable(TAG, Log.VERBOSE);
	}

	public static void v(String format, Object... args) {
		if (DEBUG) {
			Log.v(TAG, buildMessage(format, args));
		}
	}

	public static void d(String format, Object... args) {
		Log.d(TAG, buildMessage(format, args));
	}

	public static void e(String format, Object... args) {
		Log.e(TAG, buildMessage(format, args));
	}

	public static void e(Throwable tr, String format, Object... args) {
		Log.e(TAG, buildMessage(format, args), tr);
	}

	private static String buildMessage(String format, Object... args) {
		return args == null ? format : String.format(format, args);
	}

}
