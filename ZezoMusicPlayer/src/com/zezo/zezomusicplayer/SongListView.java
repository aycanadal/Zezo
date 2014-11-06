package com.zezo.zezomusicplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.zezo.dragndroplistview.DragListener;
import com.zezo.dragndroplistview.DragNDropListView;
import com.zezo.dragndroplistview.DropListener;
import com.zezo.dragndroplistview.RemoveListener;

public class SongListView extends DragNDropListView {

	public SongListView(Context context, AttributeSet attrs) {

		super(context, attrs);

		setDropListener(mDropListener);
		setRemoveListener(mRemoveListener);
		setDragListener(mDragListener);

	}

	private DropListener mDropListener = new DropListener() {
		public void onDrop(int from, int to) {

			((SongAdapter) getAdapter()).onDrop(from, to);
			invalidateViews();
		}
	};

	private RemoveListener mRemoveListener = new RemoveListener() {
		public void onRemove(int which) {
			((SongAdapter) getAdapter()).onRemove(which);
			invalidateViews();
		}
	};

	private DragListener mDragListener = new DragListener() {

		int backgroundColor = 0xe0103010;
		int defaultBackgroundColor;

		public void onDrag(int x, int y, ListView listView) {
			// TODO Auto-generated method stub
		}

		public void onStartDrag(View itemView) {
			itemView.setVisibility(View.INVISIBLE);
			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(backgroundColor);
			ImageView iv = (ImageView) itemView.findViewById(R.id.ImageView01);
			if (iv != null)
				iv.setVisibility(View.INVISIBLE);
		}

		public void onStopDrag(View itemView) {
			itemView.setVisibility(View.VISIBLE);
			itemView.setBackgroundColor(defaultBackgroundColor);
			ImageView iv = (ImageView) itemView.findViewById(R.id.ImageView01);
			if (iv != null)
				iv.setVisibility(View.VISIBLE);
		}

	};

}
