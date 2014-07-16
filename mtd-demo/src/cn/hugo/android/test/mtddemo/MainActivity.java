package cn.hugo.android.test.mtddemo;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.hugo.android.mtd.DownloadTask;
import cn.hugo.android.mtd.Mission;
import cn.hugo.android.mtd.exception.MTDError;

public class MainActivity extends Activity implements OnClickListener {

	public static final String APK_URL = "http://apkc.mumayi.com/2014/07/11/66/667901/ITzixun_V1.0.0_mumayi_e503c.apk";
	private Button downloadButton;
	private Button downloadCancel;
	private ProgressBar downloadProgress;
	private DownloadTask downloadTask;
	private File saveFile;
	private TextView download_cur_progress;
	private TextView download_totalSize;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();

		downloadTask = new DownloadTask();
		saveFile = getExternalCacheDir(getApplicationContext());

		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage("稍等");
		dialog.setTitle("初始化");
	}

	private void initView() {
		downloadButton = (Button) findViewById(R.id.download_start);
		downloadCancel = (Button) findViewById(R.id.download_cancel);
		downloadProgress = (ProgressBar) findViewById(R.id.download_progress);
		download_cur_progress = (TextView) findViewById(R.id.download_cur_progress);
		download_totalSize = (TextView) findViewById(R.id.download_total_size);

		downloadButton.setOnClickListener(this);
		downloadCancel.setOnClickListener(this);
	}

	public static boolean install(Context context, String filePath) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		File file = new File(filePath);
		if (file != null && file.length() > 0 && file.exists() && file.isFile()) {
			i.setDataAndType(Uri.parse("file://" + filePath),
					"application/vnd.android.package-archive");
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.download_cancel) {
			downloadTask.stop();
			dialog.dismiss();
		}

		if (v.getId() == R.id.download_start) {
			dialog.show();
			downloadTask.execute(new Mission(APK_URL, saveFile,
					new Mission.Listener() {

						@Override
						public void onProgress(File saveFile, int totalSize,
								int curSize) {
							downloadProgress.setProgress(curSize);
							download_cur_progress.setText(String
									.valueOf(curSize));
							if (totalSize == curSize) {
								install(getApplication(), saveFile.toString());
							}
						}

						@Override
						public void onError(MTDError error) {
							Log.e("调试", error.getMessage(), error);
							dialog.dismiss();
						}

						@Override
						public void onPrepared(int totalSize) {
							downloadProgress.setMax(totalSize);
							download_totalSize.setText(String
									.valueOf(totalSize));
							dialog.dismiss();
						}
					}));

		}
	}

	public static File getExternalCacheDir(final Context context) {
		if (hasExternalCacheDir()) {
			return context.getExternalCacheDir();
		}

		// Before Froyo we need to construct the external cache dir ourselves
		final String cacheDir = "/Android/data/" + context.getPackageName()
				+ "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath()
				+ cacheDir);
	}

	public static boolean hasExternalCacheDir() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}
}
