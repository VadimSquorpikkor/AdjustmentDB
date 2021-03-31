package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.DialogStatesAdapter;
import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class SelectStateDialogNew extends Dialog {
    private MainViewModel mViewModel;
    private Activity context;
    private ArrayList<String> stateList;
    private TextView cancelButton;
    private TextView okButton;
    private EditText description;
    private DUnit unit;
    private Spinner spinner;

    public SelectStateDialogNew(@NonNull Activity context, MainViewModel mViewModel, ArrayList<String> stateList) {
        super(context);
        this.context = context;
        this.mViewModel = mViewModel;
        this.stateList = stateList;
        ArrayList<DUnit> units = this.mViewModel.getSelectedUnits().getValue();
        if (units.size() != 0) this.unit = units.get(0);
        else this.unit = new DUnit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_states_new);

        cancelButton = findViewById(R.id.textViewInnerSerial);
        okButton = findViewById(R.id.textViewInnerSerialValue);
        description = findViewById(R.id.description);
        spinner = (Spinner) findViewById(R.id.state_spinner);

        cancelButton.setOnClickListener(view -> {
            dismiss();
        });

        okButton.setOnClickListener(view -> {
            String id = unit.getId();
            String name = unit.getName();
            String innerSerial = unit.getInnerSerial();
            String serial = unit.getSerial();
            String state = spinner.getSelectedItem().toString();//selectedEditState.getText().toString();
            String desc = description.getText().toString();
            String type = unit.getType();
            String location = mViewModel.getSelectedProfile().getLocation();
            Log.e(TAG, "♦♦♦ Dialog onCreate: " + location);
            mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, state, desc, type, location));
            dismiss();
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item,  stateList);
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
