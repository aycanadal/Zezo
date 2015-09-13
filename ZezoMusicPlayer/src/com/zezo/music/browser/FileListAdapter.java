package com.zezo.music.browser;

import java.io.File;

import com.zezo.music.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter {
	
	private File[] files;
	private LayoutInflater inflater;
	
	public FileListAdapter(Context c, File[] files, FileClickListener fileClickListener) {

		inflater = LayoutInflater.from(c);
		this.files = files;
		
	}
	
	public interface FileClickListener {

		void fileClicked(File file);

	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		File file = files[position];
		
		LinearLayout fileLayout = (LinearLayout) inflater.inflate(R.layout.file, parent, false);
		TextView fileName = (TextView) fileLayout.findViewById(R.id.filename);
		fileName.setText(file.getName());
		
		return fileLayout;
		
	}

	@Override
	public int getCount() {
		
		return files.length;
				
	}

	@Override
	public Object getItem(int position) {
		
		return files[position];
				
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

}
