package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static com.squorpikkor.app.adjustmentdb.Utils.getIdByName;
import static com.squorpikkor.app.adjustmentdb.Utils.getRightValue;
import static com.squorpikkor.app.adjustmentdb.Utils.isEmptyOrNull;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EMPTY_VALUE_TEXT;

public class SelectStateDialogSingle extends BaseDialog {

    public SelectStateDialogSingle() {
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeWithVM(R.layout.dialog_select_states_single);

        DUnit unit = mViewModel.getSelectedUnit().getValue();

        //Загружается тот список, тип прибора который загружен — ремонт или серия
        ArrayList<String> rightList;
        if (Objects.requireNonNull(mViewModel.getSelectedUnit().getValue()).isRepairUnit())
            rightList = new ArrayList<>(Objects.requireNonNull(mViewModel.getRepairStatesNames().getValue()));
        else
            rightList = new ArrayList<>(Objects.requireNonNull(mViewModel.getSerialStatesNames().getValue()));
        rightList.add(0, EMPTY_VALUE_TEXT);

        ArrayList<String> employeeList = new ArrayList<>(Objects.requireNonNull(mViewModel.getEmployeeNamesList().getValue()));
        employeeList.add(0, EMPTY_VALUE_TEXT);//Добавляю пустой элемент в начало списка

        ArrayList<String> devIdList = new ArrayList<>(Objects.requireNonNull(mViewModel.getDeviceIdList().getValue()));
        devIdList.add(0, EMPTY_VALUE_TEXT);

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button okButton = view.findViewById(R.id.ok_button);
        EditText description = view.findViewById(R.id.description);
        Spinner spinner = view.findViewById(R.id.state_spinner);
        Spinner employee = view.findViewById(R.id.state_spinner_employee);
        Spinner devices = view.findViewById(R.id.newName);
        TextView tName = view.findViewById(R.id.dName);
        EditText inner = view.findViewById(R.id.dInner);
        EditText eSerial = view.findViewById(R.id.dSerial);

        TextView labelDevices = view.findViewById(R.id.dialogNewNameLabel);
        TextView labelInner = view.findViewById(R.id.dialogInnerLabel);
        TextView labelSerial = view.findViewById(R.id.dialogSerialLabel);
        TextView labelEmployee = view.findViewById(R.id.dialogEmployeeLabel);

        devices.setVisibility(View.GONE);
        tName.setVisibility(View.GONE);
        inner.setVisibility(View.GONE);
        eSerial.setVisibility(View.GONE);
        employee.setVisibility(View.GONE);

        labelDevices.setVisibility(View.GONE);
        labelInner.setVisibility(View.GONE);
        labelSerial.setVisibility(View.GONE);
        labelEmployee.setVisibility(View.GONE);

        //Если у юнита уже есть серийный или внутренний номер или имя или ответственный, то его уже нельзя поменять, поэтому я просто скрываю его
        if (unit != null) {
            if (isEmptyOrNull(unit.getName())) {
                devices.setVisibility(View.VISIBLE);
                labelDevices.setVisibility(View.VISIBLE);
            } else {
                tName.setVisibility(View.VISIBLE);
                tName.setText(unit.getName());
            }
            if (isEmptyOrNull(unit.getInnerSerial())) {
                inner.setVisibility(View.VISIBLE);
                labelInner.setVisibility(View.VISIBLE);
            } else
                inner.setText(unit.getInnerSerial()); //смысл — если unit.getInnerSerial()==null, то и setText не нужно делать (иначе номер будет "null", а надо "")
            if (isEmptyOrNull(unit.getSerial())) {
                eSerial.setVisibility(View.VISIBLE);
                labelSerial.setVisibility(View.VISIBLE);
            } else eSerial.setText(unit.getSerial());
            if (isEmptyOrNull(unit.getEmployee())) {
                employee.setVisibility(View.VISIBLE);
                labelEmployee.setVisibility(View.VISIBLE);
            }
        }

        cancelButton.setOnClickListener(view -> dismiss());

        //todo надо сделать не через создание нового юнита, а через присваивание параметров уже существующему юниту, затем этот старый юнит сохранять
        okButton.setOnClickListener(view -> {
            if (unit != null) {
                String id = unit.getId();
                String name = getRightValue(unit.getName(), devices.getSelectedItem().toString());
                String innerSerial = getRightValue(unit.getInnerSerial(), inner.getText().toString());
                String serial = getRightValue(unit.getSerial(), eSerial.getText().toString());
                String state = "";
                String state_id = "";
                if (spinner.getSelectedItem() != null) state = spinner.getSelectedItem().toString();
                if (!state.equals(EMPTY_VALUE_TEXT)) {
                    if (unit.isRepairUnit()
                            && mViewModel.getRepairStatesNames().getValue()!=null
                            && mViewModel.getRepairStateIdList().getValue()!=null) {
                        state_id = getIdByName(state, mViewModel.getRepairStatesNames().getValue(), mViewModel.getRepairStateIdList().getValue());
                    }
                    if (unit.isSerialUnit()
                            && mViewModel.getSerialStatesNames().getValue()!=null
                            && mViewModel.getSerialStateIdList().getValue()!=null) {
                        state_id = getIdByName(state, mViewModel.getSerialStatesNames().getValue(), mViewModel.getSerialStateIdList().getValue());
                    }
                } else {
                    state_id = "";
                }

                name = getIdByName(name, mViewModel.getDeviceNameList().getValue(), mViewModel.getDeviceIdList().getValue());
                String desc = description.getText().toString();
                String type = unit.getType();
                String location = mViewModel.getLocation_id().getValue();
                Date date = unit.getDate();
                mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, state_id, desc, type, location, date), unit.getState());
            }
            dismiss();
        });

        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, rightList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(stateAdapter);

        ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, employeeList);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employee.setAdapter(employeeAdapter);

        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, devIdList);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devices.setAdapter(deviceAdapter);

        return dialog;
    }

}
