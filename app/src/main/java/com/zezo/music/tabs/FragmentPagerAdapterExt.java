package com.zezo.music.tabs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 1 on 3.09.2016.
 */
abstract public class FragmentPagerAdapterExt extends FragmentPagerAdapter {

    private final SparseArray<Fragment> mFragments;
    private Fragment mPrimaryFragment;

    public FragmentPagerAdapterExt(FragmentManager fm) {
        super(fm);
        mFragments = new SparseArray<>(getCount());
    }

    @Override public Object instantiateItem(ViewGroup container, int position) {
        Object object = super.instantiateItem(container, position);
        mFragments.put(position, (Fragment) object);
        return object;
    }

    @Override public void destroyItem(ViewGroup container, int position, Object object) {
        mFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mPrimaryFragment = (Fragment) object;
    }

    /** Returns currently visible (primary) fragment */
    public Fragment getPrimaryFragment() {
        return mPrimaryFragment;
    }

    /** Returned list can contain null-values for not created fragments */
    public SparseArray<Fragment> getFragments() {
        return mFragments; // Should actually return an unmodifiable copy.
    }

}
