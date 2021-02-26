package com.squorpikkor.app.adjustmentdb.ui.main;

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
import java.util.ArrayList;
import java.util.Objects;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class ScannerFragment extends Fragment {

    //todo Разделить на два фрагмента: QR-сканер и отображение и редактирование данных. А лучше —
    // фрагмент сканнера слелать внутри фрагмента данных, кнопка "Добавить в БД" так останется внизу
    // (ну и где это будет нужно)

    private MainViewModel mViewModel;

    EditText tName;
    EditText tInnerSerial;
    EditText tSerial;
    EditText tState;

    TextView txtBarcodeValue;
    SurfaceView surfaceView;
    Button sendButton;

    boolean state;

    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String intentData = "";
    private static final String SPLIT_SYMBOL = " ";

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

        tName = view.findViewById(R.id.editTextName);
        tInnerSerial = view.findViewById(R.id.editTextInnerSerial);
        tSerial = view.findViewById(R.id.editTextSerial);
        tState = view.findViewById(R.id.editTextState);

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
            String name = tName.getText().toString();
            String innerSerial = tInnerSerial.getText().toString();
            String serial = tSerial.getText().toString();
            String state = tState.getText().toString();
            mViewModel.saveDUnitToDB(new DUnit(name, innerSerial, serial, state));
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStackImmediate();
            }
        });

        /**Кнопка включения/включения режима редактирования для полей EditText*/
        view.findViewById(R.id.actionButtonEdit).setOnClickListener(v -> {
            state = !state;
            tName.setEnabled(state);
            tInnerSerial.setEnabled(state);
            tSerial.setEnabled(state);
            tState.setEnabled(state);
        });

        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.state_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tState.setText(parent.getItemAtPosition(position).toString());
                if (position == 0) tState.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        units = new ArrayList<>();

        final MutableLiveData<ArrayList<DUnit>> selectedUnits = mViewModel.getSelectedUnits();
        selectedUnits.observe(getViewLifecycleOwner(), s -> {
           units = selectedUnits.getValue();
           if (units==null)return;
           if (units.size()>1) Log.e(TAG, "* Есть несколько устройств с таким серийником!!!");
           if (units.size() != 0) insertDataToFields(units.get(0));

        });


        return view;
    }

    void insertDataToFields(DUnit unit) {
        tName.setText(unit.getName());
        tInnerSerial.setText(unit.getInnerSerial());
        tSerial.setText(unit.getSerial());
        tState.setText(unit.getState());
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
                        Log.e(TAG, "******decode******* " + decodeMe(intentData));

                        String[] ar = intentData.split(SPLIT_SYMBOL);
                        if (ar.length == 2) {
                            String name = ar[0];
                            String innerSerial = ar[1];
                            tName.setText(name);
                            tInnerSerial.setText(innerSerial);
                            sendButton.setEnabled(true);

                            mViewModel.getDUnitByNameAndInnerSerial(name, innerSerial);

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