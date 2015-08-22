package com.zezo.music;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;

public class FolderSelector {
	
	public static final String PACKAGE_NAME = "com.zezo.player"; 
	public static final String KEY_DIRECTORY_SELECTED = 
	    PACKAGE_NAME + ".DIRECTORY_SELECTED";

	private File[] fileList;
	private String[] filenameList;
	private MusicFolderUpdatedListener musicFolderUpdatedListener;
	
	public interface MusicFolderUpdatedListener{
	       void onMusicFolderUpdated(String musicFolderPath);
	    }
	
	public void setDialogResult(MusicFolderUpdatedListener musicFolderUpdatedListener){
        this.musicFolderUpdatedListener = musicFolderUpdatedListener;
    }

	private File[] loadFileList(String directory) {

		File path = new File(directory);

		if (path.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					// add some filters here, for now return true to see all
					// files
					File file = new File(dir, filename);
					// return filename.contains(".txt") || file.isDirectory();
					return file.isDirectory();
				}
			};

			// if null return an empty array instead
			File[] list = path.listFiles(filter);
			return list == null ? new File[0] : list;
		} else {
			return new File[0];
		}
	}

	public void showFileListDialog(final String directory, final Context context) {

		Dialog dialog = null;

		File[] tempFileList = loadFileList(directory);

		// if directory is root, no need to up one directory
		if (directory.equals("/")) {
			fileList = new File[tempFileList.length];
			filenameList = new String[tempFileList.length];

			// iterate over tempFileList
			for (int i = 0; i < tempFileList.length; i++) {
				fileList[i] = tempFileList[i];
				filenameList[i] = tempFileList[i].getName();
			}
		} else {
			fileList = new File[tempFileList.length + 1];
			filenameList = new String[tempFileList.length + 1];

			// add an "up" option as first item
			fileList[0] = new File(upOneDirectory(directory));
			filenameList[0] = "..";

			// iterate over tempFileList
			for (int i = 0; i < tempFileList.length; i++) {
				fileList[i + 1] = tempFileList[i];
				filenameList[i + 1] = tempFileList[i].getName();
			}
		}

		Builder builder = new Builder(context);
		
		builder.setTitle("Choose your file: " + directory);

		builder.setItems(filenameList, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				File chosenFile = fileList[which];

				if (chosenFile.isDirectory())
					showFileListDialog(chosenFile.getAbsolutePath(), context);
			}
		});

		builder.setNegativeButton("Cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		builder.setPositiveButton("Set Music Folder", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if( musicFolderUpdatedListener != null ){
					musicFolderUpdatedListener.onMusicFolderUpdated(directory);
	            }
			}
		});

		dialog = builder.create();
		dialog.show();
	}

	public String upOneDirectory(String directory) {
		String[] dirs = directory.split("/");
		StringBuilder stringBuilder = new StringBuilder("");

		for (int i = 0; i < dirs.length - 1; i++)
			stringBuilder.append(dirs[i]).append("/");

		return stringBuilder.toString();
	}

}
