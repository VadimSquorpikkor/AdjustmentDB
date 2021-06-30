package com.squorpikkor.app.adjustmentdb.ui.main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.squorpikkor.app.adjustmentdb.DEvent;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.Utils;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.StatesAdapter;
import java.util.ArrayList;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.BACK_PRESS_SEARCH;

public class UnitInfoFragment extends Fragment {

    private MainViewModel mViewModel;
    private int position;
    RecyclerView eventsRecycler;
    DUnit unit;

    private TextView tType;
    private TextView tName;
    private TextView tInnerSerial;
    private TextView tSerial;
    private TextView tId;
    private TextView tEmployee;
    private TextView tDaysPassed;
    private RecyclerView eventRecycler;

    public static UnitInfoFragment newInstance() {
        return new UnitInfoFragment();
    }

    public UnitInfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unit_info, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        eventsRecycler = view.findViewById(R.id.found_unit_recycler);

        mViewModel.getPosition().observe(getViewLifecycleOwner(), integer -> position = integer);
        mViewModel.getFoundUnitsList().observe(getViewLifecycleOwner(), this::updateFoundUnit);
        mViewModel.getUnitStatesList().observe(getViewLifecycleOwner(), this::updateAdapter);

        tType = view.findViewById(R.id.textViewType111);
        tName = view.findViewById(R.id.textViewName);
        tInnerSerial = view.findViewById(R.id.textViewInnerSerialValue);
        tSerial = view.findViewById(R.id.textViewSerialValue);
        tId = view.findViewById(R.id.textViewIdValue);
        tEmployee = view.findViewById(R.id.textViewEmployeeValue);
        tDaysPassed = view.findViewById(R.id.textDaysPassedValue);
        eventRecycler = view.findViewById(R.id.recyclerView);

        return view;
    }

    private void updateFoundUnit(ArrayList<DUnit> units) {
//        if (position>units.size()) return;
        unit = units.get(position);
        insertDataToFields(unit);
        mViewModel.getEventForThisUnit(unit.getEventId());
    }

    private void insertDataToFields(DUnit unit) {
        if (unit==null)return;
//        mViewModel.setBackPressCommand(BACK_PRESS_STATES);
        if (unit.isRepairUnit()) tType.setText("Ремонт");
        if (unit.isSerialUnit()) tType.setText("Серия");
        tId.setText(Utils.getRightValue(unit.getId()));
        tName.setText(Utils.getRightValue(unit.getName()));
        tInnerSerial.setText(Utils.getRightValue(unit.getInnerSerial()));
        tSerial.setText(Utils.getRightValue(unit.getSerial()));
        if (unit.getEmployee()!=null) tEmployee.setText(unit.getEmployee());
        tDaysPassed.setText(String.valueOf(unit.daysPassed()));

        mViewModel.addSelectedUnitStatesListListener(unit.getId());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mViewModel.setBackPressCommand(BACK_PRESS_SEARCH);
    }

    void updateAdapter(ArrayList<DEvent> list) {
        StatesAdapter statesAdapter = new StatesAdapter(list, mViewModel);
        eventRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventRecycler.setAdapter(statesAdapter);
    }
}
