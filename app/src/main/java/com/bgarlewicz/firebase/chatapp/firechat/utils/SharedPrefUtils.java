package com.bgarlewicz.firebase.chatapp.firechat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bgarlewicz.firebase.chatapp.firechat.R;

public final class SharedPrefUtils {

    public static String getUserName(Context context){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String userKey = context.getString(R.string.shared_pref_username_key);

        return sp.getString(userKey, null);
    }

    public static String getUserToken(Context context){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String tokenKey = context.getString(R.string.shared_pref_token_key);

        return sp.getString(tokenKey, null);
    }

    public static int getTokenState(Context context){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String tokenStateKey = context.getString(R.string.shared_pref_token_state_key);

        return sp.getInt(tokenStateKey, 1);
    }

    public static String getUserUid(Context context){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String userUid = context.getString(R.string.shared_pref_user_uid_key);

        return sp.getString(userUid, null);
    }

    public static void setUserName(Context context, String userName){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String userKey = context.getString(R.string.shared_pref_username_key);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(userKey, userName);
        editor.apply();
    }

    public static void setUserUid(Context context, String userUid){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String userUidKey = context.getString(R.string.shared_pref_user_uid_key);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(userUidKey, userUid);
        editor.apply();
    }

    public static void setUserToken(Context context, String tokenValue){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String tokenKey = context.getString(R.string.shared_pref_token_key);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(tokenKey, tokenValue);
        editor.apply();
    }

    public static void setTokenState(Context context, int tokenStateValue){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String tokenStateKey = context.getString(R.string.shared_pref_token_state_key);

        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(tokenStateKey, tokenStateValue);
        editor.apply();
    }
}
