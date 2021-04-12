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
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.StatesAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.SelectStateDialogNew;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class SingleScanFragment extends Fragment {

    private MainViewModel mViewModel;
    private TextView tType;
    private TextView tName;
    private TextView tInnerSerial;
    private TextView tSerial;
    private TextView tId;
    private TextView tLocation;
    private RecyclerView recyclerUnitsStates;
    private ArrayList<DEvent> states;
    private String location;//todo надо как observe Mutable, иначе может значение не успеть подгрузиться
    private FloatingActionButton addNewStateButton;
    private SurfaceView surfaceView;
    private ConstraintLayout infoLayout;
    public static final String EMPTY_VALUE = "- - -";

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
        tLocation = view.findViewById(R.id.textLocationValue);
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
            StatesAdapter statesAdapter = new StatesAdapter(this.states);
            recyclerUnitsStates.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerUnitsStates.setAdapter(statesAdapter);
        });

        mViewModel.startSingleScanner(getActivity(), surfaceView);

        location = mViewModel.getLocationName().getValue();

        return view;
    }

    private void insertDataToFields(DUnit unit) {
        addNewStateButton.setVisibility(View.VISIBLE);
        infoLayout.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.INVISIBLE);

        Log.e(TAG, "insertDataToFields: "+unit.getId());
        //todo это всё должно браться из viewModel
        tType.setText("- - -");
        if (unit.isRepairUnit()) tType.setText("Ремонт");
        if (unit.isSerialUnit()) tType.setText("Серия");
        tId.setText(insertRightValue(unit.getId()));
        tName.setText(insertRightValue(unit.getName()));
        tInnerSerial.setText(insertRightValue(unit.getInnerSerial()));
        tSerial.setText(insertRightValue(unit.getSerial()));
        tLocation.setText(location);

        mViewModel.addSelectedUnitStatesListListener(unit);
    }

    private String insertRightValue(String s) {
        if (s==null||s.equals("")||s.equals("null")) return EMPTY_VALUE;
        else return s;
    }

//    private void openUnitFragment(DUnit unit) {
//
//    }

    private void openStatesDialog() {
        //Загружается тот список, тип прибора который загружен — ремонт или серия
        ArrayList<String> rightList;
        if (mViewModel.getSelectedUnit().getValue().isRepairUnit()) rightList = mViewModel.getRepairStatesNames().getValue();
        else rightList = mViewModel.getSerialStatesNames().getValue();

        SelectStateDialogNew dialog = new SelectStateDialogNew(getActivity(), mViewModel, rightList);
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        surfaceView.setVisibility(View.VISIBLE);
        mViewModel.getSingleScanner().initialiseDetectorsAndSources();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.getSingleScanner().cameraSourceRelease();
        surfaceView.setVisibility(View.GONE);
    }
}
