package com.android.reader.filebrowser;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.android.reader.*;
import com.android.reader.model.*;
import com.android.reader.util.*;

public class FileActivityHelper {

	public static ArrayList<FileInfo> getFiles(Activity activity, String path) {
		File f = new File(path);
		File[] files = f.listFiles();
		if (files == null) {
			Toast.makeText(activity,
					String.format(activity.getString(R.string.file_cannotopen), path),
					Toast.LENGTH_SHORT).show();
			return null;
		}

		ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
		
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if(file.isDirectory()){
				FileInfo fileInfo = new FileInfo();
				fileInfo.Name = file.getName();
				fileInfo.IsDirectory = file.isDirectory();
				fileInfo.Path = file.getPath();
				fileInfo.Size = file.length();
				fileList.add(fileInfo);
			}else if(file.getPath().toLowerCase().endsWith(".txt")){
				FileInfo fileInfo = new FileInfo();
				fileInfo.Name = file.getName();
				fileInfo.IsDirectory = file.isDirectory();
				fileInfo.Path = file.getPath();
				fileInfo.Size = file.length();
				fileList.add(fileInfo);
			}
		}

		Collections.sort(fileList, new FileComparator());

		return fileList;
	}

	public static void createDir(final Activity activity, final String path, final Handler hander) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		View layout = LayoutInflater.from(activity).inflate(R.layout.file_create, null);
		final EditText text = (EditText) layout.findViewById(R.id.file_name);
		builder.setView(layout);
		builder.setPositiveButton("ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialoginterface, int i) {
				String newName = text.getText().toString().trim();
				if (newName.length() == 0) {
					Toast.makeText(activity, "文件名不能为空", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				String fullFileName = FileUtil.combinPath(path, newName);
				File newFile = new File(fullFileName);
				if (newFile.exists()) {
					Toast.makeText(activity, "文件已存在", Toast.LENGTH_SHORT).show();
				} else {
					try {
						if (newFile.mkdir()) {
							hander.sendEmptyMessage(0);
						} else {
							Toast.makeText(activity, R.string.file_create_fail, Toast.LENGTH_SHORT)
									.show();
						}
					} catch (Exception ex) {
						Toast.makeText(activity, ex.getLocalizedMessage(), Toast.LENGTH_SHORT)
								.show();
					}
				}
			}
		}).setNegativeButton("取消", null);
		AlertDialog alertDialog = builder.create();
		alertDialog.setTitle(R.string.mainmenu_createdir);
		alertDialog.show();
	}

	public static void renameFile(final Activity activity, final File f, final Handler hander) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		View layout = LayoutInflater.from(activity).inflate(R.layout.file_rename, null);
		final EditText text = (EditText) layout.findViewById(R.id.file_name);
		text.setText(f.getName());
		builder.setView(layout);
		builder.setPositiveButton("ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialoginterface, int i) {
				String path = f.getParentFile().getPath();
				String newName = text.getText().toString().trim();
				if (newName.equalsIgnoreCase(f.getName())) {
					return;
				}
				if (newName.length() == 0) {
					Toast.makeText(activity,  "文件名不能为空", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				String fullFileName = FileUtil.combinPath(path, newName);

				File newFile = new File(fullFileName);
				if (newFile.exists()) {
					Toast.makeText(activity, "文件已存在", Toast.LENGTH_SHORT).show();
				} else {
					try {
						if (f.renameTo(newFile)) {
							hander.sendEmptyMessage(0);
						} else {
							Toast.makeText(activity, R.string.file_rename_fail, Toast.LENGTH_SHORT)
									.show();
						}
					} catch (Exception ex) {
						Toast.makeText(activity, ex.getLocalizedMessage(), Toast.LENGTH_SHORT)
								.show();
					}
				}
			}
		}).setNegativeButton("取消", null);
		AlertDialog alertDialog = builder.create();
		alertDialog.setTitle("重命名");
		alertDialog.show();
	}

	@SuppressWarnings("deprecation")
	public static void viewFileInfo(Activity activity, File f) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		View layout = LayoutInflater.from(activity).inflate(R.layout.file_info, null);
		FileInfo info = FileUtil.getFileInfo(f);

		((TextView) layout.findViewById(R.id.file_name)).setText(f.getName());
		((TextView) layout.findViewById(R.id.file_lastmodified)).setText(new Date(f.lastModified())
				.toLocaleString());
		((TextView) layout.findViewById(R.id.file_size))
				.setText(FileUtil.formetFileSize(info.Size));
		if (f.isDirectory()) {
			((TextView) layout.findViewById(R.id.file_contents)).setText("Folder "
					+ info.FolderCount + ", File " + info.FileCount);
		}

		builder.setView(layout);
		builder.setPositiveButton("ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialoginterface, int i) {
				dialoginterface.cancel();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.setTitle("详细信息");
		alertDialog.show();
	}
}