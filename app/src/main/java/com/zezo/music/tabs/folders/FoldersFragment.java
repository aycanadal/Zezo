package com.zezo.music.tabs.folders;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.zezo.music.MusicPlayerActivity;
import com.zezo.music.R;
import com.zezo.music.tabs.folders.FoldersAdapter.FolderClickListener;

public class FoldersFragment extends Fragment implements FolderClickListener {

    private TextView currentPathView;
    private ListView fileListView;
    private FoldersAdapter fileListAdapter;
    private SharedPreferences sharedPreferences;
    private String currentFolderPath;

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        sharedPreferences = context.getSharedPreferences(MusicPlayerActivity.PACKAGE_NAME, Context.MODE_PRIVATE);

        String preferredFolderPath = sharedPreferences.getString(MusicPlayerActivity.KEY_DIRECTORY_SELECTED,
                Environment.getExternalStorageDirectory().toString());

        fileListAdapter = new FoldersAdapter(getActivity(), preferredFolderPath, this);
        currentFolderPath = preferredFolderPath;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View foldersView = inflater.inflate(R.layout.folders, container, false);
        fileListView = (ListView) foldersView.findViewById(R.id.filelist);
        fileListView.setAdapter(fileListAdapter);
        currentPathView = (TextView) foldersView.findViewById(R.id.currentPath);
        currentPathView.setText(currentFolderPath);
        return foldersView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.folders, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        ((MusicPlayerActivity) getActivity()).loadFolder(getCurrentFolderPath());
        return true;

    }

    @Override
    public void folderClicked(Folder folder) {

        fileListAdapter = new FoldersAdapter(getActivity(), folder.getAbsolutePath(), this);
        fileListView.setAdapter(fileListAdapter);
        currentFolderPath = folder.getAbsolutePath();
        currentPathView.setText(currentFolderPath);

    }

    public String getCurrentFolderPath() {
        return currentFolderPath;
    }

}