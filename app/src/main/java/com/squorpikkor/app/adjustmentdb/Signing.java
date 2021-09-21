package com.squorpikkor.app.adjustmentdb;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squorpikkor.app.adjustmentdb.ui.main.DrawableTask;

import java.util.Arrays;
import java.util.List;

public class Signing {

    private static final int RC_SIGN_IN = 200;
    Activity activity;

    public Signing(Activity activity) {
        this.activity = activity;
    }

    public void signing() {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build());
            // Create and launch sign-in intent
            activity.startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
    }

    /*class Result extends Activity{

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            Log.e(TAG, "onActivityResult: requestCode1 = "+requestCode);
            if (requestCode == RC_SIGN_IN) {
                IdpResponse response = IdpResponse.fromResultIntent(data);
                Log.e(TAG, "onActivityResult: resultCode = "+resultCode);
                if (resultCode == RESULT_OK) {
                    Log.e(TAG, "onActivityResult: OK");
//                    mViewModel.checkUserEmail(response.getEmail());
                } else {
                    Log.e(TAG, "******************************onActivityResult: NOT OK");
                }
            }
        }

    }*/
}
