package com.squorpikkor.app.adjustmentdb.ui.main.fragment;

import android.os.Bundle;
import android.util.Log;
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
import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.BACK_PRESS_MULTI;

public class MultiScanFragment extends Fragment {

    private Button nextButton;
    private MainViewModel mViewModel;
    private RecyclerView recyclerFoundUnits;
    private ArrayList<DUnit> foundUnitsList;
    private SurfaceView surfaceView;
    private TextView foundCount;

    public static MultiScanFragment newInstance() {
        return new MultiScanFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_scan, container, false);
        mViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

        nextButton = view.findViewById(R.id.button_next);
        recyclerFoundUnits = view.findViewById(R.id.recyclerViewFound);

        TextView txtBarcodeValue = view.findViewById(R.id.txtBarcodeValue);
        txtBarcodeValue.setVisibility(View.GONE);
        surfaceView = view.findViewById(R.id.surfaceViewM);
        surfaceView.setVisibility(View.INVISIBLE);
        nextButton = view.findViewById(R.id.button_next);
        nextButton.setVisibility(View.GONE);
        foundCount = view.findViewById(R.id.found_count);

        final MutableLiveData<ArrayList<DUnit>> foundUnits = mViewModel.getFoundUnitsList();
        foundUnits.observe(getViewLifecycleOwner(), s -> {
            foundUnitsList = foundUnits.getValue();
            foundCount.setText(String.valueOf(foundUnitsList.size()));
            if (foundUnitsList.size()!=0) nextButton.setVisibility(View.VISIBLE);
            FoundUnitAdapter foundUnitAdapter = new FoundUnitAdapter(foundUnitsList);
            if (foundUnits.getValue() != null) Log.e(TAG, "♦ список найденных: " + foundUnits.getValue().size());
            recyclerFoundUnits.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerFoundUnits.setAdapter(foundUnitAdapter);
        });

        nextButton.setOnClickListener(v -> {
            //todo
        });

        mViewModel.startMultiScanner(getActivity(), surfaceView);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        surfaceView.setVisibility(View.VISIBLE);
        mViewModel.getMultiScanner().initialiseDetectorsAndSources();
        mViewModel.setBackPressCommand(BACK_PRESS_MULTI);
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.getMultiScanner().cameraSourceRelease();
        surfaceView.setVisibility(View.GONE);
    }
}
