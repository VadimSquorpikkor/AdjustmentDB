package com.squorpikkor.app.adjustmentdb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
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
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import com.squorpikkor.app.adjustmentdb.ui.main.fragment.UnitInfoFragment;
import com.squorpikkor.app.adjustmentdb.ui.main.fragment_cradle.SectionsPagerAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.fragment_cradle.ZoomOutPageTransformer;

import java.util.Collections;
import java.util.List;

import static com.squorpikkor.app.adjustmentdb.BuildConfig.VERSION_NAME;
import static com.squorpikkor.app.adjustmentdb.Constant.EMPTY_LOCATION_ID;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "tag";
    private MainViewModel mViewModel;
    private TextView location;
    private TextView locationText;
    private ImageView accountImage;
    private static final int RC_SIGN_IN = 101;
    private DrawerLayout drawer_layout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //при первом запуске установить значения настроек по умолчанию, без этого обращение к
        // настройкам будет возвращать null до тех пор, пока пользователь не зайдет в настройки (SettingsActivity)
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);
        setContentView(R.layout.activity_main);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        //Выбор вкладки, которая будет открываться при старте приложения.
        //Выбирается пользователем в настройках, по умолчанию — 1 ("Поиск"). Нумерация идет с 0)
//        String saved = PreferenceManager.getDefaultSharedPreferences(this).getString("preferredTab", "1");
//        int preferredTab = Integer.parseInt(saved);
        int preferredTab = SaveLoad.getPrefInt(R.string.preferred_tab, 1);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(preferredTab);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        drawer_layout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //CustomView
        //noinspection ConstantConditions
        tabs.getTabAt(0).setCustomView(R.layout.tab_view_0);
        //noinspection ConstantConditions
        tabs.getTabAt(1).setCustomView(R.layout.tab_view_1);
        //noinspection ConstantConditions
        tabs.getTabAt(2).setCustomView(R.layout.tab_view_2);

        location = findViewById(R.id.location);

        View headerView = navigationView.getHeaderView(0);
        TextView version = headerView.findViewById(R.id.version);
        String appName = getString(R.string.app_name);
        version.setText(String.format("%s %s", appName, VERSION_NAME));
        locationText = headerView.findViewById(R.id.location_text);
        TextView emailText = headerView.findViewById(R.id.email_text);
        accountImage = headerView.findViewById(R.id.account_image);

        mViewModel.getUserImage().observe(this, drawable -> accountImage.setImageDrawable(drawable));

        mViewModel.getLocationName().observe(this, name -> {
            if (name != null) {
                location.setText(name);
                locationText.setText(name);
            }
        });

        mViewModel.getGoToSearchTab().observe(this, this::goToSearchTab);

        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) {
            Log.e(TAG, "*********************************************onCreate: "+FirebaseAuth.getInstance().getCurrentUser().getEmail());
            mViewModel.checkUserEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        } else{
            //mViewModel.getLocationName().setValue(EMPTY_LOCATION_NAME);
            signIn();
        }

        mViewModel.getLocations().observe(this, locations -> {
//            locationName.setValue(mViewModel.getLocationNameById(mViewModel.getLocation_id().getValue()));
            String name = mViewModel.getLocationNameById(mViewModel.getLocation_id().getValue()==null?EMPTY_LOCATION_ID:mViewModel.getLocation_id().getValue());
            location.setText(name);
            locationText.setText(name);
        });

        mViewModel.getEmail().observe(this, emailText::setText);
    }

    private void signIn() {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                mViewModel.checkUserEmail(response.getEmail());
            } else {
                Log.e(TAG, "******************************onActivityResult: NOT OK");
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setCheckable(false);
        int id = item.getItemId();
        switch (id) {
            case R.id.account_menu: signIn(); break;
            case R.id.settings_menu: startActivity(new Intent(this, SettingsActivity.class)); break;
            case R.id.exit_menu: this.finishAndRemoveTask(); break;
        }

        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Если кликнуть в SearchFragment на пункт списка и открыть UnitInfoFragment,
        // то нажатие на кнопку "назад" будет работать в обычном режиме, значит при нажатии будет
        // закрываться UnitInfoFragment и возвращаться к SearchFragment
        // Если открыть ScanFragment при открытом UnitInfoFragment, то нажатие "назад" вернёт на UnitInfoFragment(!)

        //if (mViewModel.getBackPressCommand().equals(BACK_PRESS_INFO_FRAGMENT)) {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.child_fragment_container_3);
        if (viewPager.getCurrentItem() == 1 && f instanceof UnitInfoFragment) {//если во втором пейджере И в открытом UnitInfoFragment
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