package com.squorpikkor.app.adjustmentdb.ui.main.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.UnitAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.ExitAskDialog;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.SearchUnitParamsDialog;
import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.BACK_PRESS_INFO_FRAGMENT;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.BACK_PRESS_SEARCH;

public class SearchDeviceFragment extends Fragment {

    private MainViewModel mViewModel;

    public static SearchDeviceFragment newInstance() {
        return new SearchDeviceFragment();
    }

    RecyclerView foundUnitRecycler;
    ImageView logoImage;
    FloatingActionButton openSearchDialogButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_device, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        foundUnitRecycler = view.findViewById(R.id.found_unit_recycler);
        logoImage = view.findViewById(R.id.logo_image);

        mViewModel.getStartExit().observe(getViewLifecycleOwner(), this::exitDialog);
        mViewModel.getFoundUnitsList().observe(getViewLifecycleOwner(), this::updateFoundRecycler);
        mViewModel.getCanWork().observe(getViewLifecycleOwner(), this::setButtonVisibility);

        openSearchDialogButton = view.findViewById(R.id.open_search);
        openSearchDialogButton.setOnClickListener(v -> openSearchDialog());

        mViewModel.setBackPressCommand(BACK_PRESS_SEARCH);
        return view;
    }

    /**Если работа запрещена (например пользователь не зарегистрирован), то кнопка поиска скрывается*/
    private void setButtonVisibility(Boolean show) {
        if(show)openSearchDialogButton.setVisibility(View.VISIBLE);
        else openSearchDialogButton.setVisibility(View.GONE);
    }

    private void openSearchDialog() {
        SearchUnitParamsDialog dialog = new SearchUnitParamsDialog();
        dialog.show(getParentFragmentManager(), null);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("TAG", "onResume: "+this);
        mViewModel.setBackPressCommand(BACK_PRESS_SEARCH);
    }

    void exitDialog(boolean state) {
        if (state) {
            ExitAskDialog dialog = new ExitAskDialog(requireActivity());
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

    private void openInfoFragment(DUnit unit) {
        mViewModel.setBackPressCommand(BACK_PRESS_INFO_FRAGMENT);
        //mViewModel.getPosition().setValue(position);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.child_fragment_container_3, UnitInfoFragment.newInstance(unit))
                .addToBackStack(null)
                .commit();
    }

}
