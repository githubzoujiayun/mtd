package cn.hugo.android.mtd;

import android.os.Handler;
import android.os.Looper;

public class DownloadTask {

	/**
	 * 正在执行的任务
	 */
	private Mission mFlghtingMission;

	public DownloadTask() {
	}

	/**
	 * 执行下载任务
	 * 
	 * @param mission
	 */
	public void execute(Mission mission) {
		// 先把正在执行的下载任务结束
		stop();

		mFlghtingMission = mission;

		// 启动下载信息初始化线程
		new DLInfoInitThread(mission, new ProgressDelivery(
				new Handler(Looper.getMainLooper()))).start();

	}

	public void stop() {
		if (mFlghtingMission != null) {
			mFlghtingMission.finish();
			mFlghtingMission = null;
		}
	}

}
