package com.squorpikkor.app.adjustmentdb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.TextView;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.StatesAdapter;
import java.util.ArrayList;

public class UnitInfoActivity extends AppCompatActivity {

    //todo Для этой активити не нужен такая ViewModel, как основная, нужно сделать сильно урезанную версию

    MainViewModel mViewModel;
    TextView deviceType;
    TextView deviceName;
    TextView innerSerial;
    TextView serial;
    TextView id;
    TextView employee;
    RecyclerView events;
    public static final String EXTRA_UNIT_ID = "extra_unit_id";
    public static final String EXTRA_EVENT_ID = "extra_event_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_info);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        deviceType = findViewById(R.id.textViewType2);
        deviceName = findViewById(R.id.textViewName2);
        innerSerial = findViewById(R.id.textViewInnerSerialValue2);
        serial = findViewById(R.id.textViewSerialValue2);
        id = findViewById(R.id.textViewIdValue2);
        employee = findViewById(R.id.textViewEmployeeValue2);
        events = findViewById(R.id.recyclerView2);

        String unit_id = this.getIntent().getStringExtra(EXTRA_UNIT_ID);
        String event_id = this.getIntent().getStringExtra(EXTRA_EVENT_ID);
        mViewModel.addSelectedUnitStatesListListener(unit_id);

        mViewModel.selectUnit(unit_id, event_id);

        final MutableLiveData<DUnit> selectedUnits = mViewModel.getSelectedUnit();
        selectedUnits.observe(this, this::insertDataToFields);

        final MutableLiveData<ArrayList<DEvent>> unitEvents = mViewModel.getUnitStatesList();
        unitEvents.observe(this, this::updateAdapter);
    }

    void updateAdapter(ArrayList<DEvent> list) {
        StatesAdapter statesAdapter = new StatesAdapter(list, mViewModel);
        events.setLayoutManager(new LinearLayoutManager(this));
        events.setAdapter(statesAdapter);
    }

    private void insertDataToFields(DUnit unit) {
        if (unit.isRepairUnit()) deviceType.setText("Ремонт");
        if (unit.isSerialUnit()) deviceType.setText("Серия");
        id.setText(Utils.getRightValue(unit.getId()));
        deviceName.setText(Utils.getRightValue(unit.getName()));
        innerSerial.setText(Utils.getRightValue(unit.getInnerSerial()));
        serial.setText(Utils.getRightValue(unit.getSerial()));
    }
}