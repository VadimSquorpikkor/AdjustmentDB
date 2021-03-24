package com.squorpikkor.app.adjustmentdb.ui.main.scanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.DevType;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static com.squorpikkor.app.adjustmentdb.ui.main.scanner.Encrypter.decodeMe;
import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class ScannerFragment extends Fragment {

    //todo Разделить на два фрагмента: QR-сканер и отображение и редактирование данных. А лучше —
    // фрагмент сканнера слелать внутри фрагмента данных, кнопка "Добавить в БД" так останется внизу
    // (ну и где это будет нужно)

    private MainViewModel mViewModel;

    EditText tId;
    EditText tName;
    EditText tInnerSerial;
    EditText tSerial;
    EditText tState;

    TextView txtBarcodeValue;
    SurfaceView surfaceView;
    Button sendButton;

    boolean state;

    boolean isRepairDev;

    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String intentData = "";
    private static final String SPLIT_SYMBOL = " ";
    private static final String REPAIR_UNIT = "Ремонт";
    private static final String NO_SELECTION = "- не выбрано -";

    ArrayList<DevType> devTypeList;
    ArrayList<String> devSpinnerList;

    ArrayList<String> serialStateSpinnerList;
    ArrayList<String> repairStateSpinnerList;

    //todo по сути — для units не нужна коллекция, нужен только один DUnit. С другой стороны БД
    // отдает всё, чтоона нашла по данному серийнику и это всё она отдает. Забота ScannerFragment'a
    // решать, что с этим всем делать, если нашлось несколько, то можно предупредить пользователя,
    // что есть несколько устройств в базе с одинаковым именем
    ArrayList<DUnit> units;


    public static ScannerFragment newInstance() {
        return new ScannerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        tId = view.findViewById(R.id.editTextId);
        tName = view.findViewById(R.id.editTextName);
        tInnerSerial = view.findViewById(R.id.editTextInnerSerial);
        tSerial = view.findViewById(R.id.editTextSerial);
        tState = view.findViewById(R.id.editTextState);

        tId.setEnabled(false);
        tName.setEnabled(false);
        tInnerSerial.setEnabled(false);
        tSerial.setEnabled(false);
        tState.setEnabled(false);

        sendButton = view.findViewById(R.id.buttonAddToBD);
        txtBarcodeValue = view.findViewById(R.id.txtBarcodeValue);
        surfaceView = view.findViewById(R.id.surfaceView);
        txtBarcodeValue.setVisibility(View.GONE);
        sendButton.setEnabled(false);

        /**Создает новый объект DUnit, заполняет его данными из формы и отправляет объект в БД*/
        sendButton.setOnClickListener(view1 -> {
            String id = tId.getText().toString();
            String name = tName.getText().toString();
            String innerSerial = tInnerSerial.getText().toString();
            String serial = tSerial.getText().toString();
            String state = tState.getText().toString();
            ////////////if (isRepairDev) mViewModel.saveRepairUnitToDB(new DUnit(id, name, innerSerial, serial, state));
            //////////else mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, state));
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStackImmediate();
            }
        });

        /**Кнопка включения/включения режима редактирования для полей EditText*/
        view.findViewById(R.id.actionButtonEdit).setOnClickListener(v -> {
            state = !state;
//            tId.setEnabled(state);
//            tName.setEnabled(state);
            tInnerSerial.setEnabled(state);
            tSerial.setEnabled(state);
//            tState.setEnabled(state);
        });


        units = new ArrayList<>();

        final MutableLiveData<ArrayList<DUnit>> selectedUnits = mViewModel.getSelectedUnits();
        selectedUnits.observe(getViewLifecycleOwner(), s -> {
           units = selectedUnits.getValue();
           if (units==null)return;
           if (units.size()>1) Log.e(TAG, "* Есть несколько устройств с таким серийником!!!");
           if (units.size() != 0) insertDataToFields(units.get(0));
           if (isRepairDev) sendButton.setText("Отправить данные в БД (ремонт)");
           else sendButton.setText("Отправить данные в БД");
        });


//--------------------------------------------------------------------------------------------------
        serialStateSpinnerList = new ArrayList<>();
        Spinner stateSpinner = (Spinner) view.findViewById(R.id.serial_state_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> serialStateAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, serialStateSpinnerList);
        // Specify the layout to use when the list of choices appears
        serialStateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        stateSpinner.setAdapter(serialStateAdapter);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tState.setText(parent.getItemAtPosition(position).toString());
                if (position == 0) tState.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        MutableLiveData<ArrayList<String>> states = mViewModel.getSerialStatesList();
        states.observe(getViewLifecycleOwner(), s -> {
            Log.e(TAG, "onComplete: "+states.getValue().get(0));
            serialStateSpinnerList.clear();
            serialStateSpinnerList.add(NO_SELECTION);
            serialStateSpinnerList.addAll(states.getValue());
            stateSpinner.setAdapter(serialStateAdapter);
        });
//--------------------------------------------------------------------------------------------------

        repairStateSpinnerList = new ArrayList<>();
        Spinner repairStateSpinner = (Spinner) view.findViewById(R.id.repair_state_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> repairStateAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,  repairStateSpinnerList);
        // Specify the layout to use when the list of choices appears
        repairStateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        repairStateSpinner.setAdapter(repairStateAdapter);
        repairStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tState.setText(parent.getItemAtPosition(position).toString());
                if (position == 0) tState.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        MutableLiveData<ArrayList<String>> states2 = mViewModel.getRepairStatesList();
        states2.observe(getViewLifecycleOwner(), s -> {
            Log.e(TAG, "onComplete: "+states.getValue().get(0));
            repairStateSpinnerList.clear();
            repairStateSpinnerList.add(NO_SELECTION);
            repairStateSpinnerList.addAll(states2.getValue());
            repairStateSpinner.setAdapter(repairStateAdapter);
        });
//--------------------------------------------------------------------------------------------------
        devSpinnerList = new ArrayList<>();
        Spinner devSpinner = (Spinner) view.findViewById(R.id.dev_types_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> devAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,  devSpinnerList);
        // Specify the layout to use when the list of choices appears
        devAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        devSpinner.setAdapter(devAdapter);
        devSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tName.setText(parent.getItemAtPosition(position).toString());
                if (position == 0) tName.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final MutableLiveData<ArrayList<DevType>> devTypes = mViewModel.getDevTypeList();
        devTypes.observe(getViewLifecycleOwner(), s -> {
            this.devTypeList = devTypes.getValue();
            devSpinnerList.clear();
            devSpinnerList.add(NO_SELECTION);
            if (this.devTypeList != null){
                Log.e(TAG, "onCreateView: " + this.devTypeList.size());
                for (int i = 0; i < this.devTypeList.size(); i++) {
                    devSpinnerList.add(devTypeList.get(i).getName());
                }

                devSpinner.setAdapter(devAdapter);
            }
        });
//--------------------------------------------------------------------------------------------------
        /*final MutableLiveData<Boolean> isRepairUnit = mViewModel.getIsRepair();
        isRepairUnit.observe(getViewLifecycleOwner(), isRepair -> {
            if (isRepair) {
                isRepairDev = true;
                tId.setVisibility(View.VISIBLE);
                view.findViewById(R.id.textView6).setVisibility(View.VISIBLE);
                repairStateSpinner.setVisibility(View.VISIBLE);
                stateSpinner.setVisibility(View.GONE);
            } else {
                isRepairDev = false;
                tId.setVisibility(View.GONE);
                view.findViewById(R.id.textView6).setVisibility(View.GONE);
                repairStateSpinner.setVisibility(View.GONE);
                stateSpinner.setVisibility(View.VISIBLE);
            }
        });*/

        return view;
    }

    void insertDataToFields(DUnit unit) {
        tId.setText(unit.getId());
        tName.setText(unit.getName());
        tInnerSerial.setText(unit.getInnerSerial());
        tSerial.setText(unit.getSerial());
        tState.setText(unit.getState());
    }

    private void initialiseDetectorsAndSources() {
        //Toast.makeText(getContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
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
            if (name.equals(REPAIR_UNIT)) {//Если это ремонт
                ////////////////mViewModel.setIsRepair(true);
                tId.setText(innerSerial);
                sendButton.setEnabled(true);
                sendButton.setText("Добавить в БД (ремонт)");
                mViewModel.getRepairUnitById(innerSerial);
            } else {
                ///////////////////mViewModel.setIsRepair(false);
                tName.setText(name);//Если это серия
                tInnerSerial.setText(innerSerial);
                sendButton.setEnabled(true);
                sendButton.setText("Добавить в БД");
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