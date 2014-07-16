package cn.hugo.android.mtd;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.conn.ConnectTimeoutException;

import cn.hugo.android.mtd.Mission.DownloadInfo;
import cn.hugo.android.mtd.exception.MTDError;
import cn.hugo.android.mtd.exception.NetworkError;
import cn.hugo.android.mtd.toolbox.MTDLog;

/**
 * 下载信息初始化线程
 * 
 * @author hugo
 * 
 */
class DLInfoInitThread extends Thread {

	private Mission mMission;
	private ProgressDelivery mDelivery;

	public DLInfoInitThread(Mission mission, ProgressDelivery delivery) {
		mMission = mission;
		mDelivery = delivery;
	}

	@Override
	public void run() {

		HttpURLConnection connection = null;
		RandomAccessFile desiredSaveFile = null;
		try {

			URL downloadUrl = mMission.getUrl();
			connection = ((HttpURLConnection) downloadUrl.openConnection());
			HttpHelper.setDefaultConnectionProperty(connection, mMission);
//			connection.setReadTimeout(mMission.getTimeoutMs());
			HttpHelper.setHttpHeaders(connection, null);

			if (mMission.isCanceled()) {
				return;
			}

			int fileSize = connection.getContentLength();
			if (fileSize > 0) {
				if (MTDLog.DEBUG) {
					MTDLog.d("got-content-length, url:%s [%d]",
							downloadUrl.toString(), fileSize);
				}

				// 两种情况处理，如果是路径，如果是指定文件
				File saveFile = mMission.getSaveFile();

				if (saveFile.isDirectory()) { // 如果是文件夹，则需要生成一个文件名，并更新保存了路径
					String fileName = getFileName(connection);
					saveFile = new File(saveFile.toString(), fileName);
					mMission.updateSaveFile(saveFile);
				}

				if (!saveFile.exists()) {
					// saveFile.delete(); 如果存在该文件，是否需要删除？
					saveFile.createNewFile();
				}

				desiredSaveFile = new RandomAccessFile(saveFile, "rwd");
				desiredSaveFile.setLength(fileSize);

				if (!mMission.isCanceled()) {

					// 通知下载准备工作完毕
					mDelivery.postPrepared(mMission, fileSize);

					desiredSaveFile.close();
					desiredSaveFile = null;

					startDownload(fileSize);
				}

			}
			else {

				mDelivery.postError(mMission, new NetworkError(
						"Cannot get file content length for url: "
								+ downloadUrl.toString()));
			}

		}
		catch (SocketTimeoutException e) {
			mDelivery.postError(mMission, new NetworkError(
					"socket timeout when initailize dl info.", e));
		}
		catch (ConnectTimeoutException e) {
			mDelivery.postError(mMission, new NetworkError(
					"connect timeout when initailize dl info.", e));
		}
		catch (IOException e) {
			mDelivery.postError(mMission, new MTDError(e));
			e.printStackTrace();
		}
		finally {
			try {
				if (desiredSaveFile != null) {
					desiredSaveFile.close();
					desiredSaveFile = null;
				}
			}
			catch (IOException e) {
				mDelivery
						.postError(
								mMission,
								new MTDError(
										"Cannot not close desiredSaveFile when initailize dl info.",
										e));
			}
			connection.disconnect();
		}

	}

	/**
	 * 开始下载
	 * 
	 * @param fileSize
	 */
	private void startDownload(int fileSize) {

		// 获取每个线程需要下载的块的大小
		List<DownloadInfo> downloadInfos = caculateBlockSize(fileSize);
		DownloadThread thread = null;
		for (int i = 0; i < mMission.getThreadPoolSize(); i++) {
			thread = new DownloadThread(mMission, downloadInfos.get(i),
					mDelivery);
			thread.setName("dl_thread_" + i);
			thread.start();
		}
	}

	/**
	 * 计算每块下载的大小
	 * 
	 * @param fileSize
	 * @return
	 */
	private List<DownloadInfo> caculateBlockSize(int fileSize) {
		int threadPoolSize = mMission.getThreadPoolSize();
		ArrayList<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
		int blocks = fileSize / mMission.getThreadPoolSize(); // 分块数
		for (int i = 0; i < threadPoolSize - 1; i++) {
			DownloadInfo info = new DownloadInfo(i * blocks, (i + 1) * blocks
					- 1);
			downloadInfos.add(info);
		}
		DownloadInfo info = new DownloadInfo((threadPoolSize - 1) * blocks,
				fileSize - 1);
		downloadInfos.add(info);

		return downloadInfos;
	}

	/**
	 * 获取文件名
	 * 
	 * @param conn
	 * @return
	 */
	private String getFileName(HttpURLConnection connection) {

		// 优先从请求url中获取文件名
		String downloadUrl = connection.getURL().toString().toLowerCase();
		String fileName = null;
		if (downloadUrl.lastIndexOf(".apk") != -1) {
			fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
		}
		else {
			fileName = UUID.randomUUID() + ".apk";
		}

		return fileName;

	}
}
