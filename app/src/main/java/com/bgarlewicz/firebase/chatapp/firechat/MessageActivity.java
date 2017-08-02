package com.bgarlewicz.firebase.chatapp.firechat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bgarlewicz.firebase.chatapp.firechat.utils.PhotoUtils;
import com.bgarlewicz.firebase.chatapp.firechat.utils.SharedPrefUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.auth.BuildConfig;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;

import static com.bgarlewicz.firebase.chatapp.firechat.utils.PhotoUtils.RC_PHOTO_PICKER;

public class MessageActivity extends AppCompatActivity {

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 160;

    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.messageListView) ListView mMessageListView;
    @BindView(R.id.photoPickerButton) ImageButton mPhotoPickerButton;
    @BindView(R.id.messageEditText) EditText mMessageEditText;
    @BindView(R.id.sendButton) Button mSendButton;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindDrawable(R.drawable.msg_logged_user_background) Drawable msgLoggedUserBackground;
    @BindDrawable(R.drawable.msg_others_background) Drawable msgOtherUserBackground;

    @BindString(R.string.msg_length_limit) String FRIENDLY_MESSAGE_LENGTH_KEY;
    @BindString(R.string.firebase_messages_child) String firebaseMessageChild;
    @BindString(R.string.firebase_chat_photos_child) String firebaseChatPhotosChild;
    @BindString(R.string.permission_denied) String permissionDenied;
    @BindString(R.string.choose_picture) String choosePicture;
    @BindString(R.string.user_uid_extra) String receiverUidExtra;
    @BindString(R.string.user_name_extra) String receiverNameExtra;

    private FirebaseListAdapter mMessageAdapter;

    private String mUsername;
    private String mUserUid;
    private String receiverUsername;
    private String receiverUserUid;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefUtils.useTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsername = SharedPrefUtils.getUserName(this);
        mUserUid = SharedPrefUtils.getUserUid(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            receiverUsername = bundle.getString(receiverNameExtra);
            receiverUserUid = bundle.getString(receiverUidExtra);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(firebaseMessageChild).child(getChatRoom(mUserUid, receiverUserUid));
        mFirebaseStorage = FirebaseStorage.getInstance();
        mPhotosStorageReference = mFirebaseStorage.getReference().child(firebaseChatPhotosChild);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mMessageAdapter = getFirebaseListAdapter();
        mMessageListView.setAdapter(mMessageAdapter);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        setMessageLength();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMessageAdapter.cleanup();
    }

    @OnClick(R.id.photoPickerButton)
    public void pickPhoto() {
        Intent intent = PhotoUtils.pickPhoto(this, this);
        if (intent != null){
            startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)), RC_PHOTO_PICKER);
        }
    }

    @OnClick(R.id.sendButton)
    public void sendMessage(){
        FirechatMessage firechatMessage = new FirechatMessage(mMessageEditText.getText().toString(),
                                                                mUsername,
                                                                mUserUid,
                                                                receiverUsername,
                                                                receiverUserUid,
                                                                null,
                                                                Calendar.getInstance().getTime().getTime());

        mMessagesDatabaseReference.push().setValue(firechatMessage);


        mMessageEditText.setText("");
    }

    private String getChatRoom(String firstUid, String secondUid) {
        String chatRoom;

        if (firstUid.compareTo(secondUid)>0){
            chatRoom = firstUid+secondUid;
        } else {
            chatRoom = secondUid+firstUid;
        }

        return chatRoom;
    }

    @OnTextChanged(value = R.id.messageEditText)
    public void afterTextChanged(CharSequence charSequence){
        if (charSequence.toString().trim().length() > 0) {
            mSendButton.setEnabled(true);
        } else {
            mSendButton.setEnabled(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PhotoUtils.onRequestPermission(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
            Uri selectedPhotoUri = data.getData();
            StorageReference storageReference = mPhotosStorageReference.child(selectedPhotoUri.getLastPathSegment());
            UploadTask uploadTask = storageReference.putFile(selectedPhotoUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    if (downloadUri != null) {
                        FirechatMessage firechatMessage = new FirechatMessage(null,
                                mUsername,
                                mUserUid,
                                receiverUsername,
                                receiverUserUid,
                                downloadUri.toString(),
                                Calendar.getInstance().getTime().getTime());
                        mMessagesDatabaseReference.push().setValue(firechatMessage);
                    }
                }
            });
        }
    }

    @NonNull
    private FirebaseListAdapter<FirechatMessage> getFirebaseListAdapter() {
        return new FirebaseListAdapter<FirechatMessage>(this, FirechatMessage.class, R.layout.item_message, mMessagesDatabaseReference) {
            @Override
            public View getView(int position, View view, ViewGroup viewGroup) {

                FirechatMessage message = getItem(position);

                if (mUserUid.equals(message.getSenderUid())){
                    View newView = getLayoutInflater().inflate(R.layout.item_message_mine, viewGroup, false);
                    populateView(newView, message, position);
                    return newView;

                } else {
                    return super.getView(position, null, viewGroup);
                }
            }

            @Override
            protected void populateView(View view, FirechatMessage firechatMessages, int position) {

                final ImageView photoImageView = (ImageView) view.findViewById(R.id.photoImageView);
                TextView messageTextView = (TextView) view.findViewById(R.id.messageTextView);
                TextView authorTextView = (TextView) view.findViewById(R.id.nameTextView);

                FirechatMessage message = getItem(position);

                String previousSender;
                if (position == 0){
                    previousSender = mUserUid;
                } else {
                    previousSender = getItem(position-1).getSenderUid();
                }

                boolean isPhoto = message.getPhotoUrl() != null;
                if (isPhoto) {
                    messageTextView.setVisibility(View.GONE);
                    photoImageView.setVisibility(View.VISIBLE);
                    Glide.with(photoImageView.getContext())
                            .load(message.getPhotoUrl())
                            .asBitmap()
                            .override(80, 120)
                            .centerCrop()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    photoImageView.setImageBitmap(resource);
                                }
                            });
                } else {
                    messageTextView.setVisibility(View.VISIBLE);
                    photoImageView.setVisibility(View.GONE);
                    messageTextView.setText(message.getText());
                }

                if (!message.getSenderUid().equals(mUserUid)) {
                    if (previousSender.equals(message.getSenderUid())) {
                        authorTextView.setVisibility(View.GONE);
                    } else {
                        authorTextView.setText(message.getSender() + "  -  " + getMessageTime(message.getTimestamp()));
                    }
                }
            }
        };
    }

    @OnItemClick(R.id.messageListView)
    public void showFullscreenPhoto(AdapterView<?> parent, int position){
        FirechatMessage message = (FirechatMessage) parent.getItemAtPosition(position);
        if(message.getPhotoUrl()!=null) {
            Intent intent = new Intent(getApplicationContext(), FullscreenPhotoActivity.class);
            intent.putExtra(FullscreenPhotoActivity.PHOTO_URI, message.getPhotoUrl());
            startActivity(intent);
        }
    }

    private void setMessageLength() {
        FirebaseRemoteConfigSettings rcSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(rcSettings);

        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(FRIENDLY_MESSAGE_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();
    }

    private void fetchConfig() {
        long cacheExpiration = 3600;

        if(mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()){
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFirebaseRemoteConfig.activateFetched();
                applyLengthLimit();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                applyLengthLimit();
            }
        });
    }

    private void applyLengthLimit() {
        Long textLength = mFirebaseRemoteConfig.getLong(FRIENDLY_MESSAGE_LENGTH_KEY);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(textLength.intValue())});
    }

    private String getMessageTime(long timestamp){
        String date;
        SimpleDateFormat sDateFormat = new SimpleDateFormat("EEE, MMM d", getCurrentLocale());
        SimpleDateFormat sTimeFormat = new SimpleDateFormat("HH:mm", getCurrentLocale());
        final long DAY_MILLIS = 24 * 3600 * 1000;

        long currentTime = Calendar.getInstance().getTime().getTime();
        long timeDiff = currentTime-timestamp;

        Date dateDate = new Date(timestamp);

        if (timeDiff < (DAY_MILLIS)) {
            date = sTimeFormat.format(dateDate);
        } else {
            date = sDateFormat.format(dateDate);
        }

        return date;
    }

    public Locale getCurrentLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return getResources().getConfiguration().getLocales().get(0);
        } else{
            return getResources().getConfiguration().locale;
        }
    }
}
