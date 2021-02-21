package com.squorpikkor.app.adjustmentdb.ui.main;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.QuickContactBadge;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;

public class MainFragment extends Fragment {

    private MainViewModel mViewModel;
    EditText tName;
    EditText tSerial;
    Button bSend;


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
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        //mViewModel.saveDUnitToDB(new DUnit("БДКГ-02", "123.002"));

    }



}