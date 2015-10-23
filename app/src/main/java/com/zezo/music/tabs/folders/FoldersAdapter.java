package com.zezo.music.tabs.folders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zezo.music.R;
import com.zezo.music.domain.Folder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class FoldersAdapter extends BaseAdapter {

    private ArrayList<Folder> folders;
    private LayoutInflater inflater;
    private FolderClickListener fileClickListener;

    public FoldersAdapter(Context c, String folderPath, FolderClickListener fileClickListener) {

        inflater = LayoutInflater.from(c);

        folders = new ArrayList<Folder>();

        if (!folderPath.equals("/")) {

            Folder parentFolder = new Folder(new File(getParentFolder(folderPath)));
            parentFolder.setDisplayName("..");
            folders.add(parentFolder);

        }

        for (File file : getFolders(folderPath)) {
            this.folders.add(new Folder(file));
        }

        this.fileClickListener = fileClickListener;

    }

    public interface FolderClickListener {

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

        return folders.size();

    }

    @Override
    public Folder getItem(int position) {

        return folders.get(position);

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
                @Override
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