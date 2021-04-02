package com.squorpikkor.app.adjustmentdb.ui.main;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
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

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.DRepairUnitAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.DSerialUnitAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.scanner.ScannerFragmentNew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EMPTY_PROFILE_NAME;

public class MainFragment extends Fragment {

    //todo ВАЖНО!!! Убрать загрузку ВСЕХ серийных и ремонтных юнитов. Это только лишний расход
    // операций на чтение (на Firebase ограничение 50К/день). Сделать поиск по параметрам. Если
    // нужно будет найти устройство, пользователь воспользуется фильтром, если пользователю нужно
    // просто отсканировать код, то ему список устройств вобще не нужен

    //todo возможность добавлять НОВЫЕ устройства без добавления статуса? НЕ НУЖНО? Даже у новых
    // устройств всегда должен быть статус с датой, когда они пришли в работу ("Принят в ремонт")

    private static final int RC_SIGN_IN = 1;
    RecyclerView recyclerViewSerialUnits;
    RecyclerView recyclerViewRepairUnits;
    ArrayList<DUnit> serialUnits;
    ArrayList<DUnit> repairUnits;
    MainViewModel mViewModel;
    TabLayout tabLayout;
    TextView textEmail;
    TextView textLocation;

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
        textEmail = view.findViewById(R.id.email);
        textLocation = view.findViewById(R.id.location);
        mViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

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

        final MutableLiveData<String> profileName = mViewModel.getProfileName();
        profileName.observe(getViewLifecycleOwner(), s -> {
            if (profileName.getValue() != null) {
                if (profileName.getValue().equals(EMPTY_PROFILE_NAME)) Log.e(TAG, "♦♦♦ EMPTY_PROFILE_NAME");//todo может и без иф, просто присваивать. А может и с иф
                else Log.e(TAG, "♦♦♦ profileName = "+profileName.getValue());

                mViewModel.setSelectedProfile(profileName.getValue());
            }

        });

        final MutableLiveData<String> locationName = mViewModel.getLocationName();
        locationName.observe(getViewLifecycleOwner(), s -> {
            if (locationName.getValue() != null) {
                textLocation.setText(locationName.getValue());
            }
        });


        /**Открываем фрагмент со сканером QR-кода и кнопкой добавления в БД*/
        /*view.findViewById(R.id.floatingActionButton).setOnClickListener(v -> {
            // Create new fragment and transaction
            Fragment newFragment = ScannerFragment.newInstance();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.container, newFragment);
            transaction.addToBackStack(null);
            // Commit the transaction
            transaction.commit();
        });*/

        /**Открываем фрагмент со сканером QR-кода и кнопкой добавления в БД*/
        view.findViewById(R.id.floatingActionButton2).setOnClickListener(v -> {

            Log.e(TAG, "♠♠ !!!createView: "+mViewModel.getRepairStatesList().getValue().size());


            // Create new fragment and transaction
            Fragment newFragment = ScannerFragmentNew.newInstance();
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

        signIn();//todo перенести в mainActivity

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

    //todo зачем нужна такая аутентификация? просто проверять по адресу аккаунту через
    // getProfileByEMail, пользователь не зарегистрированный в системе (не добавленный в БД) всё
    // равно не получит профиля, а значит статусов, а значит не сможет добавить данные в БД
    private void signIn() {
        if (mViewModel.getFirebaseUser() == null) {
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.EmailBuilder().build(),
//                new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());
//                new AuthUI.IdpConfig.FacebookBuilder().build(),
//                new AuthUI.IdpConfig.TwitterBuilder().build());

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        } else {
            textEmail.setText(mViewModel.getFirebaseUser().getEmail());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null && user.getEmail()!=null){
                    textEmail.setText(user.getEmail());
                    mViewModel.getProfileByEMail(user.getEmail());
                    mViewModel.setFirebaseUser(user);
                }
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

}