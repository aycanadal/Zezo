package com.zezo.music.tabs.queue;
import com.zezo.music.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class Queue extends Fragment {
    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
 
            View android = inflater.inflate(R.layout.playqueue, container, false);
            
            return android;
}}