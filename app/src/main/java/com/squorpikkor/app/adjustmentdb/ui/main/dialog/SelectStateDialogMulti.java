package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.MainActivity;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;

import java.util.ArrayList;

public class SelectStateDialogMulti extends Dialog {
    private MainViewModel mViewModel;
    private final Activity context;
    private EditText description;
    private Spinner spinner;
    private Spinner devSpinner;

    public SelectStateDialogMulti(@NonNull Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_states_multi);

        mViewModel = new ViewModelProvider((MainActivity)context).get(MainViewModel.class);

        ArrayList<DUnit> unitList = mViewModel.getScannerFoundUnitsList().getValue();
        DUnit unit = unitList.get(0);

        //Загружается тот список, тип прибора который загружен — ремонт или серия
        ArrayList<String> rightList;
        if (mViewModel.getScannerFoundUnitsList().getValue().get(0).isRepairUnit()) rightList = mViewModel.getRepairStatesNames().getValue();
        else rightList = mViewModel.getSerialStatesNames().getValue();

        //todo заменить ссылку на подписку
        ArrayList<String> nameList = mViewModel.getDeviceNameList().getValue();

        Button cancelButton = findViewById(R.id.cancel_button);
        Button okButton = findViewById(R.id.ok_button);
        description = findViewById(R.id.description);
        spinner = findViewById(R.id.state_spinner);
        devSpinner = findViewById(R.id.name_spinner);

        cancelButton.setOnClickListener(view -> dismiss());

        okButton.setOnClickListener(view -> {
            String state = "";
            String state_id = "";
            String type = unit.getType();
            String spinnerName = "";
            if (spinner.getSelectedItem()!=null) state = spinner.getSelectedItem().toString();
            if (devSpinner.getSelectedItem()!=null) spinnerName = devSpinner.getSelectedItem().toString();
            int position;
            if (unit.isRepairUnit()){
                position = mViewModel.getRepairStatesNames().getValue().indexOf(state);
                state_id = mViewModel.getRepairStateIdList().getValue().get(position);
            }
            if (unit.isSerialUnit()){
                position = mViewModel.getSerialStatesNames().getValue().indexOf(state);
                state_id = mViewModel.getSerialStateIdList().getValue().get(position);
            }
            String desc = description.getText().toString();
            String location = mViewModel.getLocation_id().getValue();

            for (int i = 0; i < unitList.size(); i++) {
                DUnit unitFromList = unitList.get(i);
                String id = unitFromList.getId();
                String name;
                //Если у юнита уже было назначено имя (название устройства), то оно не будет перезаписано
                if (unitFromList.getName()==null || unitFromList.getName().equals("")) name = spinnerName;
                else name = unitFromList.getName();
                String innerSerial = unitFromList.getInnerSerial();
                String serial = unitFromList.getSerial();
                mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, state_id, desc, type, location));
            }

            dismiss();
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, rightList);
        // Specify the layout to use when the list of choices appears
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(stateAdapter);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, nameList);
        // Specify the layout to use when the list of choices appears
        nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        devSpinner.setAdapter(nameAdapter);
    }
}
