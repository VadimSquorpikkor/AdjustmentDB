package com.squorpikkor.app.adjustmentdb.ui.main;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.FoundUnitAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.scanner.Scanner;
import java.util.ArrayList;
import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class MultiScanFragment extends Fragment {

    private Button nextButton;
    private MainViewModel mViewModel;
    private RecyclerView recyclerFoundUnits;
    private ArrayList<DUnit> foundUnitsList;
    private Scanner scannerMulti;
    private View view;

    public static MultiScanFragment newInstance() {
        return new MultiScanFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_multi_scan, container, false);
        mViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

        nextButton = view.findViewById(R.id.button_next);
        recyclerFoundUnits = view.findViewById(R.id.recyclerViewFound);

        final MutableLiveData<ArrayList<DUnit>> foundUnits = mViewModel.getFoundUnitsList();
        foundUnits.observe(getViewLifecycleOwner(), s -> {
            this.foundUnitsList = foundUnits.getValue();
            FoundUnitAdapter foundUnitAdapter = new FoundUnitAdapter(this.foundUnitsList);
            if (foundUnits.getValue() != null) Log.e(TAG, "♦ список найденных: " + foundUnits.getValue().size());
            recyclerFoundUnits.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerFoundUnits.setAdapter(foundUnitAdapter);
        });

        nextButton.setOnClickListener(v -> {
            //todo
        });

        scannerMulti = new Scanner(getActivity(), view, mViewModel, true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerMulti.setSurfaceVisible(true);
        scannerMulti.initialiseDetectorsAndSources();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (scannerMulti!=null)scannerMulti.cameraSourceRelease();
        if (scannerMulti!=null)scannerMulti.setSurfaceVisible(false);
    }

}