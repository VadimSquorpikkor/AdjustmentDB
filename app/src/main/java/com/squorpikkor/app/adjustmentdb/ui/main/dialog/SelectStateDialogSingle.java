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

public class SelectStateDialogSingle extends Dialog {
    private MainViewModel mViewModel;
    private final Activity context;
    private EditText description;
    private DUnit unit;
    private Spinner spinner;

    public SelectStateDialogSingle(@NonNull Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_states_single);

        mViewModel = new ViewModelProvider((MainActivity)context).get(MainViewModel.class);
        unit = this.mViewModel.getSelectedUnit().getValue();

        //Загружается тот список, тип прибора который загружен — ремонт или серия
        ArrayList<String> rightList;
        if (mViewModel.getSelectedUnit().getValue().isRepairUnit()) rightList = mViewModel.getRepairStatesNames().getValue();
        else rightList = mViewModel.getSerialStatesNames().getValue();

        Button cancelButton = findViewById(R.id.cancel_button);
        Button okButton = findViewById(R.id.ok_button);
        description = findViewById(R.id.description);
        spinner = findViewById(R.id.state_spinner);

        cancelButton.setOnClickListener(view -> dismiss());

        okButton.setOnClickListener(view -> {
            String id = unit.getId();
            String name = unit.getName();
            String innerSerial = unit.getInnerSerial();
            String serial = unit.getSerial();
            String state = "";
            String state_id = "";
            if (spinner.getSelectedItem()!=null) state = spinner.getSelectedItem().toString();
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
            String type = unit.getType();
            String location = mViewModel.getLocation_id().getValue();
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
    }

}
