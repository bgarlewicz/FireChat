package com.bgarlewicz.firebase.chatapp.firechat.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;

import com.bgarlewicz.firebase.chatapp.firechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ChangeNameDialogFragment extends DialogFragment {

    public static final String TAG = ChangeNameDialogFragment.class.getSimpleName();
    OnNameChangedListener mNameChanged;
    int mColorId;

    public interface OnNameChangedListener{
        void onNameChanged(String changedName);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_change_name, null))
        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText usernameET = (EditText) getDialog().findViewById(R.id.usernameDialogET);
                final String newUsername = usernameET.getText().toString();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newUsername)
                        .build();

                user.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            mNameChanged.onNameChanged(newUsername);
                        }
                    }
            });
            }
        }).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getDialog().cancel();
            }
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Button positiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(mColorId);
        Button negativeButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(mColorId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    private void onAttachToContext(Context context) {
        mColorId = getColorFromAttr(context, R.attr.colorPrimaryDark);
        try{
            mNameChanged = (OnNameChangedListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException();
        }
    }

    private int getColorFromAttr(Context context, int colorAttrId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(colorAttrId, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }
}
