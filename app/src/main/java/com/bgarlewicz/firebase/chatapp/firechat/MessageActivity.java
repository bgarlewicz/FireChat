package com.bgarlewicz.firebase.chatapp.firechat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bgarlewicz.firebase.chatapp.firechat.utils.ColorUtils;
import com.bgarlewicz.firebase.chatapp.firechat.utils.PhotoUtils;
import com.bgarlewicz.firebase.chatapp.firechat.utils.SharedPrefUtils;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;

import static com.bgarlewicz.firebase.chatapp.firechat.FirechatMessage.getTimeDiffInSec;
import static com.bgarlewicz.firebase.chatapp.firechat.utils.PhotoUtils.RC_PHOTO_PICKER;

public class MessageActivity extends AppCompatActivity {

    @BindView(R.id.msg_progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.messages_list_view)
    ListView mMessageListView;
    @BindView(R.id.new_msg_photo_picker_button)
    ImageButton mPhotoPickerButton;
    @BindView(R.id.new_message_edit_text)
    EditText mMessageEditText;
    @BindView(R.id.new_msg_send_button)
    Button mSendButton;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title)
    TextView appNameTextView;
    @BindView(R.id.chat_user_name_text_view)
    TextView userNameTextView;

    @BindDrawable(R.drawable.msg_current_user_background)
    Drawable msgLoggedUserBackground;
    @BindDrawable(R.drawable.msg_others_background) Drawable msgOtherUserBackground;
    @BindDrawable(R.drawable.ic_photo)
    Drawable photoIcon;
    @BindDrawable(R.drawable.ic_send)
    Drawable sendIcon;

    @BindString(R.string.firebase_messages_child) String firebaseMessageChild;
    @BindString(R.string.firebase_chat_photos_child) String firebaseChatPhotosChild;
    @BindString(R.string.firebase_users_child)
    String firebaseUsersChild;
    @BindString(R.string.permission_denied) String permissionDenied;
    @BindString(R.string.choose_picture) String choosePicture;
    @BindString(R.string.user_uid_extra) String receiverUidExtra;
    @BindString(R.string.user_name_extra) String receiverNameExtra;

    private FirebaseListAdapter mMessageAdapter;

    private String mUsername;
    private String mUserUid;
    private String mReceiverUsername;
    private String mReceiverUserUid;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;
    private DatabaseReference mUsersDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefUtils.useTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        ButterKnife.bind(this);
        photoIcon.setColorFilter(new PorterDuffColorFilter(
                ColorUtils.getColorFromAttr(this, R.attr.colorPrimary),
                PorterDuff.Mode.SRC_IN));
        sendIcon.setColorFilter(new PorterDuffColorFilter(
                getResources().getColor(R.color.colorDefaultSendButton),
                PorterDuff.Mode.SRC_IN));


        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mUsername = SharedPrefUtils.getUserName(this);
        mUserUid = SharedPrefUtils.getUserUid(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            mReceiverUsername = bundle.getString(receiverNameExtra);
            mReceiverUserUid = bundle.getString(receiverUidExtra);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(firebaseMessageChild)
                .child(getChatRoom(mUserUid, mReceiverUserUid));
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(firebaseUsersChild);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mPhotosStorageReference = mFirebaseStorage.getReference().child(firebaseChatPhotosChild);

        mMessageAdapter = getFirebaseListAdapter();
        mMessageListView.setAdapter(mMessageAdapter);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        appNameTextView.setVisibility(View.GONE);
        userNameTextView.setVisibility(View.VISIBLE);
        userNameTextView.setText(mReceiverUsername);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMessageAdapter.cleanup();
    }

    @OnClick(R.id.new_msg_photo_picker_button)
    public void pickPhoto() {
        Intent intent = PhotoUtils.pickPhoto(this, this);
        if (intent != null){
            startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)), RC_PHOTO_PICKER);
        }
    }

    @OnClick(R.id.new_msg_send_button)
    public void sendMessage(){
        FirechatMessage firechatMessage = new FirechatMessage(mMessageEditText.getText().toString(),
                                                                mUsername,
                                                                mUserUid,
                mReceiverUsername,
                mReceiverUserUid,
                                                                null,
                                                                Calendar.getInstance().getTime().getTime());

        mMessagesDatabaseReference.push().setValue(firechatMessage);
        mMessageEditText.setText("");
        mMessageEditText.clearFocus();
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

    @OnTextChanged(value = R.id.new_message_edit_text)
    public void afterTextChanged(CharSequence charSequence){

        if (charSequence.toString().trim().length() > 0) {
            mSendButton.setEnabled(true);
            sendIcon.setColorFilter(new PorterDuffColorFilter(
                    ColorUtils.getColorFromAttr(this, R.attr.colorPrimary),
                    PorterDuff.Mode.SRC_IN));
        } else {
            mSendButton.setEnabled(false);
            sendIcon.setColorFilter(new PorterDuffColorFilter(
                    getResources().getColor(R.color.colorDefaultSendButton),
                    PorterDuff.Mode.SRC_IN));
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
                                mReceiverUsername,
                                mReceiverUserUid,
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
                    View newView = getLayoutInflater().inflate(R.layout.item_message_current_user, viewGroup, false);
                    populateView(newView, message, position);
                    return newView;
                } else {
                    return super.getView(position, null, viewGroup);
                }
            }

            @Override
            protected void populateView(View view, FirechatMessage firechatMessages, int position) {

                final ImageView photoImageView = (ImageView) view.findViewById(R.id.msg_item_image_view);
                TextView messageTextView = (TextView) view.findViewById(R.id.msg_item_text_view);
                final TextView authorTextView = (TextView) view.findViewById(R.id.msg_item_date_text_view);
                final ImageView authorPhoto = (ImageView) view.findViewById(R.id.msg_item_user_photo);
                FrameLayout divider = (FrameLayout) view.findViewById(R.id.msg_item_divider);

                final FirechatMessage message = getItem(position);

                DatabaseReference ref = mUsersDatabaseReference.child(message.getSenderUid());

                String nextSender;
                if (position == getCount() - 1) {
                    nextSender = mUserUid;
                } else {
                    nextSender = getItem(position + 1).getSenderUid();
                }

                long previousTimestamp;
                if (position == 0){
                    previousTimestamp = 0;
                } else {
                    previousTimestamp = getItem(position - 1).getTimestamp();
                }

                boolean isPhoto = message.getPhotoUrl() != null;
                if (isPhoto) {
                    messageTextView.setVisibility(View.GONE);
                    photoImageView.setVisibility(View.VISIBLE);
                    PhotoUtils.setImageWithGlide(photoImageView, message.getPhotoUrl(), 80, 120);
                } else {
                    messageTextView.setVisibility(View.VISIBLE);
                    photoImageView.setVisibility(View.GONE);
                    messageTextView.setText(message.getText());
                }

                if (!message.getSenderUid().equals(mUserUid)) {
                    if (!nextSender.equals(message.getSenderUid())) {
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                PhotoUtils.setImageWithGlide(authorPhoto, user.getPhotoUrl());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }

                if (!nextSender.equals(message.getSenderUid())) {
                    divider.setVisibility(View.VISIBLE);
                }

                if (getTimeDiffInSec(previousTimestamp, message.getTimestamp()) > 600) {
                    authorTextView.setText(FirechatMessage.getMessageTime(getApplicationContext(), message.getTimestamp()));
                } else {
                    authorTextView.setVisibility(View.GONE);
                }
            }
        };
    }

    @OnItemClick(R.id.messages_list_view)
    public void showFullscreenPhoto(AdapterView<?> parent, int position){
        mMessageEditText.clearFocus();
        FirechatMessage message = (FirechatMessage) parent.getItemAtPosition(position);
        if(message.getPhotoUrl()!=null) {
            Intent intent = new Intent(getApplicationContext(), FullscreenPhotoActivity.class);
            intent.putExtra(FullscreenPhotoActivity.PHOTO_URI, message.getPhotoUrl());
            startActivity(intent);
        }
    }
}
