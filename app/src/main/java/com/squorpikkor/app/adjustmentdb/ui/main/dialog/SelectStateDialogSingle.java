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

import static com.squorpikkor.app.adjustmentdb.Utils.insertRightValue;
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
        if (mViewModel.getSelectedUnit().getValue().isRepairUnit())
            rightList = mViewModel.getRepairStatesNames().getValue();
        else rightList = mViewModel.getSerialStatesNames().getValue();
        rightList.add(0, "");

        ArrayList<String> employeeList = mViewModel.getEmployeeNamesList().getValue();
        employeeList.add(0, "");//Добавляю пустой элемент в начало списка

        Button cancelButton = findViewById(R.id.cancel_button);
        Button okButton = findViewById(R.id.ok_button);
        EditText description = findViewById(R.id.description);
        Spinner spinner = findViewById(R.id.state_spinner);
        TextView tName = findViewById(R.id.dName);
        EditText inner = findViewById(R.id.dInner);
        EditText eSerial = findViewById(R.id.dSerial);
        Spinner employee = findViewById(R.id.state_spinner_employee);

        TextView labelInner = findViewById(R.id.dialogInnerLabel);
        TextView labelSerial = findViewById(R.id.dialogSerialLabel);
        TextView labelEmployee = findViewById(R.id.dialogEmployeeLabel);

        inner.setVisibility(View.GONE);
        eSerial.setVisibility(View.GONE);
        employee.setVisibility(View.GONE);

        labelInner.setVisibility(View.GONE);
        labelSerial.setVisibility(View.GONE);
        labelEmployee.setVisibility(View.GONE);

        tName.setText(unit.getName());
        //Если у юнита уже есть серийный или внутренний номер, то его уже нельзя поменять, поэтому я просто скрываю его
        if (isEmptyOrNull(unit.getInnerSerial())) {
            inner.setVisibility(View.VISIBLE);
            labelInner.setVisibility(View.VISIBLE);
        }
        if (isEmptyOrNull(unit.getSerial())) {
            eSerial.setVisibility(View.VISIBLE);
            labelSerial.setVisibility(View.VISIBLE);
        }
        if (isEmptyOrNull(unit.getEmployee())) {
            employee.setVisibility(View.VISIBLE);
            labelEmployee.setVisibility(View.VISIBLE);
        }

        inner.setText(unit.getInnerSerial());
        eSerial.setText(unit.getSerial());

        cancelButton.setOnClickListener(view -> dismiss());

        okButton.setOnClickListener(view -> {
            String id = unit.getId();
            String name = unit.getName();
            String innerSerial = insertRightValue(unit.getInnerSerial(), inner.getText().toString());
            String serial = insertRightValue(unit.getSerial(), eSerial.getText().toString());
            String state = "";
            String state_id = "";
            if (spinner.getSelectedItem() != null) state = spinner.getSelectedItem().toString();
            if (!state.equals("")) {
                int position;
                if (unit.isRepairUnit()) {
                    position = mViewModel.getRepairStatesNames().getValue().indexOf(state);
                    position--;//отнимаю 1, потому как в rightList добавлена первым элементом пустая строка
                    state_id = mViewModel.getRepairStateIdList().getValue().get(position);
                }
                if (unit.isSerialUnit()) {
                    position = mViewModel.getSerialStatesNames().getValue().indexOf(state);
                    position--;//отнимаю 1, потому как в rightList добавлена первым элементом пустая строка
                    state_id = mViewModel.getSerialStateIdList().getValue().get(position);
                }
            } else {
                state_id = "";
            }

            String desc = description.getText().toString();
            String type = unit.getType();
            String location = mViewModel.getLocation_id().getValue();
//            mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, unit.getState(), desc, type, location), state_id);
            mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, state_id, desc, type, location));
            dismiss();
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, rightList);
        // Specify the layout to use when the list of choices appears
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(stateAdapter);

        ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, employeeList);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employee.setAdapter(employeeAdapter);
    }

}
