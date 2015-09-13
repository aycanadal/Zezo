package com.zezo.music.browser;

import java.io.File;
import java.io.FilenameFilter;

import com.zezo.music.MusicPlayerActivity;
import com.zezo.music.R;
import com.zezo.music.browser.FileListAdapter.FileClickListener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class Browser extends Fragment implements FileClickListener {

	private SharedPreferences sharedPreferences;
	private ListView fileListView;
	private FileListAdapter fileListAdapter;

	public void onAttach(Activity activity) {

		super.onAttach(activity);

		sharedPreferences = activity.getSharedPreferences(MusicPlayerActivity.PACKAGE_NAME, Context.MODE_PRIVATE);

		String musicFolder = sharedPreferences.getString(MusicPlayerActivity.KEY_DIRECTORY_SELECTED,
				Environment.getExternalStorageDirectory().toString());

		fileListAdapter = new FileListAdapter(getActivity(), getFolders(musicFolder), this);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View browserView = inflater.inflate(R.layout.browser, container, false);
		fileListView = (ListView) browserView.findViewById(R.id.filelist);
		fileListView.setAdapter(fileListAdapter);

		return browserView;
	}

	@Override
	public void fileClicked(File file) {
		// TODO Auto-generated method stub

	}
	
	private File[] getFolders(String directory) {

		File path = new File(directory);

		if (path.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					// add some filters here, for now return true to see all
					// files
					File file = new File(dir, filename);
					// return filename.contains(".txt") || file.isDirectory();
					return file.isDirectory();
				}
			};

			// if null return an empty array instead
			File[] list = path.listFiles(filter);
			return list == null ? new File[0] : list;
		} else {
			return new File[0];
		}
	}
	
}