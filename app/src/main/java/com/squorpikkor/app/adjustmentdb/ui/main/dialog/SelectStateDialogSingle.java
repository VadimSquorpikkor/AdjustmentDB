package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import org.jetbrains.annotations.NotNull;
import java.util.Date;

import static com.squorpikkor.app.adjustmentdb.Utils.isEmptyOrNull;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EMPTY_VALUE_TEXT;

public class SelectStateDialogSingle extends BaseDialog {

    private SpinnerAdapter deviceSpinnerAdapter;
    private SpinnerAdapter stateSpinnerAdapter;
    private SpinnerAdapter employeeSpinnerAdapter;
    private SpinnerAdapter deviceSetSpinnerAdapter;

    EditText descriptionEdit;
    EditText innerEdit;
    EditText serialEdit;
    TextView nameText;

    String location;
    DUnit unit;

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

        mViewModel.getDeviceSets().observe(this, list2 -> deviceSetSpinnerAdapter.setData(list2, EMPTY_VALUE_TEXT));
        mViewModel.getDevices().observe(this, list1 -> deviceSpinnerAdapter.setDataByDevSet(list1, deviceSetSpinnerAdapter.getSelectedNameId(), EMPTY_VALUE_TEXT));
        mViewModel.getStates().observe(this, list -> stateSpinnerAdapter.setDataByTypeAndLocation(list, unit.getType(), location, EMPTY_VALUE_TEXT));
        mViewModel.getEmployees().observe(this, list -> employeeSpinnerAdapter.setData(list, EMPTY_VALUE_TEXT));

        deviceSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {updateDeviceSpinner();}
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button okButton = view.findViewById(R.id.ok_button);

        nameText = view.findViewById(R.id.dName);
        descriptionEdit = view.findViewById(R.id.description);
        innerEdit = view.findViewById(R.id.dInner);
        serialEdit = view.findViewById(R.id.dSerial);

        TextView labelDevSet = view.findViewById(R.id.dialogSetNameLabel);
        TextView labelDevices = view.findViewById(R.id.dialogNewNameLabel);
        TextView labelInner = view.findViewById(R.id.dialogInnerLabel);
        TextView labelSerial = view.findViewById(R.id.dialogSerialLabel);
        TextView labelEmployee = view.findViewById(R.id.dialogEmployeeLabel);

        devicesSpinner.setVisibility(View.GONE);
        nameText.setVisibility(View.GONE);
        innerEdit.setVisibility(View.GONE);
        serialEdit.setVisibility(View.GONE);
        employeeSpinner.setVisibility(View.GONE);

        labelDevices.setVisibility(View.GONE);
        labelInner.setVisibility(View.GONE);
        labelSerial.setVisibility(View.GONE);
        labelEmployee.setVisibility(View.GONE);

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
        }

        cancelButton.setOnClickListener(view -> dismiss());
        okButton.setOnClickListener(view -> saveUnit(unit));

        return dialog;
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

        if (unit.getName().equals("") && !newNameId.equals(ANY_VALUE)) unit.setName(newNameId);
        if (unit.getInnerSerial().equals("") && !newInner.equals("")) unit.setInnerSerial(newInner);
        if (unit.getSerial().equals("") && !newSerial.equals("")) unit.setSerial(newSerial);
        if (unit.getDate()==null) unit.setDate(new Date());
        if (!newStateId.equals(ANY_VALUE)) unit.addNewEvent(mViewModel, newStateId, description, location);
        if (!employee.equals(ANY_VALUE)) unit.setEmployee(employee);
        if (!devSetId.equals(ANY_VALUE)) unit.setDeviceSet(devSetId);
    }
}
