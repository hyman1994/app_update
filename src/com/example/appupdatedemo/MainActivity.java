package com.example.appupdatedemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

	// 更新版本要用到的一些信息
	private UpdateInfo info;
	private ProgressDialog pBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toast.makeText(MainActivity.this, "正在检查版本更新..", Toast.LENGTH_SHORT).show();
		// 自动检查有没有新版本 如果有新版本就提示更新
		new Thread() {
			public void run() {
				try {
					UpdateInfoService updateInfoService = new UpdateInfoService(
							MainActivity.this);
					info = updateInfoService.getUpDateInfo();
					handler1.sendEmptyMessage(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	@SuppressLint("HandlerLeak")
	private Handler handler1 = new Handler() {
		public void handleMessage(Message msg) {
			// 如果有更新就提示
			if (isNeedUpdate()) {
				showUpdateDialog();
			}
		};
	};

	private void showUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("请升级APP至版本" + info.getVersion());
		builder.setMessage(info.getDescription());
		builder.setCancelable(false);

		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					downFile(info.getUrl());
				} else {
					Toast.makeText(MainActivity.this, "SD卡不可用，请插入SD卡",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}

		});
		builder.create().show();
	}

	private boolean isNeedUpdate() {
		
		String v = info.getVersion(); // 最新版本的版本号
		Log.i("update",v);
		Toast.makeText(MainActivity.this, v, Toast.LENGTH_SHORT).show();
		if (v.equals(getVersion())) {
			return false;
		} else {
			return true;
		}
	}

	// 获取当前版本的版本号
	private String getVersion() {
		try {
			PackageManager packageManager = getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "版本号未知";
		}
	}

	void downFile(final String url) { 
		pBar = new ProgressDialog(MainActivity.this);    //进度条，在下载的时候实时更新进度，提高用户友好度
		pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pBar.setTitle("正在下载");
		pBar.setMessage("请稍候...");
		pBar.setProgress(0);
		pBar.show();
		new Thread() {
			public void run() {        
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					int length = (int) entity.getContentLength();   //获取文件大小
                                        pBar.setMax(length);                            //设置进度条的总长度
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					if (is != null) {
						File file = new File(
								Environment.getExternalStorageDirectory(),
								"Test.apk");
						fileOutputStream = new FileOutputStream(file);
						//这个是缓冲区，即一次读取10个比特，我弄的小了点，因为在本地，所以数值太大一下就下载完了,
						//看不出progressbar的效果。
                        byte[] buf = new byte[10];   
						int ch = -1;
						int process = 0;
						while ((ch = is.read(buf)) != -1) {       
							fileOutputStream.write(buf, 0, ch);
							process += ch;
							pBar.setProgress(process);       //这里就是关键的实时更新进度了！
						}

					}
					fileOutputStream.flush();
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
					down();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}.start();
	}

	void down() {
		handler1.post(new Runnable() {
			public void run() {
				pBar.cancel();
				update();
			}
		});
	}

	void update() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), "Test.apk")),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

}