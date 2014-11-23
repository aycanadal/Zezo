package com.zezo.zezomusicplayer;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {

	public interface MediaButtonReceiverListener {
		void onMediaButtonReceive(int keyCode);
	}

	private static ArrayList<MediaButtonReceiverListener> listeners = new ArrayList<MediaButtonReceiverListener>();

	public static void addBroadcastReceiveListener(
			MediaButtonReceiverListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public static void removeBroadcastReceiveListener(
			MediaButtonReceiverListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
			KeyEvent event = (KeyEvent) intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

			for (MediaButtonReceiverListener listener : listeners) {
				listener.onMediaButtonReceive(event.getKeyCode());
			}
		}
	}
}
