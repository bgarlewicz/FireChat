package com.bgarlewicz.firebase.chatapp.firechat.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.Toast;

import com.bgarlewicz.firebase.chatapp.firechat.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

public class PhotoUtils {
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 654;
    public static final int RC_PHOTO_PICKER = 432;

    public static Intent pickPhoto(Context context, Activity activity){
        if(ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_EXTERNAL_STORAGE);
            return null;
        } else{
            return launchImagePicker();
        }
    }

    private static Intent launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        return intent;
    }

    public static void onRequestPermission(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults, Context context){
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchImagePicker();
                } else {
                    Toast.makeText(context, context.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    public static void setImageWithGlide(final ImageView imageView, String photoUrl, int width, int height) {
        Glide.with(imageView.getContext())
                .load(photoUrl)
                .asBitmap()
                .override(width, height)
                .centerCrop()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        imageView.setImageBitmap(resource);
                    }
                });
    }

    public static void setImageWithGlide(final ImageView imageView, String photoUrl) {
        Glide.with(imageView.getContext())
                .load(Uri.parse(photoUrl))
                .asBitmap()
                .centerCrop()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        imageView.setImageBitmap(resource);
                    }
                });
    }
}
