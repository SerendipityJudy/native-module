package com.android.reader.model;

import com.android.reader.*;

/**
 * 文件浏览 - 文件信息	
 * 
 * @Title: FileInfo.java
 * @Package com.android.reader.model
 */
public class FileInfo {
	public String Name;
	public String Path;
	public long Size;
	public boolean IsDirectory = false;
	public int FileCount = 0;
	public int FolderCount = 0;

	public int getIconResourceId() {
		if (IsDirectory) {
			return R.drawable.folder;
		}
		return R.drawable.doc;
	}
}