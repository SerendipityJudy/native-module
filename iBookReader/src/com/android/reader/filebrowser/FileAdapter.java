package com.android.reader.filebrowser;

import java.util.*;

import android.content.*;
import android.view.*;
import android.widget.*;

import com.android.reader.*;
import com.android.reader.model.*;

/**
 * @author zhuch
 * 
 */
public class FileAdapter extends BaseAdapter {

	private final LayoutInflater _inflater;
	private final List<FileInfo> _files;

	public FileAdapter(Context context, List<FileInfo> files) {
		_files = files;
		_inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return _files.size();
	}

	@Override
	public Object getItem(int position) {
		return _files.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;

		if (convertView == null) {
			convertView = _inflater.inflate(R.layout.file_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.file_name);
			holder.icon = (ImageView) convertView.findViewById(R.id.file_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		FileInfo f = _files.get(position);
		holder.name.setText(f.Name);
		holder.icon.setImageResource(f.getIconResourceId());

		return convertView;
	}

	private class ViewHolder {
		TextView name;
		ImageView icon;
	}
}
