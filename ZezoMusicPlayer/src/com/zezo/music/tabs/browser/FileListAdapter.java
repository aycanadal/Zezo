package com.zezo.music.tabs.browser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import com.zezo.music.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter {

	private ArrayList<Folder> fileItems;
	private LayoutInflater inflater;
	private FileClickListener fileClickListener;

	public FileListAdapter(Context c, String folderPath, FileClickListener fileClickListener) {

		inflater = LayoutInflater.from(c);
		
		fileItems = new ArrayList<Folder>();	
		
		if (!folderPath.equals("/")) {
			
			Folder parentFolder = new Folder(new File(getParentFolder(folderPath))); 
			parentFolder.setDisplayName("..");
			fileItems.add(parentFolder);
			
		}
		
		for (File file : getFolders(folderPath)) {
		   this.fileItems.add(new Folder(file));
		}
		
		this.fileClickListener = fileClickListener;

	}

	public interface FileClickListener {

		void folderClicked(Folder folder);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Folder file = getItem(position);

		LinearLayout fileLayout = (LinearLayout) inflater.inflate(R.layout.file, parent, false);
		TextView fileName = (TextView) fileLayout.findViewById(R.id.filename);
		fileName.setText(file.getDisplayName());
		fileLayout.setTag(position);

		fileLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				int fileIndex = Integer.parseInt(view.getTag().toString());
				Folder folder = getItem(fileIndex);
				fileClickListener.folderClicked(folder);

			}

		});

		return fileLayout;

	}

	@Override
	public int getCount() {

		return fileItems.size();

	}

	@Override
	public Folder getItem(int position) {

		return fileItems.get(position);

	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	private ArrayList<File> getFolders(String directory) {

		File path = new File(directory);

		if (path.exists()) {

			FilenameFilter folderFilter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					// add some filters here, for now return true to see all
					// files
					File file = new File(dir, filename);
					// return filename.contains(".txt") || file.isDirectory();
					return file.isDirectory();
				}
			};

			File[] array = path.listFiles(folderFilter);
			ArrayList<File> folders = new ArrayList<File>(Arrays.asList(array));
			return folders == null ? new ArrayList<File>() : folders;

		} else

			return new ArrayList<File>();

	}

	public String getParentFolder(String folder) {
		
		String[] dirs = folder.split("/");
		StringBuilder stringBuilder = new StringBuilder("");

		for (int i = 0; i < dirs.length - 1; i++)
			stringBuilder.append(dirs[i]).append("/");

		return stringBuilder.toString();
	}

}