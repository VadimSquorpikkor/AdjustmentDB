package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.MainActivity;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static com.squorpikkor.app.adjustmentdb.Utils.getIdByName;
import static com.squorpikkor.app.adjustmentdb.Utils.getRightValue;
import static com.squorpikkor.app.adjustmentdb.Utils.isEmptyOrNull;

public class SelectStateDialogSingle extends Dialog {
    private final Activity context;

    public SelectStateDialogSingle(@NonNull Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_states_single);

        MainViewModel mViewModel = new ViewModelProvider((MainActivity) context).get(MainViewModel.class);

        DUnit unit = mViewModel.getSelectedUnit().getValue();

        //Загружается тот список, тип прибора который загружен — ремонт или серия
        ArrayList<String> rightList;
        if (Objects.requireNonNull(mViewModel.getSelectedUnit().getValue()).isRepairUnit())
            rightList = new ArrayList<>(Objects.requireNonNull(mViewModel.getRepairStatesNames().getValue()));
        else
            rightList = new ArrayList<>(Objects.requireNonNull(mViewModel.getSerialStatesNames().getValue()));
        rightList.add(0, "");

        ArrayList<String> employeeList = new ArrayList<>(Objects.requireNonNull(mViewModel.getEmployeeNamesList().getValue()));
        employeeList.add(0, "");//Добавляю пустой элемент в начало списка

        ArrayList<String> devIdList = new ArrayList<>(Objects.requireNonNull(mViewModel.getDeviceIdList().getValue()));
        devIdList.add(0, "");

        Button cancelButton = findViewById(R.id.cancel_button);
        Button okButton = findViewById(R.id.ok_button);
        EditText description = findViewById(R.id.description);
        Spinner spinner = findViewById(R.id.state_spinner);
        Spinner employee = findViewById(R.id.state_spinner_employee);
        Spinner devices = findViewById(R.id.newName);
        TextView tName = findViewById(R.id.dName);
        EditText inner = findViewById(R.id.dInner);
        EditText eSerial = findViewById(R.id.dSerial);

        TextView labelDevices = findViewById(R.id.dialogNewNameLabel);
        TextView labelInner = findViewById(R.id.dialogInnerLabel);
        TextView labelSerial = findViewById(R.id.dialogSerialLabel);
        TextView labelEmployee = findViewById(R.id.dialogEmployeeLabel);

        devices.setVisibility(View.GONE);
        tName.setVisibility(View.GONE);
        inner.setVisibility(View.GONE);
        eSerial.setVisibility(View.GONE);
        employee.setVisibility(View.GONE);

        labelDevices.setVisibility(View.GONE);
        labelInner.setVisibility(View.GONE);
        labelSerial.setVisibility(View.GONE);
        labelEmployee.setVisibility(View.GONE);

        ////if (!isEmptyOrNull(unit.getName())) tName.setText(unit.getName());


        //Если у юнита уже есть серийный или внутренний номер или имя или ответственный, то его уже нельзя поменять, поэтому я просто скрываю его
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

        cancelButton.setOnClickListener(view -> dismiss());

        //todo надо сделать не через создание нового юнита, а через присваивание параметров уже существующему юниту, затем этот старый юнит сохранять
        okButton.setOnClickListener(view -> {
            String id = unit.getId();
            String name = getRightValue(unit.getName(), devices.getSelectedItem().toString());
            String innerSerial = getRightValue(unit.getInnerSerial(), inner.getText().toString());
            String serial = getRightValue(unit.getSerial(), eSerial.getText().toString());
            String state = "";
            String state_id = "";
            if (spinner.getSelectedItem() != null) state = spinner.getSelectedItem().toString();
            if (!state.equals("")) {
                if (unit.isRepairUnit()) {
                    state_id = getIdByName(state, mViewModel.getRepairStatesNames().getValue(), mViewModel.getRepairStateIdList().getValue());
                }
                if (unit.isSerialUnit()) {
                    state_id = getIdByName(state, mViewModel.getSerialStatesNames().getValue(), mViewModel.getSerialStateIdList().getValue());
                }
            } else {
                state_id = "";
            }

            String desc = description.getText().toString();
            String type = unit.getType();
            String location = mViewModel.getLocation_id().getValue();
            Date date = unit.getDate();
//            mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, state_id, desc, type, location));
            mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, state_id, desc, type, location, date), unit.getState());
            dismiss();
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, rightList);
        // Specify the layout to use when the list of choices appears
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(stateAdapter);

        ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, employeeList);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employee.setAdapter(employeeAdapter);

        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, devIdList);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devices.setAdapter(deviceAdapter);
    }

}
