package com.bgarlewicz.firebase.chatapp.firechat.services;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.bgarlewicz.firebase.chatapp.firechat.MessageActivity;
import com.bgarlewicz.firebase.chatapp.firechat.R;

/**
 * Created by Bartosz on 23.07.2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = MessageActivity.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        saveRegistration(refreshedToken);
    }

    private void saveRegistration(final String refreshedToken) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.shared_pref_token_key), refreshedToken);
        editor.putInt(getString(R.string.shared_pref_token_state_key), 1);
        editor.apply();


    }
}
