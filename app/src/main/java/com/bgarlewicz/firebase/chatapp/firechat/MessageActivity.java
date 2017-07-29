/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bgarlewicz.firebase.chatapp.firechat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.bgarlewicz.firebase.chatapp.firechat.utils.SharedPrefUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MessageActivity extends AppCompatActivity {

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 160;
    private static final int RC_PHOTO_PICKER = 432;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 654;

    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.messageListView) ListView mMessageListView;
    @BindView(R.id.photoPickerButton) ImageButton mPhotoPickerButton;
    @BindView(R.id.messageEditText) EditText mMessageEditText;
    @BindView(R.id.sendButton) Button mSendButton;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

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

        if(ContextCompat.checkSelfPermission(MessageActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MessageActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_EXTERNAL_STORAGE);
        } else{
            launchImagePicker();
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
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchImagePicker();
                } else {
                    Toast.makeText(this, permissionDenied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
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
            protected void populateView(View view, FirechatMessage firechatMessages, int position) {

                ImageView photoImageView = (ImageView) view.findViewById(R.id.photoImageView);
                TextView messageTextView = (TextView) view.findViewById(R.id.messageTextView);
                TextView authorTextView = (TextView) view.findViewById(R.id.nameTextView);

                FirechatMessage message = getItem(position);

                boolean isPhoto = message.getPhotoUrl() != null;
                if (isPhoto) {
                    messageTextView.setVisibility(View.GONE);
                    photoImageView.setVisibility(View.VISIBLE);
                    Glide.with(photoImageView.getContext())
                            .load(message.getPhotoUrl())
                            .into(photoImageView);
                } else {
                    messageTextView.setVisibility(View.VISIBLE);
                    photoImageView.setVisibility(View.GONE);
                    messageTextView.setText(message.getText());
                    if(mUsername.equals(message.getSender())) {
                        messageTextView.setBackground(msgLoggedUserBackground);
                    } else {
                        messageTextView.setBackground(msgOtherUserBackground);
                    }
                }
                authorTextView.setText(message.getSender());

            }
        };
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

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, choosePicture), RC_PHOTO_PICKER);
    }
}