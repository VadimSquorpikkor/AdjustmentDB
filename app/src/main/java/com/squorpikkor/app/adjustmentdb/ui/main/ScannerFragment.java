package com.squorpikkor.app.adjustmentdb.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.Encrypter;
import com.squorpikkor.app.adjustmentdb.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class ScannerFragment extends Fragment {

    private MainViewModel mViewModel;
    EditText tName;
    EditText tSerial;
    TextView txtBarcodeValue;
    SurfaceView surfaceView;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String intentData = "";
    Button sendButton;
    private static final String SPLIT_SYMBOL = " ";

    public static ScannerFragment newInstance() {
        return new ScannerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        tName = view.findViewById(R.id.editTextName);
        tSerial = view.findViewById(R.id.editTextSerial);
        sendButton = view.findViewById(R.id.buttonAddToBD);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        txtBarcodeValue = view.findViewById(R.id.txtBarcodeValue);
        surfaceView = view.findViewById(R.id.surfaceView);
        txtBarcodeValue.setVisibility(View.GONE);
        sendButton.setEnabled(false);


        sendButton.setOnClickListener(view1 -> {
            String name = tName.getText().toString();
            String serial = tSerial.getText().toString();
            mViewModel.saveDUnitToDB(new DUnit(name, serial));
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStackImmediate();
            }
        });



        return view;
    }

    private void initialiseDetectorsAndSources() {
        Toast.makeText(getContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(Objects.requireNonNull(getActivity()))
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(getActivity(), barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Toast.makeText(getContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(@NotNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    txtBarcodeValue.post(() -> {
                        intentData = barcodes.valueAt(0).displayValue;
                        txtBarcodeValue.setText(intentData);

                        txtBarcodeValue.setVisibility(View.VISIBLE);
                        Log.e(TAG, "************* " + decodeMe(intentData));

                        String[] ar = intentData.split(SPLIT_SYMBOL);
                        if (ar.length == 2) {
                            tName.setText(ar[0]);
                            tSerial.setText(ar[1]);
                            sendButton.setEnabled(true);


                        }
                        cameraSource.stop();
                    });
                }
            }
        });
    }

    private String decodeMe(String code) {
        String output = "";
        String inputString = code;
        byte[] byteArray = inputString.getBytes(StandardCharsets.UTF_16LE);
        output = new String(Encrypter.convertData(byteArray), StandardCharsets.UTF_16LE);
        return output;
    }


    @Override
    public void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    public void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }
}