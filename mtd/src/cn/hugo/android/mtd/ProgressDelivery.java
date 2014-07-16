package cn.hugo.android.mtd;

import android.os.Handler;
import android.os.SystemClock;
import cn.hugo.android.mtd.Mission.Listener;
import cn.hugo.android.mtd.exception.MTDError;

class ProgressDelivery {

	private static int DEFAULT_UPDATE_TIME = 1000;

	private long mLastUpdateTime;

	private Handler mHandler;

	private int mFileSize;

	private int mReceiveSize;

	private enum DeliveryType {
		DILIVERY_PROGRESS, // 更新进度
		DILIVERY_PREPARED, // 下载准备完毕，开始下载
		DILIVERY_ERROR; // 下载过程中发生错误
	}

	public ProgressDelivery(Handler handler) {
		mHandler = handler;
	}

	public synchronized void postProgress(Mission mission, int refreshSize) {

		mReceiveSize += refreshSize;

		if (isUpdateProgressNow() || mReceiveSize == mFileSize) {
			mLastUpdateTime = SystemClock.elapsedRealtime();
			mHandler.post(new DeliveryRunnable(mission,
					DeliveryType.DILIVERY_PROGRESS));
		}
	}

	/**
	 * 是否需要对UI进行更新
	 * 
	 * @return
	 */
	private boolean isUpdateProgressNow() {
		return SystemClock.elapsedRealtime() >= mLastUpdateTime
				+ DEFAULT_UPDATE_TIME;
	}

	public synchronized void postError(Mission mission, MTDError error) {
		mHandler.post(new DeliveryRunnable(mission, DeliveryType.DILIVERY_ERROR));
	}

	/**
	 * 
	 * 
	 * @param mission
	 * @param fileSize
	 */
	public void postPrepared(Mission mission, int fileSize) {
		mFileSize = fileSize;
		mHandler.post(new DeliveryRunnable(mission,
				DeliveryType.DILIVERY_PREPARED));
	}

	// TODO 需要一秒再刷新一次
	private class DeliveryRunnable implements Runnable {

		private DeliveryType mType;
		private Mission mMission;

		public DeliveryRunnable(Mission mission, DeliveryType type) {
			mMission = mission;
			mType = type;
		}

		@Override
		public void run() {

			Listener listener = mMission.getListener();

			switch (mType) {
				case DILIVERY_PREPARED:
					if (listener != null && !mMission.isCanceled()) {
						listener.onPrepared(mFileSize);
					}
					break;

				case DILIVERY_PROGRESS:
					if (listener != null && !mMission.isCanceled()) {
						listener.onProgress(mMission.getSaveFile(), mFileSize,
								mReceiveSize);
					}
					break;

				case DILIVERY_ERROR:
					if (listener != null && !mMission.isCanceled()) {
						listener.onError(mMission.getError());
						mMission.finish();
					}
					break;

				default:
					break;
			}

		}
	}

}
