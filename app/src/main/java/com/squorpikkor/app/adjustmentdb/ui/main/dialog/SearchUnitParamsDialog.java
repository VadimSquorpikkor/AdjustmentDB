package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.squorpikkor.app.adjustmentdb.MainActivity;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE_TEXT;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.SERIAL_TYPE;

public class SearchUnitParamsDialog extends Dialog {

    private final Activity context;
    MainViewModel mViewModel;

    RadioButton isSerialRadio;
    RadioButton isRepairRadio;
    Spinner devNameSpinner;
    Spinner locationSpinner;
    Spinner statesSpinner;
    Spinner employeeSpinner;
    Button searchButton;
    EditText serialEdit;

    public SearchUnitParamsDialog(@NonNull Activity context, MainViewModel mViewModel) {
        super(context);
        this.context = context;
        this.mViewModel = mViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_search_unit_param);

        MainViewModel mViewModel = new ViewModelProvider((MainActivity) context).get(MainViewModel.class);

        isSerialRadio = findViewById(R.id.radio_button_serial);
        isRepairRadio = findViewById(R.id.radio_button_repair);
        devNameSpinner = findViewById(R.id.spinnerDevName);
        locationSpinner = findViewById(R.id.spinnerLocation);
        statesSpinner = findViewById(R.id.spinnerState);
        employeeSpinner = findViewById(R.id.spinnerEmployee);
        searchButton = findViewById(R.id.show_button);
        serialEdit = findViewById(R.id.editTextSerial);

        final MutableLiveData<ArrayList<String>> types = mViewModel.getDeviceNameList();
        types.observe((LifecycleOwner)context, this::updateDevNamesSpinner);

        final MutableLiveData<ArrayList<String>> locations = mViewModel.getLocationNamesList();
        locations.observe((LifecycleOwner)context, this::updateLocationSpinner);

        final MutableLiveData<ArrayList<String>> employees = mViewModel.getEmployeeNamesList();
        employees.observe((LifecycleOwner)context, this::updateEmployeeSpinner);

        //todo сделать список статусов зависимым от выбраной локации (подгружать в диалог статусы ро локации). Для "пустой" локации подгружать все статусы
        final MutableLiveData<ArrayList<String>> states = mViewModel.getAllStatesNameList();
        states.observe((LifecycleOwner)context, this::updateStatesSpinner);

        searchButton.setOnClickListener(v -> startSearch());
    }

    private void startSearch() {
        String deviceName = devNameSpinner.getSelectedItem().toString();
        String location = locationSpinner.getSelectedItem().toString();
        String state = statesSpinner.getSelectedItem().toString();
        String employee = employeeSpinner.getSelectedItem().toString();
        String serial = serialEdit.getText().toString();
        if (deviceName.equals(ANY_VALUE_TEXT)) deviceName = ANY_VALUE;
        if (location.equals(ANY_VALUE_TEXT)) location = ANY_VALUE;
        if (state.equals(ANY_VALUE_TEXT)) state = ANY_VALUE;
        if (employee.equals(ANY_VALUE_TEXT)) employee = ANY_VALUE;
        if (serial.equals("")) serial = ANY_VALUE;
        String type = isSerialRadio.isChecked()?SERIAL_TYPE:REPAIR_TYPE;
        mViewModel.getUnitListFromBD(deviceName, location, employee, type, state, serial);
        dismiss();
    }

    private void updateDevNamesSpinner(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>(list);
        newList.add(0, ANY_VALUE_TEXT);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, newList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devNameSpinner.setAdapter(typeAdapter);
    }

    private void updateLocationSpinner(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>(list);
        newList.add(0, ANY_VALUE_TEXT);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, newList);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
    }

    private void updateEmployeeSpinner(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>(list);
        newList.add(0, ANY_VALUE_TEXT);
        ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, newList);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employeeSpinner.setAdapter(employeeAdapter);
    }

    private void updateStatesSpinner(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>(list);
        newList.add(0, ANY_VALUE_TEXT);
        ArrayAdapter<String> statesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, newList);
        statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statesSpinner.setAdapter(statesAdapter);
    }
}
