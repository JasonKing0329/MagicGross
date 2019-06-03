package com.king.app.gross.utils;

import android.util.Log;

import com.king.app.gross.base.MApplication;
import com.king.app.gross.conf.AppConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DBExportor {
	
	public static void execute() {

		String dbPath = MApplication.getInstance().getFilesDir().getParent() + "/databases";
	//	String dbPath = Environment.getExternalStorageDirectory() + "/tcslSystem";
		String targetPath = AppConfig.EXPORT_BASE;
		try {
			DBExportor.copyDirectiory(dbPath, targetPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void exportAsHistory() {

		String dbPath = MApplication.getInstance().getFilesDir().getParent() + "/databases/" + AppConfig.DB_NAME;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		StringBuffer target = new StringBuffer();
		target.append(AppConfig.HISTORY_BASE).append("/gross_");
		target.append(sdf.format(new Date()));
		target.append(".db");
		try {
			copyFile(new File(dbPath), new File(target.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void copyFile(File sourcefile, File targetFile)
			throws IOException {

		// 新建文件输入流并对它进行缓冲
		FileInputStream input = new FileInputStream(sourcefile);
		BufferedInputStream inbuff = new BufferedInputStream(input);

		// 新建文件输出流并对它进行缓冲
		FileOutputStream out = new FileOutputStream(targetFile);
		BufferedOutputStream outbuff = new BufferedOutputStream(out);

		// 缓冲数组
		byte[] b = new byte[1024 * 5];
		int len = 0;
		while ((len = inbuff.read(b)) != -1) {
			outbuff.write(b, 0, len);
		}

		// 刷新此缓冲的输出流
		outbuff.flush();

		// 关闭流
		inbuff.close();
		outbuff.close();
		out.close();
		input.close();

	}

	public static void copyDirectiory(String sourceDir, String targetDir)
			throws IOException {
		Log.e("DBExportor", "copy from [" + sourceDir + "] to [" + targetDir + "]");
		// 新建目标目录
		File target = new File(targetDir);
		if (!target.exists()) {
			target.mkdirs();
		}

		// 获取源文件夹当下的文件或目录
		File[] file = (new File(sourceDir)).listFiles();
		if (file == null) {
			return;
		}

		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(
						new File(targetDir).getAbsolutePath() + File.separator
								+ file[i].getName());

				copyFile(sourceFile, targetFile);

			}

			if (file[i].isDirectory()) {
				// 准备复制的源文件夹
				String dir1 = sourceDir + file[i].getName();
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();

				copyDirectiory(dir1, dir2);
			}
		}

	}
}
