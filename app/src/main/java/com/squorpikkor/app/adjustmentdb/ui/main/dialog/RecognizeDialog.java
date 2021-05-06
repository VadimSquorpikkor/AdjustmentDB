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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import java.util.Date;
import java.util.Objects;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.Utils.getIdByName;
import static com.squorpikkor.app.adjustmentdb.Utils.getRightValue;

public class RecognizeDialog extends BaseDialog{

    SurfaceView mCameraView;
    TextView mSerialText;
    TextView mDevNameText;
    CameraSource mCameraSource;
    Spinner devices;
    Spinner states;
    EditText eSerial;

    boolean nameIsEmpty;
    boolean serialIsEmpty;

    private static final int requestPermissionID = 101;

    public RecognizeDialog() {
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeWithVM(R.layout.dialog_recognize_device);

        mCameraView = view.findViewById(R.id.surface_for_recognize);
        mSerialText = view.findViewById(R.id.recognized_serial);
        mDevNameText = view.findViewById(R.id.recognized_name);
        eSerial = view.findViewById(R.id.dSerial);

        mSerialText.setOnClickListener(v -> mSerialText.setText(""));
        mDevNameText.setOnClickListener(v -> mDevNameText.setText(""));

        devices = view.findViewById(R.id.newName);
        states = view.findViewById(R.id.state_spinner);

        startCameraSource();

        DUnit unit = mViewModel.getSelectedUnit().getValue();

        //Загружается тот список, тип прибора который загружен — ремонт или серия
        ArrayList<String> rightList;
        if (Objects.requireNonNull(mViewModel.getSelectedUnit().getValue()).isRepairUnit())
            rightList = new ArrayList<>(Objects.requireNonNull(mViewModel.getRepairStatesNames().getValue()));
        else
            rightList = new ArrayList<>(Objects.requireNonNull(mViewModel.getSerialStatesNames().getValue()));
        rightList.add(0, "");

        ArrayList<String> devIdList = new ArrayList<>(Objects.requireNonNull(mViewModel.getDeviceIdList().getValue()));
        devIdList.add(0, "");

        view.findViewById(R.id.cancel_button).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.ok_button).setOnClickListener(v -> saveData(unit));

        if (unit!=null)setVisibility(unit);

        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, rightList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        states.setAdapter(stateAdapter);

        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, devIdList);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devices.setAdapter(deviceAdapter);

        return dialog;
    }

    /**Если у юнита уже задано имя устройства, то все поля с имененм скрываем
     * Если у юнита уже задан серийный номер, то все поля с серийным скрываем*/
    void setVisibility(DUnit unit) {
        nameIsEmpty = unit.getName().equals("");
        serialIsEmpty = unit.getSerial().equals("");
        if (nameIsEmpty){
            mDevNameText.setVisibility(View.VISIBLE);
            view.findViewById(R.id.dialogNewNameLabel).setVisibility(View.VISIBLE);
            view.findViewById(R.id.newName).setVisibility(View.VISIBLE);
        } else {
            mDevNameText.setVisibility(View.GONE);
            view.findViewById(R.id.dialogNewNameLabel).setVisibility(View.GONE);
            view.findViewById(R.id.newName).setVisibility(View.GONE);
        }
        if (serialIsEmpty){
            mSerialText.setVisibility(View.VISIBLE);
            view.findViewById(R.id.dialogSerialLabel).setVisibility(View.VISIBLE);
            view.findViewById(R.id.dSerial).setVisibility(View.VISIBLE);
        } else {
            mSerialText.setVisibility(View.GONE);
            view.findViewById(R.id.dialogSerialLabel).setVisibility(View.GONE);
            view.findViewById(R.id.dSerial).setVisibility(View.GONE);
        }

    }

    //todo надо сделать не через создание нового юнита, а через присваивание параметров уже существующему юниту, затем этот старый юнит сохранять
    //todo Дублирование кода (чуть различаются) — SelectStateDialogSingle
    /**Сохранение юнита с выбранными параметрами. Логика: если имя и/или серийный не выбраны, то
     * юниту будут присвоены распознанные значения. Если параметры выбрать руками, то распознанные
     * значения будут проигнорированы, юнит будет сохранен с параметрами выбранными вручную*/
    private void saveData(DUnit unit) {
        if (unit != null) {
            String id = unit.getId();
            //отличается здесь
            String name = devices.getSelectedItem().toString().equals("")?mDevNameText.getText().toString():getRightValue(unit.getName(), devices.getSelectedItem().toString());
            String innerSerial = unit.getInnerSerial();
            //отличается здесь
            String serial = eSerial.getText().toString().equals("")?mSerialText.getText().toString():getRightValue(unit.getSerial(), eSerial.getText().toString());
            String state = "";
            String state_id = "";
            if (states.getSelectedItem() != null) state = states.getSelectedItem().toString();
            if (!state.equals("")) {
                if (unit.isRepairUnit()
                        && mViewModel.getRepairStatesNames().getValue()!=null
                        && mViewModel.getRepairStateIdList().getValue()!=null) {
                    state_id = getIdByName(state, mViewModel.getRepairStatesNames().getValue(), mViewModel.getRepairStateIdList().getValue());
                }
                if (unit.isSerialUnit()
                        && mViewModel.getSerialStatesNames().getValue()!=null
                        && mViewModel.getSerialStateIdList().getValue()!=null) {
                    state_id = getIdByName(state, mViewModel.getSerialStatesNames().getValue(), mViewModel.getSerialStateIdList().getValue());
                }
            } else {
                state_id = "";
            }

            //отличается здесь
            String desc = "";
            String type = unit.getType();
            String location = mViewModel.getLocation_id().getValue();
            Date date = unit.getDate();
            mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, state_id, desc, type, location, date), unit.getState());
        }
        dismiss();
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
                if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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

        String text = item.getValue();
        //todo здесь потом будет ссылка на getDeviceNameLit
        ArrayList<String> names = new ArrayList<>();

        Log.e(TAG, "♦getDevName: "+text);

        if (text.contains("AT6130C")) return "AT6130C";
        else if (text.contains("AT6130")) return "AT6130";
        else if (text.contains("USB-DU")) return "USB-DU";
        else if (text.contains("PU2")) return "PU2";
        else if (text.contains("BDKG-05")) return "BDKG-05";
        else return "";
    }

    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(Objects.requireNonNull(getActivity())).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(Objects.requireNonNull(getActivity()), textRecognizer)
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

                        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
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
