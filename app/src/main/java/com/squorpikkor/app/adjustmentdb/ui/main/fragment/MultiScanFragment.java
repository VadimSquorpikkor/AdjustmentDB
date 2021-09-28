package com.squorpikkor.app.adjustmentdb.ui.main.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.FoundUnitAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.SelectStateDialogMulti;
import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_MULTI;
import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_MULTI_STATES;

public class MultiScanFragment extends Fragment {

    private Button nextButton;
    private MainViewModel mViewModel;
    private ArrayList<DUnit> foundUnitsList;
    private SurfaceView surfaceView;
    private TextView foundCount;
    private FoundUnitAdapter foundUnitAdapter;

    public static MultiScanFragment newInstance() {
        return new MultiScanFragment();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_scan, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        nextButton = view.findViewById(R.id.button_next);

        TextView txtBarcodeValue = view.findViewById(R.id.txtBarcodeValue);
        txtBarcodeValue.setVisibility(View.GONE);
        surfaceView = view.findViewById(R.id.surfaceViewM);
        surfaceView.setVisibility(View.INVISIBLE);
        nextButton = view.findViewById(R.id.button_next);
        nextButton.setVisibility(View.GONE);
        foundCount = view.findViewById(R.id.found_count);

        RecyclerView recyclerFoundUnits = view.findViewById(R.id.recyclerViewFound);
        foundUnitAdapter = new FoundUnitAdapter(mViewModel);
        recyclerFoundUnits.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerFoundUnits.setAdapter(foundUnitAdapter);

        final MutableLiveData<ArrayList<DUnit>> foundUnits = mViewModel.getScannerFoundUnitsList();
        foundUnits.observe(getViewLifecycleOwner(), s -> {
            foundUnitsList = foundUnits.getValue();
            foundCount.setText(String.format(getString(R.string.found_count), foundUnitsList.size()));
            if (foundUnitsList.size() != 0) {
                nextButton.setVisibility(View.VISIBLE);
                mViewModel.setBackPressCommand(BACK_PRESS_MULTI_STATES);
            } else {
                mViewModel.setBackPressCommand(BACK_PRESS_MULTI);
            }
            foundUnitAdapter.setList(foundUnitsList);
        });

        nextButton.setOnClickListener(v -> {
            SelectStateDialogMulti dialog = new SelectStateDialogMulti();
            dialog.show(getParentFragmentManager(), null);
        });

        mViewModel.getRestartMultiScanning().observe(getViewLifecycleOwner(), this::restartMultiScanning);

        mViewModel.startMultiScanner(getActivity(), surfaceView);

        return view;
    }

    private void restartMultiScanning(Boolean state) {
        if (state) {
            nextButton.setVisibility(View.GONE);
            foundUnitsList = new ArrayList<>();
            surfaceView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        surfaceView.setVisibility(View.VISIBLE);

//        boolean state = mViewModel.getMultiScanner()!=null&&mViewModel.getCanWork()!=null&&mViewModel.getCanWork().getValue()!=null&&mViewModel.getCanWork().getValue();

        mViewModel.getMultiScanner().initialiseDetectorsAndSources();
        if (foundUnitsList!=null && foundUnitsList.size() != 0) {
            nextButton.setVisibility(View.VISIBLE);
            mViewModel.setBackPressCommand(BACK_PRESS_MULTI_STATES);
        } else {
            mViewModel.setBackPressCommand(BACK_PRESS_MULTI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.getMultiScanner().cameraSourceRelease();
        surfaceView.setVisibility(View.GONE);
    }
}
