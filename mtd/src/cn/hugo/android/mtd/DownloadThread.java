package cn.hugo.android.mtd;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.ConnectTimeoutException;

import cn.hugo.android.mtd.Mission.DownloadInfo;
import cn.hugo.android.mtd.exception.MTDError;
import cn.hugo.android.mtd.exception.NetworkError;
import cn.hugo.android.mtd.toolbox.MTDLog;

/**
 * 下载线程
 * 
 * @author hugo
 * 
 */
class DownloadThread extends Thread {

	private Mission mMission;

	private DownloadInfo mDownloadInfo;

	private ProgressDelivery mDelivery;

	public DownloadThread(Mission mission, DownloadInfo downloadInfo,
			ProgressDelivery delivery) {
		mDelivery = delivery;
		mMission = mission;
		mDownloadInfo = downloadInfo;
	}

	@Override
	public void run() {

		InputStream inputStream = null;
		RandomAccessFile saveFile = null;
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) mMission.getUrl().openConnection();

			HttpHelper.setDefaultConnectionProperty(connection, mMission);

			// http头
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Range", String.format("bytes=%s-%s",
					mDownloadInfo.startPos, mDownloadInfo.endPos));
			HttpHelper.setHttpHeaders(connection, headers);

			saveFile = new RandomAccessFile(mMission.getSaveFile(), "rwd");
			saveFile.seek(mDownloadInfo.startPos);

			if (mMission.isCanceled()) {

				if (MTDLog.DEBUG) {
					MTDLog.v("cancel-before-input [%s]", getName());
				}

				return; // TODO 会不会到finally分支？
			}

			// 开始读取数据
			inputStream = connection.getInputStream();
			byte[] buffer = new byte[4096];
			int length = -1;
			while ((length = inputStream.read(buffer)) != -1) {
				saveFile.write(buffer, 0, length);

				mDelivery.postProgress(mMission, length);

				if (mMission.isCanceled()) {
					if (MTDLog.DEBUG) {
						MTDLog.v("cancel-input-going [%s]", getName());
					}

					return;
				}
			}

		}
		catch (SocketTimeoutException e) {
			mDelivery.postError(mMission, new NetworkError(
					"socket timeout when download.", e));
		}
		catch (ConnectTimeoutException e) {
			mDelivery.postError(mMission, new NetworkError(
					"connect timeout when download.", e));
		}
		catch (IOException e) {
			mDelivery.postError(mMission, new MTDError(e));
		}
		finally {

			if (saveFile != null) {
				try {
					saveFile.close();
					saveFile = null;
				}
				catch (IOException e) {
					MTDLog.e(e, "Failed to close RandomAccessFile. [%s]",
							getName());
				}
			}

			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (IOException e) {
					MTDLog.e(e, "Failed to close inputstream. [%s]", getName());
				}

			}

		}
	}

}
