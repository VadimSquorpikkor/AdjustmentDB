package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.squorpikkor.app.adjustmentdb.DEvent;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EMPTY_VALUE_TEXT;

public class SelectStateDialogMulti extends BaseDialog {
    private EditText descriptionEdit;
    private String location;

    private SpinnerAdapter stateSpinnerAdapter;
    private SpinnerAdapter deviceSpinnerAdapter;

    public SelectStateDialogMulti() {
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeWithVM(R.layout.dialog_select_states_multi);

        location = mViewModel.getLocation_id().getValue();

        ArrayList<DUnit> unitList = mViewModel.getScannerFoundUnitsList().getValue();
        DUnit unit = unitList != null ? unitList.get(0) : null;//todo переименовать -> firstUnit

        Spinner stateSpinner = view.findViewById(R.id.state_spinner);
        Spinner deviceSpinner = view.findViewById(R.id.name_spinner);

        deviceSpinnerAdapter = new SpinnerAdapter(deviceSpinner, mContext);
        stateSpinnerAdapter = new SpinnerAdapter(stateSpinner, mContext);

        mViewModel.getDevices().observe(this, list -> deviceSpinnerAdapter.setData(list, EMPTY_VALUE_TEXT));
        mViewModel.getStates().observe(this, list -> stateSpinnerAdapter.setDataByTypeAndLocation(list, unit.getType(), location, EMPTY_VALUE_TEXT));

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button okButton = view.findViewById(R.id.ok_button);
        descriptionEdit = view.findViewById(R.id.description);

        cancelButton.setOnClickListener(view -> dismiss());

        okButton.setOnClickListener(view -> {
            for (int i = 0; i < unitList.size(); i++) {
                DUnit dUnit = unitList.get(i);
                mViewModel.closeEvent(dUnit.getEventId());
                DEvent newEvent = getNewEvent(dUnit);
                updateUnitData(dUnit, newEvent);
                mViewModel.saveUnitAndEvent(dUnit, newEvent);
            }
            dismiss();
        });

        return dialog;
    }

    private void updateUnitData(DUnit unit, DEvent newEvent) {
        String eventId = newEvent==null?null:newEvent.getId();
        String newNameId = deviceSpinnerAdapter.getSelectedNameId();
        String newStateId = stateSpinnerAdapter.getSelectedNameId();//todo потом этого не будет

        if (unit.getName().equals("") && !newNameId.equals(ANY_VALUE)) unit.setName(newNameId);
        if (unit.getDate()==null) unit.setDate(new Date());
        if (unit.getState().equals("") && !newStateId.equals(ANY_VALUE)) unit.setState(newStateId);
        if (eventId!=null&&!eventId.equals("")) unit.setEventId(eventId);
    }

    private DEvent getNewEvent(DUnit unit) {
        //Если в спиннере статуса стоит "-не выбрано-", то значит нового события не будет, тогда возвращаем null
        String stateId = stateSpinnerAdapter.getSelectedNameId();
        String description = descriptionEdit.getText().toString();
        String eventId = unit.getId()+"_"+new Date().getTime();

        if (stateId.equals(ANY_VALUE)) return null;
        else return new DEvent(new Date(), stateId, description, location, unit.getId(), eventId);
    }
}
