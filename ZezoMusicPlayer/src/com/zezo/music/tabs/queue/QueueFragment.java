package com.zezo.music.tabs.queue;

import com.zezo.music.R;
import com.zezo.music.domain.Song;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class QueueFragment extends Fragment {
	
	private ListView queueListView;
	private QueueAdapter queueAdapter;
	
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

		return queueView;
	}

	public void addToQueue(Song song) {

		queueAdapter.addToQueue(song);
		
	}
}