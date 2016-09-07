package com.zezo.music.tabs.playlist;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
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
import android.widget.Toast;

import com.zezo.music.MusicPlayerActivity;
import com.zezo.music.R;
import com.zezo.music.domain.Song;
import com.zezo.music.tabs.nowplaying.NowPlayingFragment.NowPlayingClickListener;
import com.zezo.music.tabs.playlist.PlaylistAdapter.SongClickListener;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment implements SongClickListener, NowPlayingClickListener {

    private ListView songListView;
    private PlaylistAdapter playlistAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){

        Log.d("Playlist Lifecycle", "onCreate");
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {

        Log.d("Playlist Lifecycle", "onAttach");
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d("Playlist Lifecycle", "onCreateView");
        Activity activity = getActivity();
        ArrayList<Song> playlist = ((MusicPlayerActivity) activity).getPlaylist();
        playlistAdapter = new PlaylistAdapter(activity, playlist, this);
        View playlistView = inflater.inflate(R.layout.playlist, container, false);
        songListView = (ListView) playlistView.findViewById(R.id.song_list);
        songListView.setAdapter(playlistAdapter);
        registerForContextMenu(songListView);
        setHasOptionsMenu(true);
        return playlistView;

    }

    @Override
    public void onActivityCreated(Bundle bundle) {

        Log.d("Playlist Lifecycle", "onActivityCreated");
        super.onActivityCreated(bundle);
        ArrayList<Song> playlist = ((MusicPlayerActivity) getActivity()).getPlaylist();
        loadPlaylist(playlist);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.playlist, menu);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem menuItem = menu.findItem(R.id.search);
        View actionView = menuItem.getActionView();
        final SearchView searchView = (SearchView) actionView;
        final SearchableInfo searchableInfo = searchManager.getSearchableInfo(getActivity().getComponentName());
        searchView.setSearchableInfo(searchableInfo);

        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String queryTextt) {

                playlistAdapter.getFilter().filter(queryTextt);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getActivity(), "Searching for: " + query + "...", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if (!queryTextFocused) {
                    menuItem.collapseActionView();
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                }
            }

        });

        super.onCreateOptionsMenu(menu, inflater);
        ((MusicPlayerActivity) getActivity()).updateShuffleIcon();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        final MusicPlayerActivity activity = (MusicPlayerActivity) getActivity();
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.context, menu);

        menu.add(R.string.AddToQueue).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

                activity.addToQueue(info.id);

                return true;

            }
        });

        menu.add(R.string.Delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

                activity.showDeleteDialog(info.id);

                return true;

            }
        });

    }

    public void setCurrentSong(Song song) {

        playlistAdapter.setItemChecked(song.getId());
        songListView.setItemChecked(playlistAdapter.getFilteredSongs().indexOf(song), true);

    }

    public void remove(Song song) {

        playlistAdapter.getSongs().remove(song);
        playlistAdapter.getFilteredSongs().remove(song);
        playlistAdapter.notifyDataSetChanged();

    }

    public void loadPlaylist(ArrayList<Song> songs) {

        playlistAdapter = new PlaylistAdapter(getActivity(), songs, this);
        songListView.setAdapter(playlistAdapter);

    }

    public void scrollToCurrent() {

        songListView.requestFocusFromTouch();
        songListView.setSelection(songListView.getCheckedItemPosition());

    }

    @Override
    public void onSongClicked(Song song) {

        Activity activity = getActivity();

        if (activity instanceof MusicPlayerActivity)
            ((MusicPlayerActivity) activity).play(song);

    }

    @Override
    public void onNowPlayingClicked() {

        scrollToCurrent();

    }

}