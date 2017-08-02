package com.bgarlewicz.firebase.chatapp.firechat;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.bgarlewicz.firebase.chatapp.firechat.utils.SharedPrefUtils;

public class SettingsActivity extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefUtils.useTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

}
