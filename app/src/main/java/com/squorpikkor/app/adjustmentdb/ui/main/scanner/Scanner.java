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

import androidx.appcompat.widget.SwitchCompat;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.ui.main.scanner.Encrypter.decodeMe;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.SERIAL_TYPE;

class Scanner {

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
    private SwitchCompat switchCompat;
    private FloatingActionButton addNewStateButton;
    private ConstraintLayout infoLayout;
    private Button nextButton;
    private final MainViewModel mViewModel;

    private HashSet<String> dataSet;
    private ArrayList<DUnit> unitList;

    Scanner(FragmentActivity activity, View view, MainViewModel mViewModel) {
        this.activity = activity;
        this.view = view;
        this.mViewModel = mViewModel;
        init();
    }

    private void init() {
        surfaceView = view.findViewById(R.id.surfaceView);
        txtBarcodeValue = view.findViewById(R.id.txtBarcodeValue);
        infoLayout = view.findViewById(R.id.db_info_layout);
        addNewStateButton = view.findViewById(R.id.addNewState);
        txtBarcodeValue.setVisibility(View.GONE);
        infoLayout.setVisibility(View.GONE);
        switchCompat = view.findViewById(R.id.switch_auto);
        dataSet = new HashSet<>();
        unitList = new ArrayList<>();
        foundCount = view.findViewById(R.id.found_count);
        nextButton = view.findViewById(R.id.button_next);

        /*nextButton.setOnClickListener(v -> {
            doNext();
        });*/
    }

    public void initialiseDetectorsAndSources() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(Objects.requireNonNull(activity))
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(activity, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

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
                    txtBarcodeValue.post(() -> {
                        intentData = barcodes.valueAt(0).displayValue;
                        if (switchCompat.isChecked()) {
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
                }
            }
        });
    }

    /*private void doNext() {
        infoLayout.setVisibility(View.VISIBLE);
        mViewModel.getFoundUnitsList().setValue(unitList);
        Log.e(TAG, "saveFoundUnits: SIZE - "+mViewModel.getFoundUnitsList().getValue().size());
    }*/

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
            //Смысл в том, что если отсканированный блок есть в БД, то данные для этого блока
            // беруться из БД (getRepairUnitById), если этого блока в БД нет (новый), то данные для
            // блока берутся из QR-кода
            if (unit.isRepairUnit()) {//Если это ремонт
                mViewModel.setSelectedUnit(unit);
                mViewModel.getRepairUnitById(unit.getId());
                //todo надо добавить: если данные для юнита получены и включен лэйаут с данными, то камеру нужно выключить (она всё ещё включена под лэйаутом!)
            } else {
                mViewModel.setSelectedUnit(unit);
                mViewModel.getDUnitByNameAndInnerSerial(unit.getName(), unit.getInnerSerial());
            }
        }
    }

    private DUnit getDUnitFromString(String s) {
        s = decodeMe(s);
        txtBarcodeValue.setText(s);
        txtBarcodeValue.setVisibility(View.VISIBLE);
        String[] ar = s.split(SPLIT_SYMBOL);
        if (ar.length == 2) {
            //Для серии: имя+внутренний_серийный (БДКГ-02 1234)
            //Для ремонта: "Ремонт"+id (Ремонт 0001)
            String name = ar[0];
            String innerSerial = ar[1];


            // Если это ремонт:
            if (name.equals(REPAIR_UNIT)) return new DUnit(innerSerial, "", "", "", "", "", REPAIR_TYPE);
            // Если это серия:
            else return new DUnit("", name, innerSerial, "", "", "", SERIAL_TYPE);
        // Если строка некорректная, возвращаю null
        } else return null;
    }

    public void cameraSourceRelease() {
        cameraSource.release();
    }
}
