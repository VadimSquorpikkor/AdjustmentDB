package com.squorpikkor.app.adjustmentdb.ui.main.scanner;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.squorpikkor.app.adjustmentdb.R;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class ScannerNew {

    private final Activity context;
    private final boolean isMultiScan;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private final ScannerDataShow scannerDataShow;

    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private String intentData = "";
    private HashSet<String> dataSet;

    public ScannerNew(Activity context, boolean isMultiScan, ScannerDataShow scannerDataShow) {
        this.context = context;
        this.isMultiScan = isMultiScan;
        this.scannerDataShow = scannerDataShow;
    }

    public void initialiseDetectorsAndSources() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(context, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.setVisibility(View.VISIBLE);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions((Activity) context, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

            @Override
            public void release() {
                //Toast.makeText(getContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(@NotNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    context.runOnUiThread(() -> {
                        if (isMultiScan)worksWithBarcodeAnswer(barcodes);
                        else worksWithMultiBarcodeAnswer(barcodes);
                    });
                }
            }
        });
    }

    void worksWithBarcodeAnswer(SparseArray<Barcode> barcodes) {
        intentData = barcodes.valueAt(0).displayValue;
        scannerDataShow.saveUnit(intentData);
        cameraSource.stop();
        MediaPlayer.create(context, R.raw.fast_beep).start();
    }

    void worksWithMultiBarcodeAnswer(SparseArray<Barcode> barcodes) {
        for (int i = 0; i < barcodes.size(); i++) {
            intentData = barcodes.valueAt(i).displayValue;
            if (!dataSet.contains(intentData)){
                dataSet.add(intentData);
                MediaPlayer.create(context, R.raw.fast_beep).start();
                Log.e(TAG, "♦ "+intentData);
                scannerDataShow.addUnitToCollection(intentData);
            }
        }
    }

    public void cameraSourceRelease() {
        if (cameraSource!=null){
            cameraSource.release();
            cameraSource.stop();
        }

    }
}
