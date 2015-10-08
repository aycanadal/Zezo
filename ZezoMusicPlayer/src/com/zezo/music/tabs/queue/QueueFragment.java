package com.zezo.music.tabs.queue;

import java.util.ArrayList;

import com.zezo.music.MusicPlayerActivity;
import com.zezo.music.R;
import com.zezo.music.domain.Song;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class QueueFragment extends Fragment {
	
	private ListView queueListView;
	private QueueAdapter queueAdapter;
	private Menu optionsMenu;
	
	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		queueAdapter = new QueueAdapter(getActivity());

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View queueView = inflater.inflate(R.layout.queue, container, false);

		queueListView = (ListView) queueView.findViewById(R.id.song_list);
		queueListView.setAdapter(queueAdapter);
		registerForContextMenu(queueListView);

		return queueView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		optionsMenu = menu;
		optionsMenu.clear();
		inflater.inflate(R.menu.queue, optionsMenu);
		super.onCreateOptionsMenu(optionsMenu, inflater);

	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		final MusicPlayerActivity activity = (MusicPlayerActivity) getActivity();
		MenuInflater inflater = activity.getMenuInflater();
		inflater.inflate(R.menu.context, menu);

		menu.add(R.string.RemoveFromQueue).setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

				activity.removeFromQueue(info.id);

				return true;

			}
		});

	}

	public void addToQueue(Song song) {

		queueAdapter.addToQueue(song);
		
	}

	public void removeFromQueue(Song song) {

		queueAdapter.removeFromQueue(song);
		
	}

	public void setQueue(ArrayList<Song> queue) {
		
		queueAdapter.setQueue(queue);
		queueAdapter.notifyDataSetChanged();
		
	}
}