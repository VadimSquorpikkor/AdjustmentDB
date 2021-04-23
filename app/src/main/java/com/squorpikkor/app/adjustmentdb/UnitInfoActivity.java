package com.squorpikkor.app.adjustmentdb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.StatesAdapter;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.Utils.insertRightValue;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.BACK_PRESS_STATES;

public class UnitInfoActivity extends AppCompatActivity {

    MainViewModel mViewModel;
    TextView deviceType;
    TextView deviceName;
    TextView innerSerial;
    TextView serial;
    TextView location;
    TextView id;
    TextView employee;
    RecyclerView events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_info);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        deviceType = findViewById(R.id.textViewType2);
        deviceName = findViewById(R.id.textViewName2);
        innerSerial = findViewById(R.id.textViewInnerSerialValue2);
        serial = findViewById(R.id.textViewSerialValue2);
        location = findViewById(R.id.textLocationValue2);
        id = findViewById(R.id.textViewIdValue2);
        employee = findViewById(R.id.textViewEmployeeValue2);
        events = findViewById(R.id.recyclerView2);

        int position = mViewModel.getPosition();
        mViewModel.selectUnit(mViewModel.getSerialUnitsList().getValue().get(position));

        final MutableLiveData<DUnit> selectedUnits = mViewModel.getSelectedUnit();
        selectedUnits.observe(this, this::insertDataToFields);

        final MutableLiveData<ArrayList<DEvent>> unitEvents = mViewModel.getUnitStatesList();
        unitEvents.observe(this, this::updateAdapter);
    }

    void updateAdapter(ArrayList<DEvent> list) {
        StatesAdapter statesAdapter = new StatesAdapter(list);
        events.setLayoutManager(new LinearLayoutManager(this));
        events.setAdapter(statesAdapter);
    }

    private void insertDataToFields(DUnit unit) {
        if (unit.isRepairUnit()) deviceType.setText("Ремонт");
        if (unit.isSerialUnit()) deviceType.setText("Серия");
        id.setText(insertRightValue(unit.getId()));
        deviceName.setText(insertRightValue(unit.getName()));
        innerSerial.setText(insertRightValue(unit.getInnerSerial()));
        serial.setText(insertRightValue(unit.getSerial()));
        //location.setText(location);

        mViewModel.addSelectedUnitStatesListListener(unit);
    }
}