package com.squorpikkor.app.adjustmentdb.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squorpikkor.app.adjustmentdb.DState;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.StatesAdapter;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.SelectStateDialog;
import com.squorpikkor.app.adjustmentdb.ui.main.dialog.SelectStateDialogNew;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static com.squorpikkor.app.adjustmentdb.Encrypter.decodeMe;
import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.SERIAL_TYPE;

public class ScannerFragmentNew extends Fragment {

    private MainViewModel mViewModel;

    TextView txtBarcodeValue;
    SurfaceView surfaceView;
    Button addToBDButton;
    FloatingActionButton addNewStateButton;
    TextView tType;
    TextView tName;
    TextView tInnerSerial;
    TextView tSerial;
    TextView tId;
    ConstraintLayout infoLayout;
    RecyclerView recyclerUnitsStates;

    ArrayList<DUnit> units;
    ArrayList<DState> states;

    public static final String EMPTY_VALUE = "- - -";

    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String intentData = "";
    private static final String SPLIT_SYMBOL = " ";
    private static final String REPAIR_UNIT = "Ремонт";
    private static final String NO_SELECTION = "- не выбрано -";

    public static ScannerFragmentNew newInstance() {
        return new ScannerFragmentNew();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner_new, container, false);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        units = new ArrayList<>();
        states = new ArrayList<>();

        txtBarcodeValue = view.findViewById(R.id.txtBarcodeValue);
        surfaceView = view.findViewById(R.id.surfaceView);
        addToBDButton = view.findViewById(R.id.buttonAddToBD);

        tType = view.findViewById(R.id.textViewType);
        tName = view.findViewById(R.id.textViewName);
        tInnerSerial = view.findViewById(R.id.textViewInnerSerialValue);
        tSerial = view.findViewById(R.id.textViewSerialValue);
        tId = view.findViewById(R.id.textViewIdValue);

//        tType.setText(EMPTY_VALUE);
//        tName.setText(EMPTY_VALUE);
//        tInnerSerial.setText(EMPTY_VALUE);
//        tSerial.setText(EMPTY_VALUE);
//        tId.setText(EMPTY_VALUE);

        infoLayout = view.findViewById(R.id.db_info_layout);
        recyclerUnitsStates = view.findViewById(R.id.recyclerView);
        addNewStateButton = view.findViewById(R.id.addNewState);

        txtBarcodeValue.setVisibility(View.GONE);
        addToBDButton.setVisibility(View.GONE);
        infoLayout.setVisibility(View.GONE);
        addNewStateButton.setVisibility(View.GONE);

        addNewStateButton.setOnClickListener(view1 -> {
            openStatesDialog();
        });

        view.findViewById(R.id.button).setOnClickListener(view1 -> {
            SelectStateDialogNew dialog = new SelectStateDialogNew();
            dialog.setCancelable(false);
//            dialog.setTargetFragment(getTargetFragment(), REQUEST_CODE_KEYWORD_DIALOG);
            dialog.show(requireFragmentManager(), null);
        });

        final MutableLiveData<ArrayList<DUnit>> selectedUnits = mViewModel.getSelectedUnits();
        selectedUnits.observe(getViewLifecycleOwner(), s -> {
            units = selectedUnits.getValue();
            if (units==null)return;
            if (units.size()>1) Log.e(TAG, "* Есть несколько устройств с таким серийником!!!");
            if (units.size() != 0) insertDataToFields(units.get(0));
            ////////////////if (mViewModel.getIsRepair().getValue()) addToBDButton.setText("Отправить данные в БД (ремонт)");
            else addToBDButton.setText("Отправить данные в БД");
        });

        /**Отслеживает список статусов (время + текст) текущего устройства*/
        final MutableLiveData<ArrayList<DState>> statesForUnit = mViewModel.getUnitStatesList();
        statesForUnit.observe(getViewLifecycleOwner(), s -> {
            Log.e(TAG, "onCreateView: список статусов mViewModel.getUnitStatesList");
            this.states = statesForUnit.getValue();
            if (statesForUnit.getValue() != null) Log.e(TAG, "onCreateView список статусов: " + statesForUnit.getValue().size());
            StatesAdapter statesAdapter = new StatesAdapter(this.states);
            recyclerUnitsStates.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerUnitsStates.setAdapter(statesAdapter);
        });

        return view;
    }

    private void insertDataToFields(DUnit unit) {
        Log.e(TAG, "insertDataToFields: ");

        //todo это всё должно браться из viewModel
        tType.setText("- - -");
        if (unit.getType().equals(REPAIR_TYPE)) tType.setText("Ремонт");
        if (unit.getType().equals(SERIAL_TYPE)) tType.setText("Серия");
        tId.setText(unit.getId());
        tName.setText(unit.getName());
        tInnerSerial.setText(unit.getInnerSerial());
        tSerial.setText(unit.getSerial());


        /////////////////mViewModel.setSelectedUnit(unit);//getSelectedUnits().getValue().add(unit);
        if (unit.getType().equals(REPAIR_TYPE)) mViewModel.addSelectedRepairUnitStatesListListener(unit.getId());
        else if (unit.getType().equals(SERIAL_TYPE)) mViewModel.addSelectedSerialUnitStatesListListener(unit.getName(), unit.getInnerSerial());
//        if (mViewModel.getIsRepair().getValue()) mViewModel.addSelectedRepairUnitStatesListListener(unit.getId());
//        else mViewModel.addSelectedSerialUnitStatesListListener(unit.getName(), unit.getInnerSerial());
//        tState.setText(unit.getState());
    }

    private void openStatesDialog() {
        //должен загружаться тот список, тип прибора который загружен — ремонт или серия
        ArrayList<String> rightList = mViewModel.getSerialStatesList().getValue();
        Log.e(TAG, "openStatesDialog: SIZE = "+mViewModel.getSelectedUnits().getValue().size());
        if (mViewModel.getSelectedUnits().getValue().get(0).getType().equals(REPAIR_TYPE)) rightList = mViewModel.getRepairStatesList().getValue();

        Log.e(TAG, "** stateList.size() = "+rightList.size());
        SelectStateDialog dialog = new SelectStateDialog(getActivity(), mViewModel, rightList);
        dialog.show();


    }

    private void initialiseDetectorsAndSources() {
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
                //Toast.makeText(getContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(@NotNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    txtBarcodeValue.post(() -> {
                        intentData = barcodes.valueAt(0).displayValue;
                        workingWithBarCode(intentData);
                        cameraSource.stop();
                    });
                }
            }
        });
    }

    private void workingWithBarCode(String intentData) {
        intentData = decodeMe(intentData);
        txtBarcodeValue.setText(intentData);
        txtBarcodeValue.setVisibility(View.VISIBLE);

        String[] ar = intentData.split(SPLIT_SYMBOL);
        if (ar.length == 2) {
            //Для серии: имя+внутренний_серийный (БДКГ-02 1234)
            //Для ремонта: "Ремонт"+id (Ремонт 0001)
            String name = ar[0];
            String innerSerial = ar[1];
            //addToBDButton.setVisibility(View.VISIBLE);
            addNewStateButton.setVisibility(View.VISIBLE);
            infoLayout.setVisibility(View.VISIBLE);

            //Смысл в том, что если отсканированный блок есть в БД, то данные для этого блока
            // беруться из БД (getRepairUnitById), если этого блока в БД нет (новый), то данные для
            // блока берутся из QR-кода
            if (name.equals(REPAIR_UNIT)) {//Если это ремонт
                //////mViewModel.setIsRepair(true);
                mViewModel.setSelectedUnit(new DUnit(innerSerial, "", "", "", "", REPAIR_TYPE));
                mViewModel.getRepairUnitById(innerSerial);
//todo надо добавить: если данные для юнита получены и включен лэйаут с данными, то камеру нужно выключить (она всё ещё включена под лэйаутом!)
            } else {
                //////mViewModel.setIsRepair(false);
                mViewModel.setSelectedUnit(new DUnit("", name, innerSerial, "", "", SERIAL_TYPE));
                mViewModel.getDUnitByNameAndInnerSerial(name, innerSerial);
            }
        }
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