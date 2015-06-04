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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * 设置界面，包括检查版本更新、同步记录等功能。
 * @author Shen
 */
public class SetActivity extends Activity {

	// 更新版本要用到的一些信息
	private UpdateInfo info;
	private ProgressDialog progressDialog;
	UpdateInfoService updateInfoService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        Button button=(Button)findViewById(R.id.button1);
	    button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				checkUpdate();
			}
		});
	}
	
   private void checkUpdate(){
		Toast.makeText(SetActivity.this, "正在检查版本更新..", Toast.LENGTH_SHORT).show();
		// 自动检查有没有新版本 如果有新版本就提示更新
		new Thread() {
			public void run() {
				try {
					updateInfoService = new UpdateInfoService(SetActivity.this);
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
			if (updateInfoService.isNeedUpdate()) {
				showUpdateDialog();
			}
		};
	};

	//显示是否要更新的对话框
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
					Toast.makeText(SetActivity.this, "SD卡不可用，请插入SD卡",
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

	void downFile(final String url) { 
		progressDialog = new ProgressDialog(SetActivity.this);    //进度条，在下载的时候实时更新进度，提高用户友好度
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setTitle("正在下载");
		progressDialog.setMessage("请稍候...");
		progressDialog.setProgress(0);
		progressDialog.show();
		updateInfoService.downLoadFile(url, progressDialog,handler1);
	}
	
}