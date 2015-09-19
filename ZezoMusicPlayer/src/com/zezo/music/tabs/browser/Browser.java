package com.zezo.music.tabs.browser;

import com.zezo.music.MusicPlayerActivity;
import com.zezo.music.R;
import com.zezo.music.tabs.browser.FileListAdapter.FolderClickListener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class Browser extends Fragment implements FolderClickListener {

	private SharedPreferences sharedPreferences;
	private ListView fileListView;
	private FileListAdapter fileListAdapter;
	private String currentFolderPath;

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

		sharedPreferences = activity.getSharedPreferences(MusicPlayerActivity.PACKAGE_NAME, Context.MODE_PRIVATE);

		String preferredFolderPath = sharedPreferences.getString(MusicPlayerActivity.KEY_DIRECTORY_SELECTED,
				Environment.getExternalStorageDirectory().toString());

		fileListAdapter = new FileListAdapter(getActivity(), preferredFolderPath, this);

		currentFolderPath = preferredFolderPath;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View browserView = inflater.inflate(R.layout.browser, container, false);
		fileListView = (ListView) browserView.findViewById(R.id.filelist);
		fileListView.setAdapter(fileListAdapter);

		return browserView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		menu.clear();
		inflater.inflate(R.menu.browser, menu);
		super.onCreateOptionsMenu(menu, inflater);

	}

	@Override
	public void folderClicked(Folder folder) {

		fileListAdapter = new FileListAdapter(getActivity(), folder.getAbsolutePath(), this);
		fileListView.setAdapter(fileListAdapter);
		currentFolderPath = folder.getAbsolutePath();

	}

	public String getCurrentFolderPath() {
		return currentFolderPath;
	}

}