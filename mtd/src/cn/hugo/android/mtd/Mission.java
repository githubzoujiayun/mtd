package cn.hugo.android.mtd;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import cn.hugo.android.mtd.exception.MTDError;

public class Mission {

	/**
	 * 下载线程池大小
	 */
	private final static int DEFAULT_DOWNLOAD_THREAD_POOL_SIZE = 3;

	/**
	 * 连接超时时长
	 */
	private final static int DEFAULT_TIMEOUT_MS = 6000;

	/**
	 * true为终止本次任务
	 */
	private boolean mCanceled;

	private final URL mUrl;

	private int mTimeoutMs;

	// private final String mSavePath;

	private File mSaveFile;

	/**
	 * 下载线程池的大小
	 */
	private int mThreadPoolSize;

	/**
	 * 监听器
	 */
	private Listener mListener;

	private MTDError mError;

	public interface Listener {
		/**
		 * 下载准备完毕
		 * 
		 * @param totalSize
		 *            待下载文件的大小
		 */
		void onPrepared(int totalSize);

		/**
		 * 当下载过程中出错，回调该方法
		 * 
		 * @param error
		 */
		void onError(MTDError error);

		/**
		 * 下载进度更新，
		 * 
		 * @param totalSize
		 *            待下载文件的大小
		 * @param curSize
		 *            当前已经下载的大小，byte
		 */
		void onProgress(File saveFile, int totalSize, int curSize);

	}

	public Mission(String url, File saveFile, Listener listener) {
		this(url, saveFile, listener, DEFAULT_DOWNLOAD_THREAD_POOL_SIZE,
				DEFAULT_TIMEOUT_MS);
	}

	public Mission(String url, File saveFile, Listener listener,
			int threadPoolSize) {
		this(url, saveFile, listener, threadPoolSize, DEFAULT_TIMEOUT_MS);
	}

	public Mission(String url, File saveFile, Listener listener,
			int threadPoolSize, int timeoutMs) {

		try {
			mUrl = new URL(url);
		}
		catch (MalformedURLException e) {
			throw new IllegalArgumentException(String.format("Illegal url:%s",
					url), e);
		}
		mTimeoutMs = timeoutMs;
		mSaveFile = saveFile;
		mListener = listener;
		mThreadPoolSize = threadPoolSize;
	}

	/**
	 * 结束任务
	 */
	public void finish() {
		mCanceled = true;
	}

	public boolean isCanceled() {
		return mCanceled;
	}

	public URL getUrl() {
		return mUrl;
	}

	/**
	 * 获取连接超时时长
	 * 
	 * @return
	 */
	public int getTimeoutMs() {
		return mTimeoutMs;
	}

	public void setTimeoutMs(int timeoutMs) {
		mTimeoutMs = timeoutMs;
	}

	/**
	 * 更新文件保存路径
	 * 
	 * @param file
	 */
	public void updateSaveFile(File file) {
		mSaveFile = file;
	}

	public File getSaveFile() {
		return mSaveFile;
	}

	public int getThreadPoolSize() {
		return mThreadPoolSize;
	}

	public Listener getListener() {
		return mListener;
	}

	public MTDError getError() {
		return mError;
	}

	/**
	 * 关于下载的信息
	 * 
	 * @author hugo
	 * 
	 */
	static class DownloadInfo {

		public int startPos;

		public int endPos;

		/**
		 * 已经完成的字节数
		 */
		public int completeSize;

		/**
		 * 下载块的大小
		 */
		public int blockSize;

		public DownloadInfo(int startPos, int endPos) {
			this.startPos = startPos;
			this.endPos = endPos;
			this.blockSize = endPos - startPos + 1;
		}
	}

}
