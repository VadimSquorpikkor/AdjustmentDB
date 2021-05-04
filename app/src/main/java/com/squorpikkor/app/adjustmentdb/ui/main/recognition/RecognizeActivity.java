package com.squorpikkor.app.adjustmentdb.ui.main.recognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.squorpikkor.app.adjustmentdb.R;

import java.io.IOException;

public class RecognizeActivity extends AppCompatActivity {

    SurfaceView mCameraView;
    TextView mTextView;
    CameraSource mCameraSource;

    private static final String TAG = "MainActivity";
    private static final int requestPermissionID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recognize_activity);

        mCameraView = findViewById(R.id.surfaceView);
        mTextView = findViewById(R.id.text_view);

        startCameraSource();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    String cutSerialString(String text, String mask) {
        Log.e(TAG, "♦1♦ cutSerialString: "+text);
        String s = text.substring(text.indexOf(mask));
        Log.e(TAG, "♦2♦ cutSerialString: "+s);
        s = s.replace(mask, "");
        if (s.startsWith(" ")) s = s.replaceFirst(" ", "");
        Log.e(TAG, "♦3♦ cutSerialString: "+s);
        s = s.split(" ")[0];
        Log.e(TAG, "♦4♦ cutSerialString: "+s);
        s = s.trim();
        Log.e(TAG, "♦5♦ cutSerialString: "+s);
        Log.e(TAG, "☻☻☻ cutSerialString: "+s);
        return s;
    }

    String getSerial(TextBlock item) {
        String result = "";
        String text = item.getComponents().get(0).getValue();
        Log.e(TAG, "♦---------------");
        Log.e(TAG, "getSerial text: "+text);
        if (text.contains("Serial Na")) return cutSerialString(text, "Serial Na");
        else if (text.contains("Serial No")) return cutSerialString(text, "Serial No");
        else if (text.contains("Serial Ne")) return cutSerialString(text, "Serial Ne");
        else if (text.contains("Serial N")) return cutSerialString(text, "Serial N");
        else if (text.contains("Serial. N")) return cutSerialString(text, "Serial. N");
        else if (text.contains("Serial.N")) return cutSerialString(text, "Serial.N");
        else if (text.contains("SerialN")) return cutSerialString(text, "SerialN");
        else return "";
    }

    String getDevName(TextBlock item) {
        String result = "";
        String text = item.getComponents().get(0).getValue();
        return result;
    }

    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
            */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(RecognizeActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionID);
                            return;
                        }
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();
                }
            });

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0 ){

                        mTextView.post(() -> {
                            for(int i=0;i<items.size();i++){
                                TextBlock item = items.valueAt(i);
                                String res = getSerial(item);
                                if (!res.equals("")) mTextView.setText(getSerial(item));
                            }
                        });
                    }
                }
            });
        }
    }
}
