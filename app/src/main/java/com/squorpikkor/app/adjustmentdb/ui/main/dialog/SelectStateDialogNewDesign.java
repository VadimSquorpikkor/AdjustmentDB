package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.ui.main.adapter.DialogStatesAdapter;

import java.util.ArrayList;

public class SelectStateDialogNewDesign extends DialogFragment {

    private Context mContext;
    private AlertDialog dialog;
    private View view;
    private int address;


    private ArrayList<String> stateList;
    private TextView cancelButton;
    private TextView okButton;
    private ListView listViewState;
    DialogStatesAdapter sourceAdapter;
    EditText selectedEditState;
    DUnit unit;

    public static SelectStateDialogNewDesign newInstance() {
        return new SelectStateDialogNewDesign();
    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = new AlertDialog.Builder(mContext).create();
        //set background for the dialog
        Window window = dialog.getWindow();
        if (window != null) window.setBackgroundDrawableResource(R.drawable.main_gradient);
        view = requireActivity().getLayoutInflater()
                .inflate(R.layout.dialog_select_states_new, null);

        //EditText gain = view.findViewById(R.id.gain_code);

        dialog.setView(view, 0, 0, 0, 0);

//        ArrayList<DUnit> units = CommunicationService.dUnits;
//        GainCodeAdapter adapter = new GainCodeAdapter(mContext, R.layout.dialog_gain_code_item, units);
//        ListView lvItems = view.findViewById(R.id.lv_items);
//        lvItems.setAdapter(adapter);



//        view.findViewById(R.id.button_cancel).setOnClickListener(v -> dismiss());
//        view.findViewById(R.id.button_ok).setOnClickListener(v -> setGainCode());
//        lvItems.setOnItemClickListener((parent, view, position, id) -> {
//            address = units.get(position).getPosition();
//            gain.setText(String.valueOf(units.get(position).getGainCode()));
//            selectedNum.setImageResource(getImage(address));

//            for (int i = 0; i < lvItems.getChildCount(); i++) {
//                if (position == i && lvItems.getChildCount() > 1)
//                    lvItems.getChildAt(i).setBackgroundColor(Color.DKGRAY);
//                else lvItems.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
//            }
//        });
        return dialog;
    }


    void setGainCode() {

    }

}
