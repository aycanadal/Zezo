package com.zezo.music.tabs.queue;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.zezo.music.MusicPlayerActivity;
import com.zezo.music.R;
import com.zezo.music.shared.Song;

import java.util.ArrayList;

public class QueueFragment extends Fragment {

    private ListView queueListView;
    private QueueAdapter queueAdapter;
    private Menu optionsMenu;

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
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

                return false;

            }
        });

    }

    public void setQueue(ArrayList<Song> queue) {

        queueAdapter.setQueue(queue);
        queueAdapter.notifyDataSetChanged();

    }
}