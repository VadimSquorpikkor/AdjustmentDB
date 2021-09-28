package com.squorpikkor.app.adjustmentdb.ui.main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.Utils;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.StatesAdapter;
import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_SEARCH;
import static com.squorpikkor.app.adjustmentdb.Constant.LESS_THAN_ONE;

public class UnitInfoFragment extends Fragment {

    private MainViewModel mViewModel;
    private final DUnit unit;

    public static UnitInfoFragment newInstance(DUnit unit) {
        return new UnitInfoFragment(unit);
    }

    public UnitInfoFragment(DUnit unit) {
        this.unit = unit;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unit_info, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        TextView tType = view.findViewById(R.id.textViewType);
        TextView tName = view.findViewById(R.id.textViewName);
        TextView tInnerSerial = view.findViewById(R.id.textViewInnerSerialValue);
        TextView tSerial = view.findViewById(R.id.textViewSerialValue);
        TextView tId = view.findViewById(R.id.textViewIdValue);
        TextView tEmployee = view.findViewById(R.id.textViewEmployeeValue);
        TextView tDaysPassed = view.findViewById(R.id.textDaysPassedValue);

        RecyclerView eventRecycler = view.findViewById(R.id.recyclerView);
        StatesAdapter statesAdapter = new StatesAdapter(mViewModel);
        eventRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventRecycler.setAdapter(statesAdapter);

        ImageView isCompleteImage = view.findViewById(R.id.is_complete);
        isCompleteImage.setVisibility(View.GONE);

        mViewModel.getUnitStatesList().observe(getViewLifecycleOwner(), statesAdapter::setList);
        mViewModel.getEventsForThisUnit(unit.getEventId());

        if (unit.isRepairUnit()) tType.setText("Ремонт");
        if (unit.isSerialUnit()) tType.setText("Серия");
        tId.setText(Utils.getRightValue(unit.getId()));
        tName.setText(Utils.getRightValue(mViewModel.getDeviceNameById(unit.getName())));
        tInnerSerial.setText(Utils.getRightValue(unit.getInnerSerial()));
        tSerial.setText(Utils.getRightValue(unit.getSerial()));
        tEmployee.setText(Utils.getRightValue(mViewModel.getEmployeeNameById(unit.getEmployee())));
        if (unit.daysPassed()==0) tDaysPassed.setText(LESS_THAN_ONE);
        else tDaysPassed.setText(String.valueOf(unit.daysPassed()));
        if (unit.isComplete()) isCompleteImage.setVisibility(View.VISIBLE);
        else isCompleteImage.setVisibility(View.GONE);

        mViewModel.addSelectedUnitStatesListListener(unit.getId());

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mViewModel.setBackPressCommand(BACK_PRESS_SEARCH);
    }
}
