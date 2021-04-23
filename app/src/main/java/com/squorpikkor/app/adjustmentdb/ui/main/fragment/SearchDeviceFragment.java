package com.squorpikkor.app.adjustmentdb.ui.main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.DSerialUnitAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.ExitAskDialog;

import java.util.ArrayList;
import java.util.Objects;

import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE_TEXT;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.BACK_PRESS_SEARCH;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.SERIAL_TYPE;

public class SearchDeviceFragment extends Fragment {

    private MainViewModel mViewModel;

    public static SearchDeviceFragment newInstance() {
        return new SearchDeviceFragment();
    }

    RadioButton isSerialRadio;
    RadioButton isRepairRadio;
    Spinner devNameSpinner;
    Spinner locationSpinner;
    Spinner employeeSpinner;
    Button searchButton;
    RecyclerView foundUnitRecycler;
    ImageView logoImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_device, container, false);
        mViewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(MainViewModel.class);

        isSerialRadio = view.findViewById(R.id.radio_button_serial);
        isRepairRadio = view.findViewById(R.id.radio_button_repair);
        devNameSpinner = view.findViewById(R.id.spinnerDevName);
        locationSpinner = view.findViewById(R.id.spinnerLocation);
        employeeSpinner = view.findViewById(R.id.spinnerEmployee);
        searchButton = view.findViewById(R.id.show_button);
        foundUnitRecycler = view.findViewById(R.id.found_unit_recycler);
        logoImage = view.findViewById(R.id.logo_image);

        final MutableLiveData<ArrayList<String>> types = mViewModel.getDeviceNameList();
        types.observe(getActivity(), this::updateTypeSpinner);

        final MutableLiveData<ArrayList<String>> locations = mViewModel.getLocationNamesList();
        locations.observe(getActivity(), this::updateLocationSpinner);

        final MutableLiveData<ArrayList<String>> employees = mViewModel.getEmployeeNamesList();
        employees.observe(getActivity(), this::updateEmployeeSpinner);

        final MutableLiveData<ArrayList<DUnit>> units = mViewModel.getSerialUnitsList();
        units.observe(getActivity(), this::updateFoundRecycler);

        final MutableLiveData<Boolean> doExit = mViewModel.getStartExit();
        doExit.observe(this, this::exitDialog);

        searchButton.setOnClickListener(v -> startSearch());

        return view;
    }

    private void startSearch() {
        String deviceName = devNameSpinner.getSelectedItem().toString();
        String location = locationSpinner.getSelectedItem().toString();
        String employee = employeeSpinner.getSelectedItem().toString();
        if (deviceName.equals(ANY_VALUE_TEXT)) deviceName = ANY_VALUE;
        if (location.equals(ANY_VALUE_TEXT)) location = ANY_VALUE;
        if (employee.equals(ANY_VALUE_TEXT)) employee = ANY_VALUE;
        String type = isSerialRadio.isChecked()?SERIAL_TYPE:REPAIR_TYPE;
        mViewModel.getUnitListFromBD(deviceName, location, employee, type);
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.setBackPressCommand(BACK_PRESS_SEARCH);
    }

    void exitDialog(boolean state) {
        if (state) {
            ExitAskDialog dialog = new ExitAskDialog(Objects.requireNonNull(getActivity()));
            dialog.show();
        }
    }

    private void updateTypeSpinner(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>(list);
        newList.add(0, ANY_VALUE_TEXT);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, newList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devNameSpinner.setAdapter(typeAdapter);
    }

    private void updateLocationSpinner(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>(list);
        newList.add(0, ANY_VALUE_TEXT);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, newList);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
    }

    private void updateEmployeeSpinner(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>(list);
        newList.add(0, ANY_VALUE_TEXT);
        ArrayAdapter<String> employeeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, newList);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employeeSpinner.setAdapter(employeeAdapter);
    }

    private void updateFoundRecycler(ArrayList<DUnit> list) {
        if (list.size() == 0) {
            logoImage.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), getString(R.string.nothing_found), Toast.LENGTH_SHORT).show();
        } else {
            logoImage.setVisibility(View.GONE);
        }
        DSerialUnitAdapter unitAdapter = new DSerialUnitAdapter(list, mViewModel);
        foundUnitRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        foundUnitRecycler.setAdapter(unitAdapter);
    }
}
