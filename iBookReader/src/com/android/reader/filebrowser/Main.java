package com.android.reader.filebrowser;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.android.reader.*;
import com.android.reader.model.*;
import com.android.reader.util.*;

/**
 * 文件浏览 Activity
 * 
 * @Title: Main.java
 * @Package com.android.reader.filebrowser
 */
public class Main extends ListActivity {
	
	private TextView _filePath;
	private final List<FileInfo> _files = new ArrayList<FileInfo>();
	private final String _rootPath = FileUtil.getSDPath();
	private String _currentPath = _rootPath;
	private final String TAG = "Main";
	private final int MENU_RENAME = Menu.FIRST;
	private final int MENU_COPY = Menu.FIRST + 3;
	private final int MENU_MOVE = Menu.FIRST + 4;
	private final int MENU_DELETE = Menu.FIRST + 5;
	private final int MENU_INFO = Menu.FIRST + 6;
	private final int MENU_IMPORT = Menu.FIRST + 7;
	private BaseAdapter adapter = null;
	private String isImport = "0";
	private final String targetPath = "/sdcard/lovereader/";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.file_main);

		_filePath = (TextView) findViewById(R.id.file_path);

		registerForContextMenu(getListView());

		adapter = new FileAdapter(this, _files);
		setListAdapter(adapter);

		viewFiles(_currentPath);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info = null;

		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}

		FileInfo f = _files.get(info.position);
		menu.setHeaderTitle(f.Name);
		menu.add(0, MENU_RENAME, 1, "重命名");
		menu.add(0, MENU_COPY, 2, "复制");
		menu.add(0, MENU_MOVE, 3, "移动");
		menu.add(0, MENU_DELETE, 4, "删除");
		menu.add(0, MENU_INFO, 5, "详细信息");
		menu.add(0, MENU_IMPORT, 6, "导入书架");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		FileInfo fileInfo = _files.get(info.position);
		File f = new File(fileInfo.Path);
		switch (item.getItemId()) {
		case MENU_RENAME:
			FileActivityHelper.renameFile(Main.this, f, renameFileHandler);
			return true;
		case MENU_COPY:
			pasteFile(f.getPath(), "COPY");
			return true;
		case MENU_MOVE:
			pasteFile(f.getPath(), "MOVE");
			return true;
		case MENU_DELETE:
			FileUtil.deleteFile(f);
			viewFiles(_currentPath);
			return true;
		case MENU_INFO:
			FileActivityHelper.viewFileInfo(Main.this, f);
			return true;
		case MENU_IMPORT:
			String src = fileInfo.Path;
			String tar = targetPath + f.getName();
			File tarPath = new File(targetPath);
			if(!tarPath.exists()){
				tarPath.mkdirs();
			}
			final File copyfile = new File(tar);
			if (copyfile.exists()) {
				Toast.makeText(getApplicationContext(), "文件已存在", Toast.LENGTH_SHORT).show();
			}else{
				try {
					FileUtil.copyFile(new File(src), new File(tar));
					DbHelper db = new DbHelper(this);
					db.insert(f.getName(), "0");
					db.close();
					isImport = "1";
					Toast.makeText ( Main.this , "导入成功" , Toast.LENGTH_SHORT).show();
					exit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		FileInfo f = _files.get(position);

		if (f.IsDirectory) {
			viewFiles(f.Path);
		} else {
			super.openContextMenu(v);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			File f = new File(_currentPath);
			String parentPath = f.getParent();
			if (parentPath != null) {
				viewFiles(parentPath);
			} else {
				exit();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (Activity.RESULT_OK == resultCode) {
			Bundle bundle = data.getExtras();
			if (bundle != null && bundle.containsKey("CURRENTPATH")) {
				viewFiles(bundle.getString("CURRENTPATH"));
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.file_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mainmenu_home:
			viewFiles(_rootPath);
			break;
		case R.id.mainmenu_refresh:
			viewFiles(_currentPath);
			break;
		case R.id.mainmenu_createdir:
			FileActivityHelper.createDir(Main.this, _currentPath, createDirHandler);
			break;
		case R.id.mainmenu_exit:
			exit();
			break;
		default:
			break;
		}
		return true;
	}

	private void viewFiles(String filePath) {
		
		ArrayList<FileInfo> tmp = FileActivityHelper.getFiles(Main.this, filePath);
		if (tmp != null) {
			
			_files.clear();
			_files.addAll(tmp);
			tmp.clear();

			_currentPath = filePath;
			_filePath.setText(filePath);

			adapter.notifyDataSetChanged();
		}
	}

	private void openFile(String path) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		File f = new File(path);
		String type = FileUtil.getMIMEType(f.getName());
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
	}

	private final Handler renameFileHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0)
				viewFiles(_currentPath);
		}
	};

	private final Handler createDirHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0)
				viewFiles(_currentPath);
		}
	};

	private void pasteFile(String path, String action) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("CURRENTPASTEFILEPATH", path);
		bundle.putString("ACTION", action);
		intent.putExtras(bundle);
		intent.setClass(Main.this, PasteFile.class);
		startActivityForResult(intent, 0);
	}

	private void exit() {
		
		Intent resultIntent = new Intent();
		resultIntent.putExtra("isImport", isImport); 
		setResult(222, resultIntent);

		Main.this.finish();
	}
}
