package com.squorpikkor.app.adjustmentdb;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.squorpikkor.app.adjustmentdb.ui.main.MainFragment;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }
}