package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import java.util.ArrayList;

public class SelectStateDialogNew extends Dialog {
    private final MainViewModel mViewModel;
    private final Activity context;
    private final ArrayList<String> stateList;
    private EditText description;
    private final DUnit unit;
    private Spinner spinner;

    public SelectStateDialogNew(@NonNull Activity context, MainViewModel mViewModel, ArrayList<String> stateList) {
        super(context);
        this.context = context;
        this.mViewModel = mViewModel;
        this.stateList = stateList;
        this.unit = this.mViewModel.getSelectedUnit().getValue();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_states_new);

        TextView cancelButton = findViewById(R.id.textViewInnerSerial);
        TextView okButton = findViewById(R.id.textViewInnerSerialValue);
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
                android.R.layout.simple_spinner_item, stateList);
        // Specify the layout to use when the list of choices appears
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(stateAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                tState.setText(parent.getItemAtPosition(position).toString());
//                if (position == 0) tState.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

}
