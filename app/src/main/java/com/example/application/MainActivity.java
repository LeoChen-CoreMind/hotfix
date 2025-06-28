package com.example.application;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import java.io.File;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import android.content.Intent;
import android.widget.TextView;
import android.os.Looper;
import android.os.Handler;
import android.content.ClipboardManager;
import android.content.ClipData;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//TextView wb = findViewById(R.id.bug);
		//wb.setText("修复完成");
		Button button = findViewById(R.id.bo);
		//Toast.makeText(getApplication(), getApplication()., Toast.LENGTH_SHORT).show();
		button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					String dexPath = getExternalFilesDir(null).getParentFile().getAbsolutePath()+"/dex_files/";//程序的私有目录/storage/emulated/0/Android/data/包名
					//热更新文件夹路径
					File multiFolder = new File(dexPath);
					multiFolder.mkdirs();//创建文件夹，要不然复制不进去
					copyFilesFromAssets(MainActivity.this,"hotfix.dex",dexPath+"hotfix.dex");
					Toast.makeText(MainActivity.this, "热更新成功", Toast.LENGTH_SHORT).show();
					setRestartApp(MainActivity.this);
					/*
					 File dexdex = new File(dexPath+"hotfix.dex");
					 dexdex.delete();
					 //清楚热更新方法
					 */
				}
			});

	}
	//重启应用
	public static void setRestartApp(Activity activity) {
		String packageName = activity.getPackageName();

		Intent intent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * 从assets复制文件到指定路径
	 * @param context 上下文
	 * @param assetsFileName assets中的文件名
	 * @param targetPath 目标文件完整路径（包含文件名）
	 */
	private void copyFilesFromAssets(Context context, String assetsFileName, String targetPath) {
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			// 打开assets中的文件
			inputStream = context.getAssets().open(assetsFileName);
			// 创建目标文件
			File targetFile = new File(targetPath);
			// 若目标文件已存在，先删除（可选，根据需求决定是否覆盖）
			if (targetFile.exists()) {
				targetFile.delete();
			}
			// 确保目标文件的父目录存在
			File parentDir = targetFile.getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}
			// 写入文件
			outputStream = new FileOutputStream(targetFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, length);
			}
			// 刷新输出流，确保数据写入完成
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		
			Toast.makeText(context, "文件复制失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
		} finally {
			// 关闭流
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



}
