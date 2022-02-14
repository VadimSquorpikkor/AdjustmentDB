package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.tabs.TabLayout;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.SaveLoad;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.ShortStateAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.State;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;

import static com.squorpikkor.app.adjustmentdb.Constant.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.Constant.EMPTY_VALUE_TEXT;
import static com.squorpikkor.app.adjustmentdb.Constant.LOCATION_ADJUSTMENT;
import static com.squorpikkor.app.adjustmentdb.Constant.LOCATION_GR_SERVISA;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SelectStateDialogMulti extends BaseDialog {
    private EditText descriptionEdit;
    private String location;

    private SpinnerAdapter stateSpinnerAdapter;
    private SpinnerAdapter deviceSpinnerAdapter;
    private SpinnerAdapter employeeSpinnerAdapter;
    private SpinnerAdapter deviceSetSpinnerAdapter;

    private ArrayList<DUnit> mUnitList;
    private ShortStateAdapter shortStateAdapter;

    public static final String STATE_MULTI_DIALOG_TAB_STATE = "state_multi_dialog_tab_state";

    public SelectStateDialogMulti() {
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeWithVM(R.layout.dialog_select_states_multi);

        location = mViewModel.getLocation_id().getValue();

        mUnitList = mViewModel.getScannerFoundUnitsList().getValue();
        DUnit unit = mUnitList != null ? mUnitList.get(0) : null;//todo переименовать -> firstUnit
        String unitType = unit==null?null:unit.getType();

        Spinner deviceSetSpinner = view.findViewById(R.id.spinnerDevSetName);
        Spinner stateSpinner = view.findViewById(R.id.state_spinner);
        Spinner deviceSpinner = view.findViewById(R.id.name_spinner);
        Spinner employeeSpinner = view.findViewById(R.id.employee_spinner);

        deviceSetSpinnerAdapter = new SpinnerAdapter(deviceSetSpinner, mContext);
        deviceSpinnerAdapter = new SpinnerAdapter(deviceSpinner, mContext);
        stateSpinnerAdapter = new SpinnerAdapter(stateSpinner, mContext);
        employeeSpinnerAdapter = new SpinnerAdapter(employeeSpinner, mContext);

        RecyclerView stateNamesRecycler = view.findViewById(R.id.recycler_state_name);
        shortStateAdapter = new ShortStateAdapter();
        shortStateAdapter.setOnItemClickListener(this::saveUnitByState);
//        shortStateAdapter.setOnItemClickListener(name -> saveUnits(mUnitList));
        stateNamesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        stateNamesRecycler.setAdapter(shortStateAdapter);

        mViewModel.getDeviceSets().observe(this, list2 -> deviceSetSpinnerAdapter.setData(list2, EMPTY_VALUE_TEXT));
        mViewModel.getDevices().observe(this, list1 -> deviceSpinnerAdapter.setDataByDevSet(list1, deviceSetSpinnerAdapter.getSelectedNameId(), EMPTY_VALUE_TEXT));
        mViewModel.getStates().observe(this, list -> stateSpinnerAdapter.setDataByTypeAndLocation(list, unitType, location, EMPTY_VALUE_TEXT));
        mViewModel.getStates().observe(this, this::updateShortStateRecycler);
        mViewModel.getEmployees().observe(this, list -> employeeSpinnerAdapter.setData(list, EMPTY_VALUE_TEXT));

        deviceSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {updateDeviceSpinner();}
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button okButton = view.findViewById(R.id.ok_button);
        descriptionEdit = view.findViewById(R.id.description);

        cancelButton.setOnClickListener(view -> dismiss());
        okButton.setOnClickListener(view -> saveUnits(mUnitList));

        TabLayout tabs = view.findViewById(R.id.tab_layout);
        //Если локация — это регулировка или группа сервиса, то показываются вкладки выбора типа
        // диалога: кратко или подробно, иначе — доступен только краткий режим. Другими словами для
        // пользователей не с участка регулировки или сервиса не будет доступен расширенный режим
        // диалога (эти пользователи могут выбирать только статус для устройства)
        if (mViewModel.getLocation_id().getValue()!=null &&
                (mViewModel.getLocation_id().getValue().equals(LOCATION_ADJUSTMENT) || mViewModel.getLocation_id().getValue().equals(LOCATION_GR_SERVISA))) {
            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    toggleTab(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
            //установить вариант диалога при последнем выборе
            int tabIndex = SaveLoad.loadInt(STATE_MULTI_DIALOG_TAB_STATE, 1);//по умолчанию "Кратко"
            toggleTab(tabIndex);
            tabs.getTabAt(tabIndex).select();
        } else {
            tabs.setVisibility(View.GONE);
            toggleTab(1);
        }

        return dialog;
    }

    private void updateShortStateRecycler(ArrayList<State> list) {
        if (list==null||list.size()==0) return;
        ArrayList<String> ids = stateSpinnerAdapter.getNameIdsByTypeAndLocation(list, mUnitList.get(0).getType(), location);
        ArrayList<String> names = stateSpinnerAdapter.getNamesByTypeAndLocation(list, mUnitList.get(0).getType(), location);
        shortStateAdapter.setList(ids, names);
    }

    private void toggleTab(int tab) {
        SaveLoad.save(STATE_MULTI_DIALOG_TAB_STATE, tab);
        if (tab == 0) {
            view.findViewById(R.id.layout_detail).setVisibility(View.VISIBLE);
            view.findViewById(R.id.layout_short).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.layout_detail).setVisibility(View.GONE);
            view.findViewById(R.id.layout_short).setVisibility(View.VISIBLE);
        }
    }

    /**Вариант сохранения юнита, когда сохраняется через диалог "Кратко"*/
    private void saveUnitByState(String name) {
        if (mUnitList==null||mUnitList.size()==0) return;
            for (DUnit unit:mUnitList) {
                if (unit.getDate()==null) unit.setDate(new Date());
                updateUnitData(unit);
                unit.addNewEvent(mViewModel, name, "", location);
                mViewModel.saveUnitAndEvent(unit);
            }
        dismiss();
    }

    private void updateDeviceSpinner() {
        deviceSpinnerAdapter.setDataByDevSet(mViewModel.getDevices().getValue(), deviceSetSpinnerAdapter.getSelectedNameId(), EMPTY_VALUE_TEXT);
    }

    private void saveUnits(ArrayList<DUnit> unitList) {
        if (unitList==null||unitList.size()==0) return;
        for (int i = 0; i < unitList.size(); i++) {
            DUnit dUnit = unitList.get(i);
            updateUnitData(dUnit);
            mViewModel.saveUnitAndEvent(dUnit);
        }
        dismiss();
    }

    private void updateUnitData(DUnit unit) {
        String newNameId = deviceSpinnerAdapter.getSelectedNameId();
        String newStateId = stateSpinnerAdapter.getSelectedNameId();
        String employee = employeeSpinnerAdapter.getSelectedNameId();
        String description = descriptionEdit.getText().toString();
        String devSetId = deviceSetSpinnerAdapter.getSelectedNameId();

        if (unit.getName().equals("") && !newNameId.equals(ANY_VALUE)) unit.setName(newNameId);
        if (unit.getDate()==null) unit.setDate(new Date());
        unit.setLastDate(new Date());//новенькое
        if (!newStateId.equals(ANY_VALUE)) unit.addNewEvent(mViewModel, newStateId, description, location);
        if (!employee.equals(ANY_VALUE)) unit.setEmployee(employee);
        if (!devSetId.equals(ANY_VALUE)) unit.setDeviceSet(devSetId);
    }
}
