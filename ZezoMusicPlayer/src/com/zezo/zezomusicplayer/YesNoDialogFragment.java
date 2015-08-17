package com.zezo.zezomusicplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class YesNoDialogFragment extends DialogFragment {

	public interface OnDeleteConfirmedListener {
		public void onDeleteConfirmed(long songId);
	}

	private static final int REQUEST_CODE = 1235;

	public static int getRequestCode() {
		return REQUEST_CODE;
	}

	private OnDeleteConfirmedListener onDeleteConfirmedListener;

	public YesNoDialogFragment() {

	}

	@Override
	public void onAttach(Activity activity) {
		
		super.onAttach(activity);
		
		try {
			
			onDeleteConfirmedListener = (OnDeleteConfirmedListener) activity;
			
		} catch (ClassCastException e) {
			
			throw new ClassCastException(activity.toString()
					+ " must implement OnDeleteConfirmedListener");
			
		}
		
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		String title = args.getString("title", "");
		String message = args.getString("message", "");
		final long songId = args.getLong("songId");

		return new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								onDeleteConfirmedListener
										.onDeleteConfirmed(songId);

							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).create();
	}
}