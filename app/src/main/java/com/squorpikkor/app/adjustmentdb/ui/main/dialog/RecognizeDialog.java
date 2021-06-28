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
import java.util.Collections;
import java.util.Date;
import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.Utils.getRightValue;

public class RecognizeDialog extends BaseDialog{

    SurfaceView mCameraView;
    TextView mSerialText;
    TextView mDevNameText;
    CameraSource mCameraSource;
    Spinner devices;
    Spinner states;
    EditText eSerial;
    ArrayList<String> names;

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
//        ArrayList<String> rightList;
//        if (mViewModel.getSelectedUnit().getValue()!=null && mViewModel.getSelectedUnit().getValue().isRepairUnit())
//            rightList = new ArrayList<>(Objects.requireNonNull(mViewModel.getRepairStatesNames().getValue()));
//        else
//            rightList = new ArrayList<>(Objects.requireNonNull(mViewModel.getSerialStatesNames().getValue()));
//        rightList.add(0, EMPTY_VALUE_TEXT);

//        ArrayList<String> devIdList = new ArrayList<>(Objects.requireNonNull(mViewModel.getDeviceIdList().getValue()));
//        devIdList.add(0, EMPTY_VALUE_TEXT);

        view.findViewById(R.id.cancel_button).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.ok_button).setOnClickListener(v -> saveData(unit));

        setVisibility(unit);




//        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, rightList);
//        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        states.setAdapter(stateAdapter);

//        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, devIdList);
//        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        devices.setAdapter(deviceAdapter);

//        names = new ArrayList<>(mViewModel.getDeviceNameList().getValue());
        ArrayList<String> names = mViewModel.getDeviceNames();


        //сортировка по длине имени в обратном порядке, чтобы сразу искался "6130С" и только потом "6130"
        Collections.sort(names, Collections.reverseOrder());

        return dialog;
    }

    /**Если у юнита уже задано имя устройства, то все поля с имененм скрываем
     * Если у юнита уже задан серийный номер, то все поля с серийным скрываем
     * Если у юнита уже задан и номер, и имя, то скрываем все поля И ОКНО КАМЕРЫ */
    void setVisibility(DUnit unit) {
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
            view.findViewById(R.id.newName).setVisibility(View.VISIBLE);
            view.findViewById(R.id.name_already_recognized).setVisibility(View.GONE);
        } else {
            mDevNameText.setVisibility(View.GONE);
            view.findViewById(R.id.dialogNewNameLabel).setVisibility(View.GONE);
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

    //todo надо сделать не через создание нового юнита, а через присваивание параметров уже существующему юниту, затем этот старый юнит сохранять
    //todo Дублирование кода (чуть различаются) — SelectStateDialogSingle
    /**Сохранение юнита с выбранными параметрами. Логика: если имя и/или серийный не выбраны, то
     * юниту будут присвоены распознанные значения. Если параметры выбрать руками, то распознанные
     * значения будут проигнорированы, юнит будет сохранен с параметрами выбранными вручную*/
    private void saveData(DUnit unit) {
        if (unit != null) {
            String id = unit.getId();
            String innerSerial = unit.getInnerSerial();
            String state = "";
            String state_id = "";
            if (states.getSelectedItem() != null) state = states.getSelectedItem().toString();

//            if (!state.equals(EMPTY_VALUE_TEXT)) {
//                if (unit.isRepairUnit()
//                        && mViewModel.getRepairStatesNames().getValue()!=null
//                        && mViewModel.getRepairStateIdList().getValue()!=null) {
//                    state_id = getIdByName(state, mViewModel.getRepairStatesNames().getValue(), mViewModel.getRepairStateIdList().getValue());
//                }
//                if (unit.isSerialUnit()
//                        && mViewModel.getSerialStatesNames().getValue()!=null
//                        && mViewModel.getSerialStateIdList().getValue()!=null) {
//                    state_id = getIdByName(state, mViewModel.getSerialStatesNames().getValue(), mViewModel.getSerialStateIdList().getValue());
//                }
//            } else {
//                state_id = "";
//            }

//            String name = devices.getSelectedItem().toString().equals(EMPTY_VALUE_TEXT)?mDevNameText.getText().toString():devices.getSelectedItem().toString();
//            name = getRightValue(unit.getName(), name);
//            name = getIdByName(name, mViewModel.getDeviceNameList().getValue(), mViewModel.getDeviceIdList().getValue());

            String serial = eSerial.getText().toString().equals("")?mSerialText.getText().toString():eSerial.getText().toString();
            serial = getRightValue(unit.getSerial(), serial);

            String desc = "";
            String type = unit.getType();
            String location = mViewModel.getLocation_id().getValue();
            Date date = unit.getDate();
//            mViewModel.saveDUnitToDB(new DUnit(id, name, innerSerial, serial, state_id, desc, type, location, date), unit.getState());
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
                if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    String getDevName_old(TextBlock item) {

        String text = item.getValue();
        //todo здесь потом будет ссылка на getDeviceNameLit
        ArrayList<String> names = new ArrayList<>();

        Log.e(TAG, "♦getDevName: "+text);

        if (text.contains("AT6130C")) return "AT6130C";
        else if (text.contains("AT6130")) return "AT6130";
        else if (text.contains("USB-DU")) return "USB-DU";
        else if (text.contains("PU2")) return "PU2";
        else if (text.contains("BDKG-05")) return "BDKG-05";
        else if (text.contains("5AKT-04")) return "BDKG-04";
        else if (text.contains("5AKT-01")) return "BDKG-01";
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

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(requireActivity(), textRecognizer)
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
