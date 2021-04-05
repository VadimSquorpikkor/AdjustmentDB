package com.squorpikkor.app.adjustmentdb.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.squorpikkor.app.adjustmentdb.R;

public class SearchDeviceFragment extends Fragment {
    public static SearchDeviceFragment newInstance() {
        return new SearchDeviceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_device, container, false);
        return view;
    }
}
