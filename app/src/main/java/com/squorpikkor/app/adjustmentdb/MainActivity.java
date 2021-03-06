package com.squorpikkor.app.adjustmentdb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squorpikkor.app.adjustmentdb.ui.main.DrawableTask;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.fragment.UnitInfoFragment;
import com.squorpikkor.app.adjustmentdb.ui.main.fragment_cradle.SectionsPagerAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.fragment_cradle.ZoomOutPageTransformer;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.squorpikkor.app.adjustmentdb.BuildConfig.VERSION_NAME;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.BACK_PRESS_INFO_FRAGMENT;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    public static final String TAG = "tag";
    MainViewModel mViewModel;
    TextView location;
    TextView locationText;
    TextView emailText;
    TextView version;
    ImageView accountImage;
    private static final int RC_SIGN_IN = 1;
    TabLayout tabs;
    DrawerLayout drawer_layout;
    NavigationView navigationView;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(1);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        drawer_layout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //CustomView
        tabs.getTabAt(0).setCustomView(R.layout.tab_view_0);
        tabs.getTabAt(1).setCustomView(R.layout.tab_view_1);
        tabs.getTabAt(2).setCustomView(R.layout.tab_view_2);

        //Для варианта без customView
        //setupTabIcons();

        location = findViewById(R.id.location);

        View headerView = navigationView.getHeaderView(0);
        version = headerView.findViewById(R.id.version);
        String appName = getString(R.string.app_name);
        version.setText(String.format("%s %s", appName, VERSION_NAME));
        locationText = headerView.findViewById(R.id.location_text);
        emailText = headerView.findViewById(R.id.email_text);
        accountImage = headerView.findViewById(R.id.account_image);

        final MutableLiveData<Drawable> getImage = mViewModel.getUserImage();
        getImage.observe(this, drawable -> accountImage.setImageDrawable(drawable));

        /*final MutableLiveData<String> locationId = mViewModel.getLocation_id();
        locationId.observe(this, s -> {
            if (locationId.getValue() != null) {
                mViewModel.setStatesForLocation(locationId.getValue());
            }
        });*/

        final MutableLiveData<String> locationName = mViewModel.getLocationName();
        locationName.observe(this, s -> {
            if (locationName.getValue() != null) {
                location.setText(locationName.getValue());
                locationText.setText(locationName.getValue());
            }
        });

        final MutableLiveData<Boolean> goToSearch = mViewModel.getGoToSearchTab();
        goToSearch.observe(this, this::goToSearchTab);

        signIn();
    }

    private void setupTabIcons() {
        tabs.getTabAt(0).setIcon(R.drawable.ic_baseline_camera_24);
        tabs.getTabAt(1).setIcon(R.drawable.ic_baseline_search_24);
        tabs.getTabAt(2).setIcon(R.drawable.ic_baseline_multi_24);
    }

    private void reSignIn() {
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.GoogleBuilder().build());
        AuthUI.getInstance().signOut(this);
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
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
//                    accountImage.setImageDrawable(user.getPhotoUrl());
                    mViewModel.setLocationByEmail(user.getEmail());
                    mViewModel.setFirebaseUser(user);

                    DrawableTask task = new DrawableTask(mViewModel);
                    if (user.getPhotoUrl()==null){mViewModel.updateUserImage(ContextCompat.getDrawable(this, R.mipmap.logo));}
                    else {
                        task.execute(user.getPhotoUrl().toString());
                    }
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
            reSignIn();
        } else if (id == R.id.third) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.finishAndRemoveTask();
            } else {
                this.finish();
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }

    //Замена для AsyncTask (который deprecated)
    public void doSomeTaskAsync(String url) {
        ExecutorService executors = Executors.newFixedThreadPool(1);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // your async code goes here.
                HttpURLConnection connection = null;
                Bitmap x;
                InputStream input = null;
                Drawable img;
                try {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.connect();
                    input = connection.getInputStream();

                    x = BitmapFactory.decodeStream(input);
                    img = new BitmapDrawable(Resources.getSystem(), x);
                    mViewModel.updateUserImage(img);
                } catch (Exception e) {
                    Log.e(TAG, "run: EXCEPTION");
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }
        };
        executors.submit(runnable);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Если кликнуть в SearchFragment на пункт списка и открыть UnitInfoFragment,
        // то нажатие на кнопку "назад" будет работать в обычном режиме, значит при нажатии будет
        // закрываться UnitInfoFragment и возвращаться к SearchFragment
        // Если открыть ScanFragment при открытом UnitInfoFragment, то нажатие "назад" вернёт на UnitInfoFragment(!)

        //if (mViewModel.getBackPressCommand().equals(BACK_PRESS_INFO_FRAGMENT)) {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.child_fragment_container_3);
        if(viewPager.getCurrentItem()==1 && f instanceof UnitInfoFragment){//если во втором пейджере И в открытом UnitInfoFragment
            return super.onKeyDown(keyCode, event);
        }
        //Иначе нажатие перехватывается и кнопка "назад" уже работает по-разному от ситуации
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mViewModel.getBack();
        }
        return false;
    }

    private void goToSearchTab(boolean state) {
        if (state) viewPager.setCurrentItem(1);
    }


}