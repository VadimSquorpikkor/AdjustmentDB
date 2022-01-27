package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.SaveLoad;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.ShortStateAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.State;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;

import static com.squorpikkor.app.adjustmentdb.Constant.LOCATION_ADJUSTMENT;
import static com.squorpikkor.app.adjustmentdb.Constant.LOCATION_GR_SERVISA;
import static com.squorpikkor.app.adjustmentdb.Utils.generateTrackId;
import static com.squorpikkor.app.adjustmentdb.Utils.getPreviouslyGeneratedTrackId;
import static com.squorpikkor.app.adjustmentdb.Utils.isEmptyOrNull;
import static com.squorpikkor.app.adjustmentdb.Constant.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.Constant.EMPTY_VALUE_TEXT;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SelectStateDialogSingle extends BaseDialog {

    private SpinnerAdapter deviceSpinnerAdapter;
    private SpinnerAdapter stateSpinnerAdapter;
    private SpinnerAdapter employeeSpinnerAdapter;
    private SpinnerAdapter deviceSetSpinnerAdapter;

    private EditText descriptionEdit;
    private EditText innerEdit;
    private EditText serialEdit;
    private EditText trackIdEdit;

    private String location;
    private DUnit unit;

    private ShortStateAdapter adapter;

    public static final String STATE_SINGLE_DIALOG_TAB_STATE = "state_single_dialog_tab_state";


    public SelectStateDialogSingle() {
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeWithVM(R.layout.dialog_select_states_single);

        unit = mViewModel.getSelectedUnit().getValue();
        location = mViewModel.getLocation_id().getValue();

        Spinner deviceSetSpinner = view.findViewById(R.id.spinnerDevSetName);
        Spinner devicesSpinner = view.findViewById(R.id.newName);
        Spinner statesSpinner = view.findViewById(R.id.state_spinner);
        Spinner employeeSpinner = view.findViewById(R.id.state_spinner_employee);

        deviceSetSpinnerAdapter = new SpinnerAdapter(deviceSetSpinner, mContext);
        deviceSpinnerAdapter = new SpinnerAdapter(devicesSpinner, mContext);
        stateSpinnerAdapter = new SpinnerAdapter(statesSpinner, mContext);
        employeeSpinnerAdapter = new SpinnerAdapter(employeeSpinner, mContext);

        RecyclerView stateNamesRecycler = view.findViewById(R.id.recycler_state_name);
        adapter = new ShortStateAdapter();
//        adapter.setOnItemClickListener(this::saveUnitNewStateOnly);
        adapter.setOnItemClickListener(name -> saveUnit(unit));
        stateNamesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        stateNamesRecycler.setAdapter(adapter);

        mViewModel.getDeviceSets().observe(this, list2 -> deviceSetSpinnerAdapter.setData(list2, EMPTY_VALUE_TEXT));
        mViewModel.getDevices().observe(this, list1 -> deviceSpinnerAdapter.setDataByDevSet(list1, deviceSetSpinnerAdapter.getSelectedNameId(), EMPTY_VALUE_TEXT));
        /*if (unit!=null) */mViewModel.getStates().observe(this, list -> stateSpinnerAdapter.setDataByTypeAndLocation(list, unit.getType(), location, EMPTY_VALUE_TEXT));
        /*if (unit!=null) */mViewModel.getStates().observe(this, this::updateShortStateRecycler);
        mViewModel.getEmployees().observe(this, list -> employeeSpinnerAdapter.setData(list, EMPTY_VALUE_TEXT));

        deviceSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {updateDeviceSpinner();}
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button okButton = view.findViewById(R.id.ok_button);
        Button generateTrackIdButton = view.findViewById(R.id.button_generate);
        Button pasteTrackIdButton = view.findViewById(R.id.paste_button);

        TextView nameText = view.findViewById(R.id.dName);
        descriptionEdit = view.findViewById(R.id.description);
        innerEdit = view.findViewById(R.id.dInner);
        serialEdit = view.findViewById(R.id.dSerial);
        trackIdEdit = view.findViewById(R.id.dTrackId);

        TextView labelDevSet = view.findViewById(R.id.dialogSetNameLabel);
        TextView labelDevices = view.findViewById(R.id.dialogNewNameLabel);
        TextView labelInner = view.findViewById(R.id.dialogInnerLabel);
        TextView labelSerial = view.findViewById(R.id.dialogSerialLabel);
        TextView labelEmployee = view.findViewById(R.id.dialogEmployeeLabel);
        TextView labelTrackId = view.findViewById(R.id.dialogTrackIdLabel);

        devicesSpinner.setVisibility(View.GONE);
        nameText.setVisibility(View.GONE);
        innerEdit.setVisibility(View.GONE);
        serialEdit.setVisibility(View.GONE);
        trackIdEdit.setVisibility(View.GONE);
        employeeSpinner.setVisibility(View.GONE);

        labelDevices.setVisibility(View.GONE);
        labelInner.setVisibility(View.GONE);
        labelSerial.setVisibility(View.GONE);
        labelEmployee.setVisibility(View.GONE);
        labelTrackId.setVisibility(View.GONE);

        generateTrackIdButton.setVisibility(View.GONE);
        pasteTrackIdButton.setVisibility(View.GONE);

        String savedTrackId = getPreviouslyGeneratedTrackId();

        //Если у юнита уже есть серийный или внутренний номер или имя или ответственный, то его уже нельзя поменять, поэтому я просто скрываю его
        if (unit != null) {
            if (isEmptyOrNull(unit.getName())) {
                devicesSpinner.setVisibility(View.VISIBLE);
                labelDevices.setVisibility(View.VISIBLE);
            } else {
                nameText.setVisibility(View.VISIBLE);
                nameText.setText(unit.getName());
            }
            if (isEmptyOrNull(unit.getInnerSerial())) {
                innerEdit.setVisibility(View.VISIBLE);
                labelInner.setVisibility(View.VISIBLE);
            } else
                innerEdit.setText(unit.getInnerSerial()); //смысл — если unit.getInnerSerial()==null, то и setText не нужно делать (иначе номер будет "null", а надо "")
            if (isEmptyOrNull(unit.getSerial())) {
                serialEdit.setVisibility(View.VISIBLE);
                labelSerial.setVisibility(View.VISIBLE);
            } else serialEdit.setText(unit.getSerial());
            if (isEmptyOrNull(unit.getEmployee())) {
                employeeSpinner.setVisibility(View.VISIBLE);
                labelEmployee.setVisibility(View.VISIBLE);
            }
            if (unit.isRepairUnit()&&(unit.getTrackId()==null||unit.getTrackId().equals("")||unit.getTrackId().equals("null"))) { //для TRACKID
                labelTrackId.setVisibility(View.VISIBLE);
                trackIdEdit.setVisibility(View.VISIBLE);
                generateTrackIdButton.setVisibility(View.VISIBLE);
                if (!savedTrackId.equals("")) pasteTrackIdButton.setVisibility(View.VISIBLE);
            }
        }

        cancelButton.setOnClickListener(view -> dismiss());
        okButton.setOnClickListener(view -> saveUnit(unit));
        generateTrackIdButton.setOnClickListener(v -> trackIdEdit.setText(generateTrackId()));
        pasteTrackIdButton.setOnClickListener(v -> trackIdEdit.setText(getPreviouslyGeneratedTrackId()));

        TabLayout tabs = view.findViewById(R.id.tab_layout);

        //Если локация — это регулировка или группа сервиса, то показываются вкладки выбора типа
        // диалога: кратко или подробно, иначе — доступен только краткий режим. Другими словами для
        // пользователей не с участка регулировки или сервиса не будет доступен расширенный режим
        // диалога (эти пользователи могут выбирать только статус для устройства)
        if (mViewModel.getLocation_id().getValue().equals(LOCATION_ADJUSTMENT) || mViewModel.getLocation_id().getValue().equals(LOCATION_GR_SERVISA)) {
            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) { toggleTab(tab.getPosition()); }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
            //установить вариант диалога при последнем выборе
            int tabIndex = SaveLoad.loadInt(STATE_SINGLE_DIALOG_TAB_STATE, 1);//по умолчанию "Кратко"
            toggleTab(tabIndex);
            tabs.getTabAt(tabIndex).select();
        } else {
            tabs.setVisibility(View.GONE);
            toggleTab(1);
        }
        return dialog;
    }

    private void updateShortStateRecycler(ArrayList<State> list) {
        ArrayList<String> ids = stateSpinnerAdapter.getNameIdsByTypeAndLocation(list, unit.getType(), location);
        ArrayList<String> names = stateSpinnerAdapter.getNamesByTypeAndLocation(list, unit.getType(), location);
        adapter.setList(ids, names);
    }

    private void toggleTab(int tab) {
        SaveLoad.save(STATE_SINGLE_DIALOG_TAB_STATE, tab);
        if (tab == 0) {
            view.findViewById(R.id.layout_detail).setVisibility(View.VISIBLE);
            view.findViewById(R.id.layout_short).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.layout_detail).setVisibility(View.GONE);
            view.findViewById(R.id.layout_short).setVisibility(View.VISIBLE);
        }
    }

    /**Вариант сохранения юнита, когда обновляется единственный параметр — статус устройства*/
    private void saveUnitNewStateOnly(String name) {
        if (unit.getDate()==null) unit.setDate(new Date());
        unit.addNewEvent(mViewModel, name, "", location);
        mViewModel.saveUnitAndEvent(unit);
        dismiss();
    }

    private void updateDeviceSpinner() {
        deviceSpinnerAdapter.setDataByDevSet(mViewModel.getDevices().getValue(), deviceSetSpinnerAdapter.getSelectedNameId(), EMPTY_VALUE_TEXT);
    }

    private void saveUnit(DUnit unit) {
        updateUnitData(unit);
        mViewModel.saveUnitAndEvent(unit);
        dismiss();
    }

    private void updateUnitData(DUnit unit) {
        String newNameId = deviceSpinnerAdapter.getSelectedNameId();
        String newInner = innerEdit.getText().toString();
        String newSerial = serialEdit.getText().toString();
        String newStateId = stateSpinnerAdapter.getSelectedNameId();
        String employee = employeeSpinnerAdapter.getSelectedNameId();
        String description = descriptionEdit.getText().toString();
        String devSetId = deviceSetSpinnerAdapter.getSelectedNameId();
        String trackId = trackIdEdit.getText().toString();

        if (unit.getName().equals("") && !newNameId.equals(ANY_VALUE)) unit.setName(newNameId);
        if (unit.getInnerSerial().equals("") && !newInner.equals("")) unit.setInnerSerial(newInner);
        if (unit.getSerial().equals("") && !newSerial.equals("")) unit.setSerial(newSerial);
        if (unit.getDate()==null) unit.setDate(new Date());
        if (!newStateId.equals(ANY_VALUE)) unit.addNewEvent(mViewModel, newStateId, description, location);
        if (!employee.equals(ANY_VALUE)) unit.setEmployee(employee);
        if (!devSetId.equals(ANY_VALUE)) unit.setDeviceSet(devSetId);
//        if (!trackId.equals("") && !(unit.getTrackId()==null || unit.getTrackId().equals(""))) unit.setTrackId(trackId);
        if (!trackId.equals("") && (unit.getTrackId()==null || unit.getTrackId().equals("") || unit.getTrackId().equals("null"))) unit.setTrackId(trackId);
    }
}
