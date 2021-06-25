package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.squorpikkor.app.adjustmentdb.R;
import org.jetbrains.annotations.NotNull;

import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.SERIAL_TYPE;

public class SearchUnitParamsDialog extends BaseDialog {

    private RadioButton isSerialRadio;
    private EditText serialEdit;
    private SpinnerAdapter deviceSpinnerAdapter;
    private SpinnerAdapter locationSpinnerAdapter;
    private SpinnerAdapter stateSpinnerAdapter;
    private SpinnerAdapter employeeSpinnerAdapter;

    //todo сделать список статусов зависимым от выбраной локации (подгружать в диалог статусы по локации). Для "пустой" локации подгружать все статусы
    public SearchUnitParamsDialog() {
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeWithVM(R.layout.dialog_search_unit_param);

        isSerialRadio = view.findViewById(R.id.radio_button_serial);
        Spinner devNameSpinner = view.findViewById(R.id.spinnerDevName);
        Spinner locationSpinner = view.findViewById(R.id.spinnerLocation);
        Spinner statesSpinner = view.findViewById(R.id.spinnerState);
        Spinner employeeSpinner = view.findViewById(R.id.spinnerEmployee);
        Button searchButton = view.findViewById(R.id.show_button);
        serialEdit = view.findViewById(R.id.editTextSerial);

        deviceSpinnerAdapter = new SpinnerAdapter(devNameSpinner, mContext);
        locationSpinnerAdapter = new SpinnerAdapter(locationSpinner, mContext);
        stateSpinnerAdapter = new SpinnerAdapter(statesSpinner, mContext);
        employeeSpinnerAdapter = new SpinnerAdapter(employeeSpinner, mContext);

        mViewModel.getDevices().observe(this, deviceSpinnerAdapter::setData);
        mViewModel.getLocations().observe(this, locationSpinnerAdapter::setData);
        mViewModel.getStates().observe(this, stateSpinnerAdapter::setData);
        mViewModel.getEmployees().observe(this, employeeSpinnerAdapter::setData);

        searchButton.setOnClickListener(v -> startSearch());
        return dialog;
    }

    private void startSearch() {
        String nameId = deviceSpinnerAdapter.getSelectedNameId();
        String locationId = locationSpinnerAdapter.getSelectedNameId();
        String employeeId = employeeSpinnerAdapter.getSelectedNameId();
        String typeId = isSerialRadio.isChecked()?SERIAL_TYPE:REPAIR_TYPE;
        String stateId = stateSpinnerAdapter.getSelectedNameId();
        String serial = serialEdit.getText().toString();

        mViewModel.getUnitListFromBD(nameId, locationId, employeeId, typeId, stateId, serial);
        dismiss();
    }
}
