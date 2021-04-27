package com.squorpikkor.app.adjustmentdb.ui.main.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squorpikkor.app.adjustmentdb.DEvent;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.Utils;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.StatesAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.SelectStateDialogSingle;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.BACK_PRESS_SINGLE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.BACK_PRESS_STATES;

public class SingleScanFragment extends Fragment {

    private MainViewModel mViewModel;
    private TextView tType;
    private TextView tName;
    private TextView tInnerSerial;
    private TextView tSerial;
    private TextView tId;
//    private TextView tLocation;
    private RecyclerView recyclerUnitsStates;
    private ArrayList<DEvent> states;
//    private String location;//todo надо как observe Mutable, иначе может значение не успеть подгрузиться
    private FloatingActionButton addNewStateButton;
    private SurfaceView surfaceView;
    private ConstraintLayout infoLayout;

    public static SingleScanFragment newInstance() {
        return new SingleScanFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_scan, container, false);
        mViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

        addNewStateButton = view.findViewById(R.id.addNewState);
        addNewStateButton.setVisibility(View.GONE);
        addNewStateButton.setOnClickListener(view1 -> openStatesDialog());

        tType = view.findViewById(R.id.textViewType);
        tName = view.findViewById(R.id.textViewName);
        tInnerSerial = view.findViewById(R.id.textViewInnerSerialValue);
        tSerial = view.findViewById(R.id.textViewSerialValue);
        tId = view.findViewById(R.id.textViewIdValue);
//        tLocation = view.findViewById(R.id.textLocationValue);
        recyclerUnitsStates = view.findViewById(R.id.recyclerView);

        infoLayout = view.findViewById(R.id.db_info_layout);
        addNewStateButton = view.findViewById(R.id.addNewState);
        infoLayout.setVisibility(View.GONE);

        TextView txtBarcodeValue = view.findViewById(R.id.txtBarcodeValue);
        txtBarcodeValue.setVisibility(View.GONE);

        surfaceView = view.findViewById(R.id.surfaceViewS);
        surfaceView.setVisibility(View.INVISIBLE);

        final MutableLiveData<DUnit> selectedUnits = mViewModel.getSelectedUnit();
        selectedUnits.observe(getViewLifecycleOwner(), s -> {
            DUnit unit = selectedUnits.getValue();
//            if (unit!=null) openUnitFragment(unit);
            if (unit!=null) insertDataToFields(unit);
        });

        //Отслеживает список событий (время + текст) текущего устройства
        final MutableLiveData<ArrayList<DEvent>> unitEvents = mViewModel.getUnitStatesList();
        unitEvents.observe(getViewLifecycleOwner(), s -> {
            Log.e(TAG, "onCreateView: список статусов mViewModel.getUnitStatesList");
            this.states = unitEvents.getValue();
            if (unitEvents.getValue() != null) Log.e(TAG, "onCreateView список статусов: " + unitEvents.getValue().size());
            StatesAdapter statesAdapter = new StatesAdapter(this.states, mViewModel);
            recyclerUnitsStates.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerUnitsStates.setAdapter(statesAdapter);
        });

        final MutableLiveData<Boolean> restartScanning = mViewModel.getRestartScanning();
        restartScanning.observe(this, this::restartScanning);

        mViewModel.startSingleScanner(getActivity(), surfaceView);

//        location = mViewModel.getLocationName().getValue();

        return view;
    }

    private void restartScanning(boolean state) {
        if (state) {
            if (states!=null) states.clear();
            surfaceView.setVisibility(View.VISIBLE);
            infoLayout.setVisibility(View.GONE);
            mViewModel.startSingleScanner(getActivity(), surfaceView);
            mViewModel.getSingleScanner().initialiseDetectorsAndSources();
            mViewModel.setBackPressCommand(BACK_PRESS_SINGLE);
            mViewModel.getSelectedUnit().setValue(null);
        }
    }

    private void insertDataToFields(DUnit unit) {
        addNewStateButton.setVisibility(View.VISIBLE);
        infoLayout.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.INVISIBLE);

        mViewModel.setBackPressCommand(BACK_PRESS_STATES);

        Log.e(TAG, "insertDataToFields: "+unit.getId());
        //todo это всё должно браться из viewModel
        tType.setText("- - -");
        if (unit.isRepairUnit()) tType.setText("Ремонт");
        if (unit.isSerialUnit()) tType.setText("Серия");
        tId.setText(Utils.getRightValue(unit.getId()));
        tName.setText(Utils.getRightValue(unit.getName()));
        tInnerSerial.setText(Utils.getRightValue(unit.getInnerSerial()));
        tSerial.setText(Utils.getRightValue(unit.getSerial()));
        //tLocation.setText(location);

        mViewModel.addSelectedUnitStatesListListener(unit.getId());
    }

//    private void openUnitFragment(DUnit unit) {
//
//    }

    private void openStatesDialog() {
        SelectStateDialogSingle dialog = new SelectStateDialogSingle(getActivity());
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        surfaceView.setVisibility(View.VISIBLE);
        mViewModel.getSingleScanner().initialiseDetectorsAndSources();
        if (infoLayout.getVisibility()==View.GONE) mViewModel.setBackPressCommand(BACK_PRESS_SINGLE);
        else mViewModel.setBackPressCommand(BACK_PRESS_STATES);
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.getSingleScanner().cameraSourceRelease();
        surfaceView.setVisibility(View.GONE);
    }
}
