package com.squorpikkor.app.adjustmentdb.ui.main;

import androidx.fragment.app.FragmentTransaction;
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
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class MainFragment extends Fragment {

    RecyclerView recyclerViewSerialUnits;
    RecyclerView recyclerViewRepairUnits;
    ArrayList<DUnit> serialUnits;
    ArrayList<DUnit> repairUnits;

    TabLayout tabLayout;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        recyclerViewSerialUnits = view.findViewById(R.id.recycler_serial_units);
        recyclerViewRepairUnits = view.findViewById(R.id.recycler_repair_units);
        MainViewModel mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        serialUnits = new ArrayList<>();
        repairUnits = new ArrayList<>();

        final MutableLiveData<ArrayList<DUnit>> serialUnits = mViewModel.getSerialUnitsList();
        serialUnits.observe(getViewLifecycleOwner(), s -> {
            this.serialUnits = serialUnits.getValue();
            if (serialUnits.getValue() != null) Log.e(TAG, "onCreateView: " + serialUnits.getValue().size());
            DSerialUnitAdapter unitsAdapter = new DSerialUnitAdapter(this.serialUnits);
            recyclerViewSerialUnits.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerViewSerialUnits.setAdapter(unitsAdapter);
        });

        final MutableLiveData<ArrayList<DUnit>> repairUnits = mViewModel.getRepairUnitsList();
        repairUnits.observe(getViewLifecycleOwner(), s -> {
            this.repairUnits = repairUnits.getValue();
            if (repairUnits.getValue() != null) Log.e(TAG, "onCreateView: " + repairUnits.getValue().size());
            DRepairUnitAdapter unitsAdapter = new DRepairUnitAdapter(this.repairUnits);
            recyclerViewRepairUnits.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerViewRepairUnits.setAdapter(unitsAdapter);
        });

        /**Открываем фрагмент со сканером QR-кода и кнопкой добавления в БД*/
        view.findViewById(R.id.floatingActionButton).setOnClickListener(v -> {
            // Create new fragment and transaction
            Fragment newFragment = ScannerFragment.newInstance();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.container, newFragment);
            transaction.addToBackStack(null);
            // Commit the transaction
            transaction.commit();
        });

        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                               @Override
                                               public void onTabSelected(TabLayout.Tab tab) {
                                                   switch(tab.getPosition()) {
                                                       case 0:selectSerial();break;
                                                       case 1:selectRepair();break;
                                                   }
                                               }

                                               @Override
                                               public void onTabUnselected(TabLayout.Tab tab) {

                                               }

                                               @Override
                                               public void onTabReselected(TabLayout.Tab tab) {

                                               }
                                           });

        recyclerViewRepairUnits.setVisibility(View.GONE);

        ((TextView)view.findViewById(R.id.versionText)).setText(mViewModel.getVersion());

        return view;
    }


    private void selectSerial() {
        Log.e(TAG, "onTabSelected: 0");
        recyclerViewSerialUnits.setVisibility(View.VISIBLE);
        recyclerViewRepairUnits.setVisibility(View.GONE);

    }

    private void selectRepair() {
        Log.e(TAG, "onTabSelected: 1");
        recyclerViewSerialUnits.setVisibility(View.GONE);
        recyclerViewRepairUnits.setVisibility(View.VISIBLE);
    }
}