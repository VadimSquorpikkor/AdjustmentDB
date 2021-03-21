package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.DialogStatesAdapterNew;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class SelectStateDialog extends Dialog {

    private MainViewModel mViewModel;
    private Activity context;
    //private RecyclerView recyclerViewStates;
    private ArrayList<String> stateList;
    private TextView cancelButton;
    private TextView okButton;
    private ListView listViewState;
    DialogStatesAdapterNew sourceAdapter;
    EditText selectedEditState;
    DUnit unit;

    public SelectStateDialog(@NonNull Activity context, MainViewModel mViewModel, ArrayList<String> stateList) {
        super(context);
        this.context = context;
        this.mViewModel = mViewModel;
        this.stateList = stateList;
        ArrayList<DUnit> units = mViewModel.getSelectedUnits().getValue();
        if (units.size() != 0) this.unit = units.get(0);
        else this.unit = new DUnit();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_states);

        //recyclerViewStates = findViewById(R.id.recycler_states);
        cancelButton = findViewById(R.id.textViewInnerSerial);
        okButton = findViewById(R.id.textViewInnerSerialValue);
        selectedEditState = findViewById(R.id.selectedState);

        cancelButton.setOnClickListener(view -> {
            dismiss();
        });

        okButton.setOnClickListener(view -> {
            String id = unit.getId();
            String name = unit.getName();
            String innerSerial = unit.getInnerSerial();
            String serial = unit.getSerial();
            String state = selectedEditState.getText().toString();
            if (mViewModel.getIsRepair().getValue()) mViewModel.saveRepairUnitToDB(new DUnit(id, name, innerSerial, serial, state));
            else mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, state));
            dismiss();
        });


        Log.e(TAG, "*** stateList.size() = "+stateList.size());

        listViewState = findViewById(R.id.recycler_states);

        // создаем адаптер
        sourceAdapter = new DialogStatesAdapterNew(context,
                R.layout.dialog_state_item_new, this.stateList);
        listViewState.setAdapter(sourceAdapter);

        listViewState.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = stateList.get((int)id);
                Toast.makeText(context, selectedState, Toast.LENGTH_SHORT).show();
                selectedEditState.setText(selectedState);

//                view.findViewById(R.id.name).set

                /*for (int a = 0; a< parent.getChildCount();a++){
                    parent.getChildAt(a).setBackgroundColor(Color.TRANSPARENT);
                    view.refreshDrawableState();
                    if(parent.getChildAt(a) == view){
                        view.findViewById(R.id.state).setBackgroundColor(Color.RED);//setBackgroundColor(getResources().getColor(R.color.soft_opaque));
                        view.refreshDrawableState();
                    }
                }*/
            }
        });

//        DialogStatesAdapter dialogStatesAdapter = new DialogStatesAdapter(this.stateList);
//        recyclerViewStates.setLayoutManager(new LinearLayoutManager(context));
//        recyclerViewStates.setAdapter(dialogStatesAdapter);

    }


}
