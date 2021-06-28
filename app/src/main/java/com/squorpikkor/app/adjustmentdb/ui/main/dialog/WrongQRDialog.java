package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.squorpikkor.app.adjustmentdb.R;

public class WrongQRDialog  extends Dialog {

    public WrongQRDialog(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_wrong_qr);
        Button okButton = findViewById(R.id.ok_button);
        okButton.setOnClickListener(view -> dismiss());
    }
}
