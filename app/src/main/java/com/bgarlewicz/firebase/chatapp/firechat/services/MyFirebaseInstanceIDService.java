package com.bgarlewicz.firebase.chatapp.firechat.services;

import com.bgarlewicz.firebase.chatapp.firechat.utils.SharedPrefUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        saveRegistration(refreshedToken);
    }

    private void saveRegistration(final String refreshedToken) {

        SharedPrefUtils.setUserToken(this, refreshedToken);
        SharedPrefUtils.setTokenState(this, 1);
    }
}
