package com.squorpikkor.app.adjustmentdb.ui.main.fragment_cradle;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.fragment.SearchDeviceFragment;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};
    private final Context mContext;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<Integer, Fragment> fragmentMap;
    FragmentManager fm;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fm = fm;
        mContext = context;
        fragmentMap = new HashMap<Integer, Fragment>() {
            {
                put(0, SingleScanParent.newInstance());
                put(1, SearchDeviceFragment.newInstance());
                put(2, MultiScanParent.newInstance());
            }
        };
    }



    @NotNull
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
//        if (position==0)fm.beginTransaction().remove((Fragment)MultiScanFragment.newInstance()).commit();
        return fragmentMap.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }
}