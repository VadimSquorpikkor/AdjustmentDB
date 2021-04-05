package com.squorpikkor.app.adjustmentdb.ui.main.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.SingleScanFragment;

public class SingleScanParent extends Fragment {

    public static SingleScanParent newInstance() {
        return new SingleScanParent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parent, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
//        Fragment childFragment = new SingleScanFragment();
        Fragment childFragment = SingleScanFragment.newInstance();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.child_fragment_container, childFragment).commit();
    }
}
