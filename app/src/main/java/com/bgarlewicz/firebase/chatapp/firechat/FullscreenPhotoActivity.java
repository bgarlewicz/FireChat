package com.bgarlewicz.firebase.chatapp.firechat;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullscreenPhotoActivity extends AppCompatActivity {

    @BindView(R.id.fullscreen_image_view)
    ImageView imageView;

    public static final String PHOTO_URI = "photo_uri_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_photo);

        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        String photoUri = bundle.getString(PHOTO_URI, null);

        if(photoUri != null){
            Glide.with(this)
                    .load(Uri.parse(photoUri))
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            imageView.setImageBitmap(resource);
                        }
                    });
        }
    }
}
