package com.zezo.zezomusicplayer;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.zezo.dragndroplistview.DragListener;
import com.zezo.dragndroplistview.DropListener;
import com.zezo.dragndroplistview.RemoveListener;

public class SongListView extends ListView {

	private DragListener mDragListener = new DragListener() {

		int backgroundColor = Color.BLACK;
		int defaultBackgroundColor;

		@Override
		public void onDrag(int x, int y, ListView listView) {
		}

		@Override
		public void onStartDrag(View itemView) {
			// itemView.setVisibility(View.INVISIBLE);
			// defaultBackgroundColor =
			// itemView.getDrawingCacheBackgroundColor();
			// itemView.setBackgroundColor(backgroundColor);
			// ImageView iv = (ImageView)
			// itemView.findViewById(R.id.DragHandleImageView);
			// if (iv != null)
			// iv.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onStopDrag(View itemView) {
			// itemView.setVisibility(View.VISIBLE);
			// itemView.setBackgroundColor(defaultBackgroundColor);
			// ImageView iv = (ImageView)
			// itemView.findViewById(R.id.DragHandleImageView);
			// if (iv != null)
			// iv.setVisibility(View.VISIBLE);
		}

	};

//	private DropListener mDropListener = new DropListener() {
//
//		@Override
//		public void onDrop(int firstItemIndex, int secondItemIndex) {
//
//			((SongAdapter) getAdapter())
//					.onDrop(firstItemIndex, secondItemIndex);
//
//			int checkedItemIndex = getCheckedItemPosition();
//
//			if (firstItemIndex < checkedItemIndex
//					&& secondItemIndex >= checkedItemIndex)
//				setItemChecked(checkedItemIndex - 1, true);
//
//			else if (firstItemIndex > checkedItemIndex
//					&& secondItemIndex <= checkedItemIndex)
//				setItemChecked(checkedItemIndex + 1, true);
//
//			else {
//
//				boolean isSecondChecked = isItemChecked(secondItemIndex);
//				setItemChecked(secondItemIndex, isItemChecked(firstItemIndex));
//				setItemChecked(firstItemIndex, isSecondChecked);
//
//			}
//
//			invalidateViews();
//
//		}
//	};

//	private RemoveListener mRemoveListener = new RemoveListener() {
//		@Override
//		public void onDrag(int which) {
//			((SongAdapter) getAdapter()).onDrag(which);
//			invalidateViews();
//		}
//	};

	public SongListView(Context context, AttributeSet attrs) {

		super(context, attrs);

		// setDropListener(mDropListener);
		// setRemoveListener(mRemoveListener);
		// setDragListener(mDragListener);

	}

}
