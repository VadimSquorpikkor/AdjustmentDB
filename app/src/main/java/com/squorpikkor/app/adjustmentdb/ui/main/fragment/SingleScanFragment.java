package com.squorpikkor.app.adjustmentdb.ui.main.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
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
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.RecognizeDialog;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.SelectStateDialogSingle;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.WrongQRDialog;
import java.util.ArrayList;
import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_SINGLE;
import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_STATES;
import static com.squorpikkor.app.adjustmentdb.Constant.LESS_THAN_ONE;

public class SingleScanFragment extends Fragment {

    private MainViewModel mViewModel;
    private TextView tType;
    private TextView tName;
    private TextView tInnerSerial;
    private TextView tSerial;
    private TextView tId;
    private TextView tEmployee;
    private TextView tDaysPassed;
    private TextView tTrackId;
    private ArrayList<DEvent> states;
    private FloatingActionButton addNewStateButton;
    private FloatingActionButton recognizeButton;
    private SurfaceView surfaceView;
    private ConstraintLayout infoLayout;
    private ImageView isCompleteImage;
    private StatesAdapter statesAdapter;

    public static SingleScanFragment newInstance() {
        return new SingleScanFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_scan, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        addNewStateButton = view.findViewById(R.id.addNewState);
        addNewStateButton.setVisibility(View.GONE);
        addNewStateButton.setOnClickListener(view1 -> openStatesDialog(true));

        recognizeButton = view.findViewById(R.id.recognize_button);
        recognizeButton.setVisibility(View.GONE);
        recognizeButton.setOnClickListener(view1 -> openRecognizeDialog());

        tType = view.findViewById(R.id.textViewType);
        tName = view.findViewById(R.id.textViewName);
        tInnerSerial = view.findViewById(R.id.textViewInnerSerialValue);
        tSerial = view.findViewById(R.id.textViewSerialValue);
        tId = view.findViewById(R.id.textViewIdValue);
        tEmployee = view.findViewById(R.id.textViewEmployeeValue);
        tDaysPassed = view.findViewById(R.id.textDaysPassedValue);

        RecyclerView recyclerUnitsStates = view.findViewById(R.id.recyclerView);
        statesAdapter = new StatesAdapter(mViewModel);
        recyclerUnitsStates.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerUnitsStates.setAdapter(statesAdapter);

        isCompleteImage = view.findViewById(R.id.is_complete);
        tTrackId = view.findViewById(R.id.textTrackIdValue);

        isCompleteImage.setVisibility(View.GONE);

        infoLayout = view.findViewById(R.id.db_info_layout);
        addNewStateButton = view.findViewById(R.id.addNewState);
        infoLayout.setVisibility(View.GONE);

        surfaceView = view.findViewById(R.id.surfaceViewS);
        surfaceView.setVisibility(View.INVISIBLE);

        mViewModel.getIsWrongQR().observe(getViewLifecycleOwner(), this::showWrongDialog);
        mViewModel.getSelectedUnit().observe(getViewLifecycleOwner(), this::insertDataToFields);
        mViewModel.getUnitStatesList().observe(getViewLifecycleOwner(), this::updateEvents);
        mViewModel.getRestartScanning().observe(getViewLifecycleOwner(), this::restartScanning);
        mViewModel.getShouldOpenDialog().observe(getViewLifecycleOwner(), this::openStatesDialog);
        mViewModel.getShowSurface().observe(getViewLifecycleOwner(), this::showSurface);

        mViewModel.startSingleScanner(getActivity(), surfaceView);

        return view;
    }

    private void showSurface(boolean show) {
        surfaceView.setVisibility(show?View.VISIBLE:View.GONE);
    }

    private void updateEvents(ArrayList<DEvent> events) {
        states = events;
        if (events==null)return;
        statesAdapter.setList(states);
    }

    private void showWrongDialog(boolean isWrong) {
        if (isWrong) {
            mViewModel.getShowSurface().setValue(false);
            WrongQRDialog dialog = new WrongQRDialog(requireActivity());
            dialog.show();
            mViewModel.getIsWrongQR().setValue(false);
        }
    }

    private void restartScanning(boolean state) {
        Log.e("TAG", "restartScanning: "+state);
        if (state) {
            if (states!=null) states.clear();
            //surfaceView.setVisibility(View.VISIBLE);
            mViewModel.getShowSurface().setValue(true);
            infoLayout.setVisibility(View.GONE);
            mViewModel.startSingleScanner(getActivity(), surfaceView);
            mViewModel.getSingleScanner().initialiseDetectorsAndSources();
            mViewModel.setBackPressCommand(BACK_PRESS_SINGLE);
            mViewModel.getSelectedUnit().setValue(null);
            mViewModel.getShouldOpenDialog().setValue(false);
        }
    }

    private void insertDataToFields(DUnit unit) {
        if (unit==null)return;
        addNewStateButton.setVisibility(View.VISIBLE);
        infoLayout.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.INVISIBLE);

        mViewModel.setBackPressCommand(BACK_PRESS_STATES);

        if (unit.isRepairUnit()) tType.setText("Ремонт");
        if (unit.isSerialUnit()) tType.setText("Серия");
        tId.setText(Utils.getRightValue(unit.getId()));
        tName.setText(Utils.getRightValue(mViewModel.getDeviceNameById(unit.getName())));
        tInnerSerial.setText(Utils.getRightValue(unit.getInnerSerial()));
        tSerial.setText(Utils.getRightValue(unit.getSerial()));
        if (unit.isRepairUnit()) recognizeButton.setVisibility(View.VISIBLE);
        tEmployee.setText(Utils.getRightValue(mViewModel.getEmployeeNameById(unit.getEmployee())));
        String passed = String.valueOf(unit.daysPassed());
        if (passed.equals("0")) passed = LESS_THAN_ONE;
        tDaysPassed.setText(passed);

        if (unit.isComplete()) isCompleteImage.setVisibility(View.VISIBLE);
        else isCompleteImage.setVisibility(View.GONE);

        tTrackId.setText(Utils.getRightValue(unit.getTrackId()));

        mViewModel.addSelectedUnitStatesListListener(unit.getId());
    }

    private void openStatesDialog(boolean b) {
        if (!b) return;
        new SelectStateDialogSingle().show(getParentFragmentManager(), null);
        mViewModel.getShouldOpenDialog().setValue(false);
    }

    private void openRecognizeDialog() {
        RecognizeDialog dialog = new RecognizeDialog();
        dialog.show(getParentFragmentManager(), null);
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
