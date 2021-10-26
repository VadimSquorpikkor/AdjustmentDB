package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.squorpikkor.app.adjustmentdb.R;
import org.jetbrains.annotations.NotNull;

import static com.squorpikkor.app.adjustmentdb.Constant.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.Constant.SERIAL_TYPE;

@SuppressWarnings("FieldCanBeLocal")
public class SearchUnitParamsDialog extends BaseDialog {

    private RadioButton isSerialRadio;
    private RadioButton isRepairRadio;
    private EditText serialEdit;
    private SpinnerAdapter deviceSpinnerAdapter;
    private SpinnerAdapter locationSpinnerAdapter;
    private SpinnerAdapter stateSpinnerAdapter;
    private SpinnerAdapter employeeSpinnerAdapter;
    private SpinnerAdapter deviceSetSpinnerAdapter;

    //todo сделать список статусов зависимым от выбраной локации (подгружать в диалог статусы по локации). Для "пустой" локации подгружать все статусы
    public SearchUnitParamsDialog() {
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeWithVM(R.layout.dialog_search_unit_param);

        isSerialRadio = view.findViewById(R.id.radio_button_serial);
        isRepairRadio = view.findViewById(R.id.radio_button_repair);
        Spinner deviceSetSpinner = view.findViewById(R.id.spinnerDevSetName);
        Spinner devNameSpinner = view.findViewById(R.id.spinnerDevName);
        Spinner locationSpinner = view.findViewById(R.id.spinnerLocation);
        Spinner statesSpinner = view.findViewById(R.id.spinnerState);
        Spinner employeeSpinner = view.findViewById(R.id.spinnerEmployee);
        Button searchButton = view.findViewById(R.id.show_button);
        serialEdit = view.findViewById(R.id.editTextSerial);

        deviceSetSpinnerAdapter = new SpinnerAdapter(deviceSetSpinner, mContext);
        deviceSpinnerAdapter = new SpinnerAdapter(devNameSpinner, mContext);
        locationSpinnerAdapter = new SpinnerAdapter(locationSpinner, mContext);
        stateSpinnerAdapter = new SpinnerAdapter(statesSpinner, mContext);
        employeeSpinnerAdapter = new SpinnerAdapter(employeeSpinner, mContext);

        mViewModel.getDeviceSets().observe(this, deviceSetSpinnerAdapter::setDataWithEmpty);
        mViewModel.getDevices().observe(this, list1 -> deviceSpinnerAdapter.setDataByDevSet(list1, deviceSetSpinnerAdapter.getSelectedNameId()));
        mViewModel.getLocations().observe(this, locationSpinnerAdapter::setData);
        mViewModel.getStates().observe(this, list -> stateSpinnerAdapter.setDataByTypeAndLocation(list, getSelectedType(), locationSpinnerAdapter.getSelectedNameId()));
        mViewModel.getEmployees().observe(this, employeeSpinnerAdapter::setData);

        isRepairRadio.setOnClickListener(v -> updateStateSpinner());
        isSerialRadio.setOnClickListener(v -> updateStateSpinner());

        //todo сделать частью SpinnerAdapter
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {updateStateSpinner();}
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        deviceSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {updateDeviceSpinner();}
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        searchButton.setOnClickListener(v -> startSearch());
        return dialog;
    }

    private void startSearch() {
        String nameId = deviceSpinnerAdapter.getSelectedNameId();
        String locationId = locationSpinnerAdapter.getSelectedNameId();
        String employeeId = employeeSpinnerAdapter.getSelectedNameId();
        String typeId = getSelectedType();
        String stateId = stateSpinnerAdapter.getSelectedNameId();
        String serial = serialEdit.getText().toString();
        String devSet = deviceSetSpinnerAdapter.getSelectedNameId();

        mViewModel.startSearch(nameId, locationId, employeeId, typeId, stateId, devSet, serial);
        dismiss();
    }

    private String getSelectedType() {
        return isSerialRadio.isChecked()?SERIAL_TYPE:REPAIR_TYPE;
    }

    private void updateStateSpinner() {
        stateSpinnerAdapter.setDataByTypeAndLocation(mViewModel.getStates().getValue(), getSelectedType(), locationSpinnerAdapter.getSelectedNameId());
    }

    private void updateDeviceSpinner() {
        deviceSpinnerAdapter.setDataByDevSet(mViewModel.getDevices().getValue(), deviceSetSpinnerAdapter.getSelectedNameId());
    }
}
