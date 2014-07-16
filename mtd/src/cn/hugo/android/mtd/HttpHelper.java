package cn.hugo.android.mtd;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

class HttpHelper {

	/**
	 * 设置头信息
	 * 
	 * @param connection
	 * @param headers
	 * @throws ProtocolException
	 */
	public static void setHttpHeaders(HttpURLConnection connection,
			Map<String, String> headers) throws ProtocolException {
		connection.setRequestMethod("GET");

		if (headers != null && headers.size() > 0) {
			Iterator<Entry<String, String>> iterator = headers.entrySet()
					.iterator();
			Entry<String, String> entry;
			while (iterator.hasNext()) {
				entry = iterator.next();
				connection.addRequestProperty(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * 设置连接属性
	 * 
	 * @param connection
	 */
	public static void setDefaultConnectionProperty(
			HttpURLConnection connection, Mission mission) {
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setConnectTimeout(mission.getTimeoutMs());
	}

}
