package com.zezo.music.browser;

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

		String folderPath = sharedPreferences.getString(MusicPlayerActivity.KEY_DIRECTORY_SELECTED,
				Environment.getExternalStorageDirectory().toString());

		fileListAdapter = new FileListAdapter(getActivity(), folderPath, this);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View browserView = inflater.inflate(R.layout.browser, container, false);
		fileListView = (ListView) browserView.findViewById(R.id.filelist);
		fileListView.setAdapter(fileListAdapter);

		return browserView;
	}

	@Override
	public void folderClicked(Folder folder) {

		fileListAdapter = new FileListAdapter(getActivity(), folder.getAbsolutePath(), this);
		fileListView.setAdapter(fileListAdapter);		

	}
	
}