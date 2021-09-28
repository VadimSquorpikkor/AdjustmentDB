package com.squorpikkor.app.adjustmentdb.ui.main.fragment_cradle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.fragment.SearchDeviceFragment;

public class SearchParent extends Fragment {
    public static SearchParent newInstance() {
        return new SearchParent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parent_3, container, false);
        Fragment childFragment = SearchDeviceFragment.newInstance();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.child_fragment_container_3, childFragment).commit();
        return view;
    }
}
