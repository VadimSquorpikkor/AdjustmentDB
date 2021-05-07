package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;

import static com.squorpikkor.app.adjustmentdb.Utils.getIdByName;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EMPTY_VALUE_TEXT;

public class SelectStateDialogMulti extends BaseDialog {
    private EditText description;
    private Spinner spinner;
    private Spinner devSpinner;

    public SelectStateDialogMulti() {
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeWithVM(R.layout.dialog_select_states_multi);

        ArrayList<DUnit> unitList = mViewModel.getScannerFoundUnitsList().getValue();
        DUnit unit = unitList != null ? unitList.get(0) : null;

        //Загружается тот список, тип прибора который загружен — ремонт или серия
        ArrayList<String> rightList;
        if (mViewModel.getScannerFoundUnitsList().getValue().get(0).isRepairUnit())
            rightList = mViewModel.getRepairStatesNames().getValue();
        else rightList = mViewModel.getSerialStatesNames().getValue();
        rightList.add(0, EMPTY_VALUE_TEXT);

        //todo заменить ссылку на подписку
        ArrayList<String> nameList = mViewModel.getDeviceNameList().getValue();
        nameList.add(0, EMPTY_VALUE_TEXT);

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button okButton = view.findViewById(R.id.ok_button);
        description = view.findViewById(R.id.description);
        spinner = view.findViewById(R.id.state_spinner);
        devSpinner = view.findViewById(R.id.name_spinner);

        cancelButton.setOnClickListener(view -> dismiss());

        okButton.setOnClickListener(view -> {
            if (unit != null) {
                String state = "";
                String state_id = "";
                String type = unit.getType();
                String spinnerName = "";
                if (spinner.getSelectedItem() != null) state = spinner.getSelectedItem().toString();
                if (devSpinner.getSelectedItem() != null)
                    spinnerName = devSpinner.getSelectedItem().toString();
            /*int position;
            if (unit.isRepairUnit()){
                position = mViewModel.getRepairStatesNames().getValue().indexOf(state);
                state_id = mViewModel.getRepairStateIdList().getValue().get(position);
            }
            if (unit.isSerialUnit()){
                position = mViewModel.getSerialStatesNames().getValue().indexOf(state);
                state_id = mViewModel.getSerialStateIdList().getValue().get(position);
            }*/
                if (unit.isRepairUnit()
                        && mViewModel.getRepairStatesNames().getValue() != null
                        && mViewModel.getRepairStateIdList().getValue() != null) {
                    state_id = getIdByName(state, mViewModel.getRepairStatesNames().getValue(), mViewModel.getRepairStateIdList().getValue());
                }
                if (unit.isSerialUnit()
                        && mViewModel.getSerialStatesNames().getValue() != null
                        && mViewModel.getSerialStateIdList().getValue() != null) {
                    state_id = getIdByName(state, mViewModel.getSerialStatesNames().getValue(), mViewModel.getSerialStateIdList().getValue());
                }
                String desc = description.getText().toString();
                String location = mViewModel.getLocation_id().getValue();

                for (int i = 0; i < unitList.size(); i++) {
                    DUnit unitFromList = unitList.get(i);
                    String id = unitFromList.getId();
                    String name;
                    //Если у юнита уже было назначено имя (название устройства), то оно не будет перезаписано
                    if (unitFromList.getName() == null || unitFromList.getName().equals(""))
//                        name = spinnerName;
                        name = getIdByName(spinnerName, mViewModel.getDeviceNameList().getValue(), mViewModel.getDeviceIdList().getValue());
                    else name = unitFromList.getName();
                    String innerSerial = unitFromList.getInnerSerial();
                    String serial = unitFromList.getSerial();
                    Date date = unitFromList.getDate();
                    mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, state_id, desc, type, location, date));
                }
            }
            dismiss();
        });

        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, rightList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(stateAdapter);

        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, nameList);
        nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devSpinner.setAdapter(nameAdapter);

        return dialog;
    }
}
