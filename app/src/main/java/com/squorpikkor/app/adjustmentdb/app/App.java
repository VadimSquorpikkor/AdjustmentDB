package com.squorpikkor.app.adjustmentdb.app;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.squorpikkor.app.adjustmentdb.BuildConfig;

public class App extends Application {
    private static Application mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        showLogo();
    }

    public static Context getContext(){
        return mApplication.getApplicationContext();
    }

    static void showLogo() {
        Log.e(TAG, "╔═══╗╔════╗ ╔══╗╔══╗╔══╗╔═══╗╔════╗╔══════╗╔═══╗╔═╗ ╔╗╔════╗   ╔════╗╔═══╗  ");
        Log.e(TAG, "║╔═╗║╚╗╔═╗║ ╚═╗║╚╗╔╝╚╗╔╝║╔═╗║║╔╗╔╗║║╔═╗╔═╗║║╔══╝║ ╚╗║║║╔╗╔╗║   ╚╗╔═╗║╚╗╔╗║  ");
        Log.e(TAG, "║║ ║║ ║║ ║║   ║║ ║║  ║║ ║╚══╗╚╝║║╚╝║║ ║║ ║║║╚══╗║╔╗╚╝║╚╝║║╚╝    ║║ ║║ ║╚╝╚╗ ");
        Log.e(TAG, "║╚═╝║ ║║ ║║   ║║ ║║  ║║ ╚══╗║  ║║  ║║ ║║ ║║║╔══╝║║╚╗ ║  ║║      ║║ ║║ ║╔═╗║ ");
        Log.e(TAG, "║╔═╗║╔╝╚═╝║║╚═╝║ ║╚══╝║ ║╚═╝║  ║║  ║║ ║║ ║║║╚══╗║║ ║ ║  ║║     ╔╝╚═╝║╔╝╚═╝║ ");
        Log.e(TAG, "╚╝ ╚╝╚════╝╚═══╝ ╚════╝ ╚═══╝  ╚╝  ╚╝ ╚╝ ╚╝╚═══╝╚╝ ╚═╝  ╚╝     ╚════╝╚════╝ ");
        Log.e(TAG, "♣VERSION_NAME: " + BuildConfig.VERSION_NAME);
    }
}
