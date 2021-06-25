package com.squorpikkor.app.adjustmentdb.ui.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.UnitInfoActivity;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.UnitAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.ExitAskDialog;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.RecognizeDialog;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.SearchUnitParamsDialog;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Location;

import java.util.ArrayList;
import java.util.Objects;

import static com.squorpikkor.app.adjustmentdb.UnitInfoActivity.EXTRA_UNIT_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.BACK_PRESS_SEARCH;

public class SearchDeviceFragment extends Fragment {

    private MainViewModel mViewModel;

    public static SearchDeviceFragment newInstance() {
        return new SearchDeviceFragment();
    }

    RecyclerView foundUnitRecycler;
    ImageView logoImage;
    FloatingActionButton openSearchDialogButton;
//    FloatingActionButton recognizeButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_device, container, false);
        mViewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(MainViewModel.class);

        foundUnitRecycler = view.findViewById(R.id.found_unit_recycler);
        logoImage = view.findViewById(R.id.logo_image);

//        final MutableLiveData<ArrayList<DUnit>> units = mViewModel.getSerialUnitsList();
//        units.observe(getActivity(), this::updateFoundRecycler);



        final MutableLiveData<Boolean> doExit = mViewModel.getStartExit();
        doExit.observe(getViewLifecycleOwner(), this::exitDialog);





        //------------------------

//        //todo проверка
//        mViewModel.getLocations().observe(this, locations -> {
//            for (Location location:locations) {
//                Log.e("TAG", "onCreateView: "+location.getId()+" "+location.getNameId()+" "+location.getName());
//            }
//        });
//        //todo конец проверки


        mViewModel.getFoundUnitsList().observe(getViewLifecycleOwner(), this::updateFoundRecycler);


        //------------------------





        openSearchDialogButton = view.findViewById(R.id.open_search);
        openSearchDialogButton.setOnClickListener(v -> openSearchDialog());

//        recognizeButton = view.findViewById(R.id.floatingActionButton);
//        recognizeButton.setOnClickListener(v -> openRecognize());

        return view;
    }

    private void openSearchDialog() {
        SearchUnitParamsDialog dialog = new SearchUnitParamsDialog();
        dialog.show(requireFragmentManager(), null);
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

    private void updateFoundRecycler(ArrayList<DUnit> list) {
        if (list.size() == 0) {
            logoImage.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), getString(R.string.nothing_found), Toast.LENGTH_SHORT).show();
        } else {
            logoImage.setVisibility(View.GONE);
        }
        UnitAdapter unitAdapter = new UnitAdapter(list, mViewModel);
        unitAdapter.setOnItemClickListener(SearchDeviceFragment.this::openInfoFragment);
        foundUnitRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        foundUnitRecycler.setAdapter(unitAdapter);
    }

    private void openInfoFragment(int position) {
        if (mViewModel.getFoundUnitsList().getValue() != null) {
            Intent intent = new Intent(getActivity(), UnitInfoActivity.class);
            intent.putExtra(EXTRA_UNIT_ID, mViewModel.getFoundUnitsList().getValue().get(position).getId());
            startActivity(intent);
        }
    }

//    private void openRecognize() {
//        RecognizeDialog dialog = new RecognizeDialog();
//        dialog.show(requireFragmentManager(), null);
//    }

}
