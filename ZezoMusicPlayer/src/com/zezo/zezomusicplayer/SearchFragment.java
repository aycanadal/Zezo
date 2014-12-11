package com.zezo.zezomusicplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class SearchFragment extends Fragment {

	SearchListener mCallback;
	private EditText searchBox;

	// Container Activity must implement this interface
	public interface SearchListener {
		public void initSearch(View view);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (SearchListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement SearchListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.search, container, false);

		searchBox = (EditText) view.findViewById(R.id.searchBox);

		mCallback.initSearch(view);

		return view;
	}

	public void enableSearch(FragmentManager fragmentManager,
			InputMethodManager inputMethodManager) {

		fragmentManager.beginTransaction().show(this).commit();

		inputMethodManager.toggleSoftInputFromWindow(
				searchBox.getWindowToken(), 0, 0);
		
		searchBox.requestFocus();

		/*
		 * InputMethodManager imm = (InputMethodManager)
		 * getSystemService(Context.INPUT_METHOD_SERVICE);
		 * imm.showSoftInput(searchBox, InputMethodManager.SHOW_FORCED);
		 */

		// showKeyboard();

	}
	
	public void disableSearch(FragmentManager fragmentManager,
			InputMethodManager inputMethodManager) {

		 searchBox.setText("");

		 fragmentManager.beginTransaction()
		           .hide(this)
		           .commit();
		 
		 inputMethodManager.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);

	}
	
}