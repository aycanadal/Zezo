package com.zezo.music;

import java.util.ArrayList;

import com.zezo.music.util.VoiceRecognitionHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class SearchFragment extends Fragment {

	public interface SearchListener {

		public void onSearchTextChanged(CharSequence cs);

	}
	private boolean isOn = false;

	private EditText searchBox;
	
	private SearchListener searchListener;

	private VoiceRecognitionHelper voiceRecognitionHelper;
	
	public void hide(FragmentManager fragmentManager,
			InputMethodManager inputMethodManager) {

		searchBox.setText("");

		inputMethodManager.hideSoftInputFromWindow(searchBox.getWindowToken(),
				0);

		fragmentManager.beginTransaction().hide(this).commit();
		
		isOn = false;

	}

	public boolean isOn(){
		return isOn;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == voiceRecognitionHelper.getRequestCode()
				&& resultCode == Activity.RESULT_OK) {

			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			searchBox.setText(matches.get(0));

		}

		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onAttach(Activity activity) {
		
		super.onAttach(activity);

		try {
			searchListener = (SearchListener) activity;
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

		voiceRecognitionHelper = new VoiceRecognitionHelper(searchBox);

		searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				searchListener.onSearchTextChanged(cs);
			}
		});

		final Button talkButton = (Button) view.findViewById(R.id.talkButton);
		talkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivityForResult(voiceRecognitionHelper.getIntent(),
						voiceRecognitionHelper.getRequestCode());
			}
		});

		return view;
	}

	public void show(FragmentManager fragmentManager,
			InputMethodManager inputMethodManager) {

		fragmentManager.beginTransaction().show(this).addToBackStack("searchFragment").commit();
		searchBox.requestFocus();

		inputMethodManager.toggleSoftInputFromWindow(
				searchBox.getWindowToken(), 0, 0);
		
		isOn = true;

	}

}