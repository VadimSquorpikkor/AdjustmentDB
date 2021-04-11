package com.squorpikkor.app.adjustmentdb.ui.main.scanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.ui.main.scanner.Encrypter.decodeMe;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.SERIAL_TYPE;

public class Scanner {

    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private String intentData = "";
    private static final String SPLIT_SYMBOL = " ";
    private static final String REPAIR_UNIT = "Ремонт";

    private final FragmentActivity activity;
    private final View view;
    private SurfaceView surfaceView;
    private TextView txtBarcodeValue;
    private TextView foundCount;
    private FloatingActionButton addNewStateButton;
    private ConstraintLayout infoLayout;
    private Button nextButton;
    private final MainViewModel mViewModel;

    private HashSet<String> dataSet;
    private ArrayList<DUnit> unitList;
    private boolean isMultiScan;

    public Scanner(FragmentActivity activity, View view, MainViewModel mViewModel, boolean isMultiScan) {
        this.activity = activity;
        this.view = view;
        this.mViewModel = mViewModel;
        this.isMultiScan = isMultiScan;

        txtBarcodeValue = view.findViewById(R.id.txtBarcodeValue);
        txtBarcodeValue.setVisibility(View.GONE);
        if (!isMultiScan) {
            this.surfaceView = view.findViewById(R.id.surfaceViewS);
            this.surfaceView.setVisibility(View.INVISIBLE);
            init();
        } else {
            this.surfaceView = view.findViewById(R.id.surfaceViewM);
            this.surfaceView.setVisibility(View.INVISIBLE);
            nextButton = view.findViewById(R.id.button_next);
            nextButton.setVisibility(View.GONE);
            dataSet = new HashSet<>();
            unitList = new ArrayList<>();
            foundCount = view.findViewById(R.id.found_count);
        }
    }

    void init() {
        infoLayout = view.findViewById(R.id.db_info_layout);
        addNewStateButton = view.findViewById(R.id.addNewState);
        infoLayout.setVisibility(View.GONE);
    }

    public void setSurfaceVisible(boolean state) {
        if (state) surfaceView.setVisibility(View.VISIBLE);
        else surfaceView.setVisibility(View.INVISIBLE);
    }

    public void initialiseDetectorsAndSources() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(Objects.requireNonNull(activity))
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(activity, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.setVisibility(View.VISIBLE);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(activity), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(activity, new
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

//                final MediaPlayer mp = MediaPlayer.create(activity, R.raw.fast_beep);
                /*MediaPlayer mp = MediaPlayer.create(activity, R.raw.fast_beep);
                 mp.setLooping(false);
                 mp.setOnSeekCompleteListener(mp1 -> {
                     mp.stop();
                     mp.release();
                 });*/

                if (barcodes.size() != 0) {
                    activity.runOnUiThread(() -> {
                        intentData = barcodes.valueAt(0).displayValue;
                        if (isMultiScan) {
                            for (int i = 0; i < barcodes.size(); i++) {
                                intentData = barcodes.valueAt(i).displayValue;
                                if (!dataSet.contains(intentData)){
                                    dataSet.add(intentData);
//                                    mp.start();
                                    MediaPlayer.create(activity, R.raw.fast_beep).start();
                                    Log.e(TAG, "♦ "+intentData);
                                    addUnitToCollection(getDUnitFromString(intentData));
                                }
                            }
                        } else {
                            intentData = barcodes.valueAt(0).displayValue;
                            saveUnit(getDUnitFromString(intentData));
                            cameraSource.stop();
                            MediaPlayer.create(activity, R.raw.fast_beep).start();
//                            mp.start();
                        }

                    });
                    //txtBarcodeValue.post(() -> {
                    //});
                }
            }
        });
    }



    private void addUnitToCollection(DUnit unit) {
        if (unit!=null){
            unitList.add(unit);
            foundCount.setText(String.valueOf(unitList.size()));
            if (unitList.size()!=0) nextButton.setVisibility(View.VISIBLE);
            if (unit.isRepairUnit()) Log.e(TAG, unit.getId());
            else Log.e(TAG, unit.getName()+" "+unit.getInnerSerial());

            mViewModel.getFoundUnitsList().setValue(unitList);

        }
    }

    private void saveUnit(DUnit unit) {
        if (unit != null) {
            addNewStateButton.setVisibility(View.VISIBLE);
            infoLayout.setVisibility(View.VISIBLE);
//            surfaceView.setVisibility(View.INVISIBLE);
            //Смысл в том, что если отсканированный блок есть в БД, то данные для этого блока
            // беруться из БД (getRepairUnitById), если этого блока в БД нет (новый), то данные для
            // блока берутся из QR-кода
            mViewModel.updateSelectedUnit(unit);
            mViewModel.getThisUnitFromDB(unit);
        }
    }

    private DUnit getDUnitFromString(String s) {
        s = decodeMe(s);
        txtBarcodeValue.setText(s);
        txtBarcodeValue.setVisibility(View.VISIBLE);
        String[] ar = s.split(SPLIT_SYMBOL);
        if (ar.length == 2) {
            //Для серии: имя+внутренний_серийный (БДКГ-02 1234), id = БДКГ-02_1234
            //Для ремонта: "Ремонт"+id (Ремонт 0001), id = r_0005
            String name = ar[0];
            String innerSerial = ar[1];
            String id;
            String location = mViewModel.getLocation_id().getValue();

            // Если это ремонт:
            if (name.equals(REPAIR_UNIT)){
                id = "r_"+ar[1];
                return new DUnit(id, "", "", "", "", "", REPAIR_TYPE, location);
            }
            // Если это серия:
            else{
                id = name+"_"+innerSerial;
                return new DUnit(id, name, innerSerial, "", "", "", SERIAL_TYPE, location);
            }

        // Если строка некорректная, возвращаю null
        } else return null;
    }

    public void cameraSourceRelease() {
        if (cameraSource!=null){
            cameraSource.release();
            cameraSource.stop();
        }

    }
}
