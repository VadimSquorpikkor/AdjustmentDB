package com.squorpikkor.app.adjustmentdb.ui.main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.ExitAskDialog;

import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.BACK_PRESS_SEARCH;

public class SearchDeviceFragment extends Fragment {

    private MainViewModel mViewModel;

    public static SearchDeviceFragment newInstance() {
        return new SearchDeviceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_device, container, false);
        mViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

        final MutableLiveData<Boolean> doExit = mViewModel.getStartExit();
        doExit.observe(this, this::exitDialog);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.setBackPressCommand(BACK_PRESS_SEARCH);
    }

    void exitDialog(boolean state) {
        if (state) {
            ExitAskDialog dialog = new ExitAskDialog(getActivity());
            dialog.show();
        }
    }
}
