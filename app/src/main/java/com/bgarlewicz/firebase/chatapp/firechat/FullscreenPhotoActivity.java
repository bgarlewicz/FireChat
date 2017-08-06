package com.bgarlewicz.firebase.chatapp.firechat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.bgarlewicz.firebase.chatapp.firechat.utils.PhotoUtils;
import com.bgarlewicz.firebase.chatapp.firechat.utils.SharedPrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullscreenPhotoActivity extends AppCompatActivity {

    public static final String PHOTO_URI = "photo_uri_key";
    @BindView(R.id.fullscreen_image_view)
    ImageView imageView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefUtils.useTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_photo);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();
        String photoUri = bundle.getString(PHOTO_URI, null);
        Log.d("TAGTAGATAT", photoUri);

        if(photoUri != null){
            PhotoUtils.setImageWithGlide(imageView, photoUri);
        }
    }
}
