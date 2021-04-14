package com.squorpikkor.app.adjustmentdb.ui.main;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DrawableTask  extends AsyncTask<String, Void, Drawable> {

    MainViewModel mViewModel;

    public DrawableTask(MainViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }

    protected Drawable doInBackground(String... urls) {
        HttpURLConnection connection = null;
        Bitmap x;
        InputStream input = null;
        try {
            connection = (HttpURLConnection) new URL(urls[0]).openConnection();
            connection.connect();
            input = connection.getInputStream();

            x = BitmapFactory.decodeStream(input);
            return new BitmapDrawable(Resources.getSystem(), x);
        } catch (Exception e) {

            return null;
        } finally {
            if (connection!=null)connection.disconnect();
        }
    }

    protected void onPostExecute(Drawable img) {
        mViewModel.updateUserImage(img);
    }
}

