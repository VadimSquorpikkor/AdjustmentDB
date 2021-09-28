package com.squorpikkor.app.adjustmentdb.ui.main.fragment_cradle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.fragment.MultiScanFragment;
import com.squorpikkor.app.adjustmentdb.ui.main.fragment.SingleScanFragment;

public class MultiScanParent extends Fragment {

    public static MultiScanParent newInstance() {
        return new MultiScanParent();
    }

    private MainViewModel mViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mViewModel.getCanWork().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) startFragment();
            else stopFragment();
        });
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parent_2, container, false);
    }

    private void stopFragment() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (getChildFragmentManager().findFragmentById(R.id.child_fragment_container)!=null)
            transaction.remove(getChildFragmentManager().findFragmentById(R.id.child_fragment_container)).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mViewModel.getCanWork()!=null&&mViewModel.getCanWork().getValue() == true) {
            startFragment();
        }
    }

    private void startFragment() {
        Fragment childFragment = MultiScanFragment.newInstance();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.child_fragment_container, childFragment).commit();
    }

}
