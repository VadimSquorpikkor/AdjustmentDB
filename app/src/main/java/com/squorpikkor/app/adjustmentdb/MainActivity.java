package com.squorpikkor.app.adjustmentdb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.fragment_cradle.SectionsPagerAdapter;

import java.util.Arrays;
import java.util.List;

import static com.squorpikkor.app.adjustmentdb.BuildConfig.VERSION_NAME;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    public static final String TAG = "tag";
    MainViewModel mViewModel;
    TextView location;
    TextView locationText;
    TextView emailText;
    TextView version;
    private static final int RC_SIGN_IN = 1;
    TabLayout tabs;
    DrawerLayout drawer_layout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(1);

        drawer_layout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        setupTabIcons();

        location = findViewById(R.id.location);

        View headerView = navigationView.getHeaderView(0);
        version = headerView.findViewById(R.id.version);
        String appName = getString(R.string.app_name);
        version.setText(String.format("%s %s", appName, VERSION_NAME));
        locationText = headerView.findViewById(R.id.location_text);
        emailText = headerView.findViewById(R.id.email_text);

        final MutableLiveData<String> locationId = mViewModel.getLocation_id();
        locationId.observe(this, s -> {
            if (locationId.getValue() != null) {
                mViewModel.setStatesForLocation(locationId.getValue());
            }
        });

        final MutableLiveData<String> locationName = mViewModel.getLocationName();
        locationName.observe(this, s -> {
            if (locationName.getValue() != null) {
                location.setText(locationName.getValue());
                locationText.setText(locationName.getValue());
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
                    emailText.setText(user.getEmail());
                    mViewModel.getLocationIdByEMail(user.getEmail());
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setCheckable(false);
        int id = item.getItemId();
        if (id == R.id.first) {
            Log.e(TAG, "onNavigationItemSelected: FIRST");
        } else if (id == R.id.second) {
            Log.e(TAG, "onNavigationItemSelected: SECOND");
        } else if (id == R.id.third) {
            Log.e(TAG, "onNavigationItemSelected: THIRD");
        }
        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }

}