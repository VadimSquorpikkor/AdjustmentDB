package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EMPTY_VALUE_TEXT;

/**Диалог распознает серийные номера и имя устройства ("БДКГ-02"). Имена распознает на совпадение
 * со списком устройств (MutableLiveData<ArrayList<Device>> devices), поэтому при добавлении новых
 * устройств в БД нет необходимости что-то дописывать в коде, RecognizeDialog автоматом будет
 * получать самую свежую версию списка устройств при каждом добавлении нового устройства в БД*/
public class RecognizeDialog extends BaseDialog{

    private SurfaceView mCameraView;
    private TextView mSerialText;
    private TextView mDevNameText;
    private CameraSource mCameraSource;

    private SpinnerAdapter deviceSpinnerAdapter;
    private SpinnerAdapter stateSpinnerAdapter;
    private SpinnerAdapter employeeSpinnerAdapter;
    private SpinnerAdapter deviceSetSpinnerAdapter;

    EditText eSerial;
    ArrayList<String> names;

    String location;

    boolean nameIsEmpty = true;
    boolean serialIsEmpty = true;

    private static final int requestPermissionID = 101;

    public RecognizeDialog() {
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeWithVM(R.layout.dialog_recognize_device);

        location = mViewModel.getLocation_id().getValue();
        DUnit unit = mViewModel.getSelectedUnit().getValue();
        String unitType = unit==null?null:unit.getType();

        mCameraView = view.findViewById(R.id.surface_for_recognize);
        mSerialText = view.findViewById(R.id.recognized_serial);
        mDevNameText = view.findViewById(R.id.recognized_name);
        eSerial = view.findViewById(R.id.dSerial);

        mSerialText.setOnClickListener(v -> mSerialText.setText(""));
        mDevNameText.setOnClickListener(v -> mDevNameText.setText(""));

        Spinner deviceSetSpinner = view.findViewById(R.id.spinnerDevSetName);
        Spinner deviceSpinner = view.findViewById(R.id.newName);
        Spinner stateSpinner = view.findViewById(R.id.state_spinner);
        Spinner employeeSpinner = view.findViewById(R.id.employee_spinner);

        deviceSetSpinnerAdapter = new SpinnerAdapter(deviceSetSpinner, mContext);
        deviceSpinnerAdapter = new SpinnerAdapter(deviceSpinner, mContext);
        stateSpinnerAdapter = new SpinnerAdapter(stateSpinner, mContext);
        employeeSpinnerAdapter = new SpinnerAdapter(employeeSpinner, mContext);

        mViewModel.getDeviceSets().observe(this, deviceSetSpinnerAdapter::setData);
        mViewModel.getDevices().observe(this, list1 -> {
            deviceSpinnerAdapter.setDataByDevSet(list1, deviceSetSpinnerAdapter.getSelectedNameId(), EMPTY_VALUE_TEXT);
            names = mViewModel.getDeviceNamesRuAndEn();
            //сортировка по длине имени в обратном порядке, чтобы сразу искался "6130С" и только потом "6130"
            Collections.sort(names, Collections.reverseOrder());
        });
        mViewModel.getStates().observe(this, list -> stateSpinnerAdapter.setDataByTypeAndLocation(list, unitType, location, EMPTY_VALUE_TEXT));
        mViewModel.getEmployees().observe(this, list -> employeeSpinnerAdapter.setData(list, EMPTY_VALUE_TEXT));

        deviceSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {updateDeviceSpinner();}
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        startCameraSource();

        view.findViewById(R.id.cancel_button).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.ok_button).setOnClickListener(v -> saveData(unit));

        setVisibility(unit);

        return dialog;
    }

    private void updateDeviceSpinner() {
        deviceSpinnerAdapter.setDataByDevSet(mViewModel.getDevices().getValue(), deviceSetSpinnerAdapter.getSelectedNameId(), EMPTY_VALUE_TEXT);
    }

    /**Если у юнита уже задано имя устройства, то все поля с имененм скрываем
     * Если у юнита уже задан серийный номер, то все поля с серийным скрываем
     * Если у юнита уже задан и номер, и имя, то скрываем все поля И ОКНО КАМЕРЫ */
    void setVisibility(DUnit unit) {
        if (unit==null) return;
        nameIsEmpty = unit.getName().equals("");
        serialIsEmpty = unit.getSerial().equals("");
        if (!nameIsEmpty && !serialIsEmpty) {
            view.findViewById(R.id.surface_layout).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.surface_layout).setVisibility(View.VISIBLE);
        }
        if (nameIsEmpty){
            mDevNameText.setVisibility(View.VISIBLE);
            view.findViewById(R.id.dialogNewNameLabel).setVisibility(View.VISIBLE);
            view.findViewById(R.id.dialogSetNameLabel).setVisibility(View.VISIBLE);
            view.findViewById(R.id.newName).setVisibility(View.VISIBLE);
            view.findViewById(R.id.name_already_recognized).setVisibility(View.GONE);
        } else {
            mDevNameText.setVisibility(View.GONE);
            view.findViewById(R.id.dialogNewNameLabel).setVisibility(View.GONE);
            view.findViewById(R.id.dialogSetNameLabel).setVisibility(View.GONE);
            view.findViewById(R.id.newName).setVisibility(View.GONE);
            view.findViewById(R.id.name_already_recognized).setVisibility(View.VISIBLE);
        }
        if (serialIsEmpty){
            mSerialText.setVisibility(View.VISIBLE);
            view.findViewById(R.id.dialogSerialLabel).setVisibility(View.VISIBLE);
            view.findViewById(R.id.dSerial).setVisibility(View.VISIBLE);
            view.findViewById(R.id.serial_already_recognized).setVisibility(View.GONE);
        } else {
            mSerialText.setVisibility(View.GONE);
            view.findViewById(R.id.dialogSerialLabel).setVisibility(View.GONE);
            view.findViewById(R.id.dSerial).setVisibility(View.GONE);
            view.findViewById(R.id.serial_already_recognized).setVisibility(View.VISIBLE);
        }

    }

    private void saveData(DUnit unit) {
        updateUnitData(unit);
        mViewModel.saveUnitAndEvent(unit);
        dismiss();
    }

    /**Сохранение юнита с выбранными параметрами. Логика: если имя и/или серийный не выбраны, то
     * юниту будут присвоены распознанные значения. Если параметры выбрать руками, то распознанные
     * значения будут проигнорированы, юнит будет сохранен с параметрами выбранными вручную*/
    private void updateUnitData(DUnit unit) {
        String name_id = mViewModel.getDeviceNameId(mDevNameText.getText().toString());//получить id из имени
        String newNameId = deviceSpinnerAdapter.getSelectedNameId().equals(ANY_VALUE)?name_id:deviceSpinnerAdapter.getSelectedNameId();
        String newSerial = eSerial.getText().toString().equals("")?mSerialText.getText().toString():eSerial.getText().toString();

        String newStateId = stateSpinnerAdapter.getSelectedNameId();
        String employee = employeeSpinnerAdapter.getSelectedNameId();
        String description = "";

        if (unit.getName()==null||unit.getName().equals("") && !newNameId.equals(ANY_VALUE)) unit.setName(newNameId);
        if (unit.getSerial()==null||unit.getSerial().equals("") && !newSerial.equals("")) unit.setSerial(newSerial);
        if (unit.getDate()==null) unit.setDate(new Date());
        if (!newStateId.equals(ANY_VALUE)) unit.addNewEvent(mViewModel, newStateId, description, location);
        if (!employee.equals(ANY_VALUE)) unit.setEmployee(employee);
    }

    String cutSerialString(String text, String mask) {
        String s = text.substring(text.indexOf(mask));
        s = s.replace(mask, "");
        if (s.startsWith(" ")) s = s.replaceFirst(" ", "");
        s = s.split(" ")[0];
        s = s.trim();
        return s;
    }

    void recognize(TextBlock item) {
        if (serialIsEmpty && mSerialText.getText().length() == 0) {
            String serial = getSerial(item);
            if (!serial.equals("")) mSerialText.setText(serial);
        }
        if (nameIsEmpty && mDevNameText.getText().length() == 0) {
            String devName = getDevName(item);
            if (!devName.equals("")) mDevNameText.setText(devName);
        }
    }

    String getSerial(TextBlock item) {
        String text = item.getComponents().get(0).getValue();
        if (text.contains("Serial")) {
                 if (text.contains("Serial Na")) return cutSerialString(text, "Serial Na");
            else if (text.contains("Serial No")) return cutSerialString(text, "Serial No");
            else if (text.contains("Serial Ne")) return cutSerialString(text, "Serial Ne");
            else if (text.contains("Serial N")) return cutSerialString(text, "Serial N");
            else if (text.contains("Serial. N")) return cutSerialString(text, "Serial. N");
            else if (text.contains("Serial.N")) return cutSerialString(text, "Serial.N");
            else if (text.contains("SerialN")) return cutSerialString(text, "SerialN");
            else return "";
        } else if (text.contains("3aB")) {
                 if (text.contains("3aB. No")) return cutSerialString(text, "3aB. No");
            else if (text.contains("3aB. Ne")) return cutSerialString(text, "3aB. Ne");
            else if (text.contains("3aB. Na")) return cutSerialString(text, "3aB. Na");
            else return "";
        }
        else return "";
    }

    /**Сделано для распознавания кирилических символов (само распознавание различает только латиницу,
     * поэтому приходится извращаться). Если распознавание вернуло "5AKT", это значит, что на самом
     * деле на наклейке было написано "БДКГ"*/
    String getRightNameByMask(String recognizedText, String mask, String rightName) {
        String nameDef = "";
        for (String name : names) {
            String newName = name.replace(rightName, mask);//BDKG-01 -> 5AKT-01
            if (recognizedText.contains(newName)){
                nameDef = name;
                break;
            }
        }
        return nameDef;
    }

    /**Чтобы сократить время на поиск имени, проверяем, может оно (имя) вообще не похоже на имя*/
    boolean isWrongName(String name) {
        return name.length()<6;
    }

    String getDevName(TextBlock item) {
        String nameDef = "";
        String text = item.getValue();
        if (isWrongName(text)) return "";
        Log.e(TAG, "♦ getDevName: "+text);

        if (text.contains("5AKT")) nameDef = getRightNameByMask(text, "5AKT", "BDKG");
        else if (text.contains("5AKr")) nameDef = getRightNameByMask(text, "5AKr", "BDKG");
        else if (text.contains("6AKE")) nameDef = getRightNameByMask(text, "6AKE", "BDKG");
        else if (text.contains("5AKE")) nameDef = getRightNameByMask(text, "5AKE", "BDKG");
        else if (text.contains("5AKH")) nameDef = getRightNameByMask(text, "5AKH", "BDKN");
        else {
            for (String name : names) {
                if (text.contains(name)){
                    nameDef = name;
                    break;
                }
            }
        }
        return nameDef;
    }

    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(requireActivity()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            //Initialize camera source to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(requireActivity(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();

            /*
              Add call back to SurfaceView and check if camera permission is granted.
              If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(requireActivity(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(requireActivity(),
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
                public void receiveDetections(@NotNull Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0 ){

                        mSerialText.post(() -> {
                            for(int i=0;i<items.size();i++){
                                TextBlock item = items.valueAt(i);
                                recognize(item);
                            }
                        });
                    }
                }
            });
        }
    }
}
