package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import androidx.annotation.NonNull;
import com.squorpikkor.app.adjustmentdb.R;

public class ExitAskDialog  extends Dialog {
    private final Activity activity;

    public ExitAskDialog(@NonNull Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_ask_exit);

        Button cancelButton = findViewById(R.id.cancel_button);
        Button okButton = findViewById(R.id.ok_button);

        cancelButton.setOnClickListener(view -> dismiss());

        okButton.setOnClickListener(view -> {
            activity.finish();
            dismiss();
        });
    }
}
