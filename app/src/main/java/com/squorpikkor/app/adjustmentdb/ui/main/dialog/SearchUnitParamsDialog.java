package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.squorpikkor.app.adjustmentdb.MainActivity;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE_TEXT;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.SERIAL_TYPE;

public class SearchUnitParamsDialog extends DialogFragment {

    Context mContext;
    MainViewModel mViewModel;
    RadioButton isSerialRadio;
    RadioButton isRepairRadio;
    Spinner devNameSpinner;
    Spinner locationSpinner;
    Spinner statesSpinner;
    Spinner employeeSpinner;
    Button searchButton;
    EditText serialEdit;

//    public static SearchUnitParamsDialog newInstance() {
//        return new SearchUnitParamsDialog();
//    }

    public SearchUnitParamsDialog() {
    }

    @Override
    public void onAttach(@NotNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog dialog = new AlertDialog.Builder(mContext).create();
        Window window = dialog.getWindow();
        if (window != null) window.setBackgroundDrawableResource(R.drawable.main_gradient);
        View view = requireActivity().getLayoutInflater()
                .inflate(R.layout.dialog_search_unit_param, null);
        dialog.setView(view, 0, 0, 0, 0);

        MainViewModel mViewModel = new ViewModelProvider((MainActivity) mContext).get(MainViewModel.class);

        isSerialRadio = view.findViewById(R.id.radio_button_serial);
        isRepairRadio = view.findViewById(R.id.radio_button_repair);
        devNameSpinner = view.findViewById(R.id.spinnerDevName);
        locationSpinner = view.findViewById(R.id.spinnerLocation);
        statesSpinner = view.findViewById(R.id.spinnerState);
        employeeSpinner = view.findViewById(R.id.spinnerEmployee);
        searchButton = view.findViewById(R.id.show_button);
        serialEdit = view.findViewById(R.id.editTextSerial);

        final MutableLiveData<ArrayList<String>> types = mViewModel.getDeviceNameList();
        types.observe((LifecycleOwner)mContext, this::updateDevNamesSpinner);

        final MutableLiveData<ArrayList<String>> locations = mViewModel.getLocationNamesList();
        locations.observe((LifecycleOwner)mContext, this::updateLocationSpinner);

        final MutableLiveData<ArrayList<String>> employees = mViewModel.getEmployeeNamesList();
        employees.observe((LifecycleOwner)mContext, this::updateEmployeeSpinner);

        //todo сделать список статусов зависимым от выбраной локации (подгружать в диалог статусы ро локации). Для "пустой" локации подгружать все статусы
        final MutableLiveData<ArrayList<String>> states = mViewModel.getAllStatesNameList();
        states.observe((LifecycleOwner)mContext, this::updateStatesSpinner);

        searchButton.setOnClickListener(v -> startSearch());
        return dialog;
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
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, newList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devNameSpinner.setAdapter(typeAdapter);
    }

    private void updateLocationSpinner(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>(list);
        newList.add(0, ANY_VALUE_TEXT);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, newList);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
    }

    private void updateEmployeeSpinner(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>(list);
        newList.add(0, ANY_VALUE_TEXT);
        ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, newList);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employeeSpinner.setAdapter(employeeAdapter);
    }

    private void updateStatesSpinner(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>(list);
        newList.add(0, ANY_VALUE_TEXT);
        ArrayAdapter<String> statesAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, newList);
        statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statesSpinner.setAdapter(statesAdapter);
    }
}
