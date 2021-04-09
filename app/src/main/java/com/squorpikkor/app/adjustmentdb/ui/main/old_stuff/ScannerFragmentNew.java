package com.squorpikkor.app.adjustmentdb.ui.main.old_stuff;

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
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squorpikkor.app.adjustmentdb.DEvent;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.FoundUnitAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.StatesAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.SelectStateDialogNew;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.SelectStateDialogNewDesign;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class ScannerFragmentNew extends Fragment {

    private MainViewModel mViewModel;

    private Button addToBDButton;
    private Button nextButton;

    private TextView tType;
    private TextView tName;
    private TextView tInnerSerial;
    private TextView tSerial;
    private TextView tId;
    private TextView tLocation;
    private RecyclerView recyclerUnitsStates;
    private RecyclerView recyclerFoundUnits;

    private DUnit unit;
    private ArrayList<DUnit> foundUnitsList;
    private ArrayList<DEvent> states;

    private ScannerOld scanner;
    private String location;//todo надо как observe Mutable, иначе может значение не успеть подгрузиться

    public static final String EMPTY_VALUE = "- - -";

    public static ScannerFragmentNew newInstance() {
        return new ScannerFragmentNew();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner_new, container, false);
        mViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

//        units = new ArrayList<>();
        states = new ArrayList<>();

        addToBDButton = view.findViewById(R.id.buttonAddToBD);
        tType = view.findViewById(R.id.textViewType);
        tName = view.findViewById(R.id.textViewName);
        tInnerSerial = view.findViewById(R.id.textViewInnerSerialValue);
        tSerial = view.findViewById(R.id.textViewSerialValue);
        tId = view.findViewById(R.id.textViewIdValue);
        tLocation = view.findViewById(R.id.textLocationValue);
        recyclerUnitsStates = view.findViewById(R.id.recyclerView);
        recyclerFoundUnits = view.findViewById(R.id.recyclerViewFound);
        nextButton = view.findViewById(R.id.button_next);

        FloatingActionButton addNewStateButton = view.findViewById(R.id.addNewState);
        addToBDButton.setVisibility(View.GONE);
        addNewStateButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);

        addNewStateButton.setOnClickListener(view1 -> openStatesDialog());

        view.findViewById(R.id.button).setOnClickListener(view1 -> {
            SelectStateDialogNewDesign dialog = new SelectStateDialogNewDesign();
            dialog.setCancelable(false);
//            dialog.setTargetFragment(getTargetFragment(), REQUEST_CODE_KEYWORD_DIALOG);
            dialog.show(requireFragmentManager(), null);
        });

        final MutableLiveData<DUnit> selectedUnit = mViewModel.getSelectedUnit();
        selectedUnit.observe(getViewLifecycleOwner(), s -> {
            unit = selectedUnit.getValue();
            if (unit!=null)insertDataToFields(unit);
//                unit = selectedUnit.getValue();
//            if (units==null)return;
//            if (units.size()>1) Log.e(TAG, "* Есть несколько устройств с таким серийником!!!");
//            if (units.size() != 0) insertDataToFields(units.get(0));
            ////////////////if (mViewModel.getIsRepair().getValue()) addToBDButton.setText("Отправить данные в БД (ремонт)");
//            else addToBDButton.setText("Отправить данные в БД");
        });

        //Отслеживает список статусов (время + текст) текущего устройства/
        final MutableLiveData<ArrayList<DEvent>> statesForUnit = mViewModel.getUnitStatesList();
        statesForUnit.observe(getViewLifecycleOwner(), s -> {
            Log.e(TAG, "onCreateView: список статусов mViewModel.getUnitStatesList");
            this.states = statesForUnit.getValue();
            if (statesForUnit.getValue() != null) Log.e(TAG, "onCreateView список статусов: " + statesForUnit.getValue().size());
            StatesAdapter statesAdapter = new StatesAdapter(this.states);
            recyclerUnitsStates.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerUnitsStates.setAdapter(statesAdapter);
        });

        final MutableLiveData<ArrayList<DUnit>> foundUnits = mViewModel.getFoundUnitsList();
        foundUnits.observe(getViewLifecycleOwner(), s -> {
            this.foundUnitsList = foundUnits.getValue();
            FoundUnitAdapter foundUnitAdapter = new FoundUnitAdapter(this.foundUnitsList);
            if (foundUnits.getValue() != null) Log.e(TAG, "♦ список найденных: " + foundUnits.getValue().size());
            recyclerFoundUnits.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerFoundUnits.setAdapter(foundUnitAdapter);
        });

        //todo сканнер нужно будет размещать в viewModel
        scanner = new ScannerOld(getActivity(), view, mViewModel);

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
        TreeMap<String, String> rightList;
        if (mViewModel.getSelectedUnit().getValue().isRepairUnit()) rightList = mViewModel.getRepairStatesDictionary().getValue();
        else rightList = mViewModel.getSerialStatesDictionary().getValue();

        SelectStateDialogNew dialog = new SelectStateDialogNew(getActivity(), mViewModel, getValueListFromMap(rightList));
        dialog.show();
    }

    ArrayList<String> getValueListFromMap(TreeMap<String, String> map) {
        ArrayList<String> list = new ArrayList<>();
        for(Map.Entry<String,String> entry : map.entrySet()) {
            String value = entry.getValue();
            list.add(value);
        }
        return list;
    }

    @Override
    public void onPause() {
        super.onPause();
        scanner.cameraSourceRelease();
    }

    @Override
    public void onResume() {
        super.onResume();
        scanner.initialiseDetectorsAndSources();
    }
}