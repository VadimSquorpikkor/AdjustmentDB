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
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squorpikkor.app.adjustmentdb.DState;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.StatesAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.SelectStateDialogNew;
import com.squorpikkor.app.adjustmentdb.ui.main.scanner.Scanner;
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

    private ArrayList<DUnit> units;
    private ArrayList<DState> states;

    private Scanner scannerSingle;
    private String location;//todo надо как observe Mutable, иначе может значение не успеть подгрузиться

    public static final String EMPTY_VALUE = "- - -";

    public static SingleScanFragment newInstance() {
        return new SingleScanFragment();
    }

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_single_scan, container, false);
        mViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

        units = new ArrayList<>();
        states = new ArrayList<>();

        tType = view.findViewById(R.id.textViewType);
        tName = view.findViewById(R.id.textViewName);
        tInnerSerial = view.findViewById(R.id.textViewInnerSerialValue);
        tSerial = view.findViewById(R.id.textViewSerialValue);
        tId = view.findViewById(R.id.textViewIdValue);
        tLocation = view.findViewById(R.id.textLocationValue);
        recyclerUnitsStates = view.findViewById(R.id.recyclerView);

        FloatingActionButton addNewStateButton = view.findViewById(R.id.addNewState);
        addNewStateButton.setVisibility(View.GONE);

        addNewStateButton.setOnClickListener(view1 -> openStatesDialog());

        final MutableLiveData<ArrayList<DUnit>> selectedUnits = mViewModel.getSelectedUnits();
        selectedUnits.observe(getViewLifecycleOwner(), s -> {
            units = selectedUnits.getValue();
            if (units==null)return;
            if (units.size()>1) Log.e(TAG, "* Есть несколько устройств с таким серийником!!!");
            if (units.size() != 0) insertDataToFields(units.get(0));
            else Log.e(TAG, "* Можно Отправить данные в БД");
        });

        //Отслеживает список статусов (время + текст) текущего устройства/
        final MutableLiveData<ArrayList<DState>> statesForUnit = mViewModel.getUnitStatesList();
        statesForUnit.observe(getViewLifecycleOwner(), s -> {
            Log.e(TAG, "onCreateView: список статусов mViewModel.getUnitStatesList");
            this.states = statesForUnit.getValue();
            if (statesForUnit.getValue() != null) Log.e(TAG, "onCreateView список статусов: " + statesForUnit.getValue().size());
            StatesAdapter statesAdapter = new StatesAdapter(this.states);
            recyclerUnitsStates.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerUnitsStates.setAdapter(statesAdapter);
        });

        scannerSingle = new Scanner(getActivity(), view, mViewModel, false);

        location = mViewModel.getLocationName().getValue();

        return view;
    }

    private void insertDataToFields(DUnit unit) {
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

    private void openStatesDialog() {
        //Загружается тот список, тип прибора который загружен — ремонт или серия
        ArrayList<String> rightList;
        if (mViewModel.getSelectedUnits().getValue().get(0).isRepairUnit()) rightList = mViewModel.getRepairStatesList().getValue();
        else rightList = mViewModel.getSerialStatesList().getValue();

        SelectStateDialogNew dialog = new SelectStateDialogNew(getActivity(), mViewModel, rightList);
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerSingle.setSurfaceVisible(true);
        scannerSingle.initialiseDetectorsAndSources();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerSingle.cameraSourceRelease();
        scannerSingle.setSurfaceVisible(false);
    }

}