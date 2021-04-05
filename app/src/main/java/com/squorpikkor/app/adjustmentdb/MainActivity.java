package com.squorpikkor.app.adjustmentdb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.ui.main.SectionsPagerAdapter;

import java.util.Arrays;
import java.util.List;

import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EMPTY_PROFILE_NAME;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "tag";
    MainViewModel mViewModel;
    TextView location;
    private static final int RC_SIGN_IN = 1;
    TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(1);

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        setupTabIcons();

        location = findViewById(R.id.location);

        final MutableLiveData<String> profileName = mViewModel.getProfileName();
        profileName.observe(this, s -> {
            if (profileName.getValue() != null) {
                if (profileName.getValue().equals(EMPTY_PROFILE_NAME))
                    Log.e(TAG, "♦♦♦ EMPTY_PROFILE_NAME");//todo может и без иф, просто присваивать. А может и с иф
                else Log.e(TAG, "♦♦♦ profileName = " + profileName.getValue());

                mViewModel.setSelectedProfile(profileName.getValue());
            }

        });

        final MutableLiveData<String> locationName = mViewModel.getLocationName();
        locationName.observe(this, s -> {
            if (locationName.getValue() != null) {
                location.setText(locationName.getValue());
            }
        });

        signIn();
    }

    private void setupTabIcons() {
        tabs.getTabAt(0).setIcon(R.drawable.ic_baseline_camera_24);
        tabs.getTabAt(1).setIcon(R.drawable.ic_baseline_search_24);
        tabs.getTabAt(2).setIcon(R.drawable.ic_baseline_multi_24);
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
            //todo//textEmail.setText(mViewModel.getFirebaseUser().getEmail());
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
                if (user != null && user.getEmail() != null) {
                    //todo//textEmail.setText(user.getEmail());
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