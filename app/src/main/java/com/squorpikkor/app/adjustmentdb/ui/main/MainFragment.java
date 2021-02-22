package com.squorpikkor.app.adjustmentdb.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class MainFragment extends Fragment {

    private MainViewModel mViewModel;
    EditText tName;
    EditText tSerial;
    RecyclerView recyclerViewUnits;
    ArrayList<DUnit> units;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        tName = view.findViewById(R.id.editTextName);
        tSerial = view.findViewById(R.id.editTextSerial);
        view.findViewById(R.id.buttonAddToBD).setOnClickListener(view1 -> {
            String name = tName.getText().toString();
            String serial = tSerial.getText().toString();
            mViewModel.saveDUnitToDB(new DUnit(name, serial));
        });

        recyclerViewUnits = view.findViewById(R.id.recycler_units);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        units = new ArrayList<>();
        units.add(new DUnit("q", "123"));

        final MutableLiveData<ArrayList<DUnit>> units = mViewModel.getUnitsList();
        units.observe(getViewLifecycleOwner(), s -> {
            this.units = units.getValue();
            Log.e(TAG, "onCreateView: "+units.getValue().size());
            DUnitAdapter unitsAdapter = new DUnitAdapter(this.units);
            recyclerViewUnits.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerViewUnits.setAdapter(unitsAdapter);
        });

        return view;
    }

}