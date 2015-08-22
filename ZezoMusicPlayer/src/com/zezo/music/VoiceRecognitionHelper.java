package com.zezo.music;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.EditText;

public class VoiceRecognitionHelper {

	private static final int REQUEST_CODE = 1234;
	private EditText searchBox;

	public VoiceRecognitionHelper(EditText searchBox) {

		this.searchBox = searchBox;

	}

	public Intent getIntent() {

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening...");
		return intent;

	}

	public int getRequestCode() {

		return REQUEST_CODE;

	}

}
