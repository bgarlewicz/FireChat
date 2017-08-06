package com.bgarlewicz.firebase.chatapp.firechat;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bgarlewicz.firebase.chatapp.firechat.dialogs.ChangeNameDialogFragment;
import com.bgarlewicz.firebase.chatapp.firechat.utils.PhotoUtils;
import com.bgarlewicz.firebase.chatapp.firechat.utils.SharedPrefUtils;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Arrays;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

import static com.bgarlewicz.firebase.chatapp.firechat.utils.PhotoUtils.RC_PHOTO_PICKER;

public class UsersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ChangeNameDialogFragment.OnNameChangedListener {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = UsersActivity.class.getSimpleName();
    @BindString(R.string.anonymus_user)
    String ANONYMOUS;
    @BindString(R.string.firebase_users_child)
    String firebaseUsersChild;
    @BindString(R.string.greetings)
    String greetings;
    @BindString(R.string.user_uid_extra)
    String userUidExtra;
    @BindString(R.string.user_name_extra)
    String userNameExtra;
    @BindString(R.string.firebase_chat_photos_child)
    String firebaseChatPhotosChild;
    @BindView(R.id.users_list_view)
    ListView mUserListView;
    @BindView(R.id.users_progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView mNavView;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseStateListener;
    private StorageReference mPhotosStorageReference;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseListAdapter mUserAdapter;
    private String mUsername;
    private String mUserUid;
    private String mUserPhoto;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView mNavUserName;
    private TextView mNavUserEmail;
    private ImageView mNavImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefUtils.useTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mNavUserName = (TextView) mNavView.getHeaderView(0).findViewById(R.id.nav_user_name);
        mNavUserEmail = (TextView) mNavView.getHeaderView(0).findViewById(R.id.nav_user_email);
        mNavImageView = (ImageView) mNavView.getHeaderView(0).findViewById(R.id.nav_image_view);

        mUsername = ANONYMOUS;
        mUserUid = "";

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_closed);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(firebaseUsersChild);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mPhotosStorageReference = mFirebaseStorage.getReference().child(firebaseChatPhotosChild);

        mFirebaseStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){
                    initializeSigningIn(firebaseAuth.getCurrentUser());

                    mUserAdapter = getUsersListAdapter();
                    mUserListView.setAdapter(mUserAdapter);
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                } else {
                    initiallizeSigningOut();
                }
            }
        };

        mNavView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mFirebaseStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mFirebaseStateListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mUserAdapter != null) {
            mUserAdapter.cleanup();
        }
    }

    @NonNull
    private FirebaseListAdapter<User> getUsersListAdapter() {
        return new FirebaseListAdapter<User>(this, User.class, R.layout.item_user, mUsersDatabaseReference) {
            @Override
            public View getView(int position, View view, ViewGroup viewGroup) {

                User currentUser = getItem(position);

                if (currentUser.getUserUid().equals(mUserUid)){
                    return getLayoutInflater().inflate(R.layout.item_user_empty, viewGroup, false);
                } else {
                    return super.getView(position, null, viewGroup);
                }
            }

            @Override
            protected void populateView(View view, User user, int position) {

                ImageView userImageView = (ImageView) view.findViewById(R.id.item_user_image_view);
                TextView userTextView = (TextView) view.findViewById(R.id.user_name_text_view);

                User currentUser = getItem(position);

                if(currentUser.getPhotoUrl() != null) {
                    Glide.with(userImageView.getContext())
                            .load(Uri.parse(currentUser.getPhotoUrl()))
                            .into(userImageView);
                }

                userTextView.setText(currentUser.getUserName());
            }
        };
    }

    private void initializeSigningIn(FirebaseUser firebaseUser) {
        mUsername = firebaseUser.getDisplayName();
        mUserUid = firebaseUser.getUid();
        if (firebaseUser.getPhotoUrl() != null) {
            mUserPhoto = firebaseUser.getPhotoUrl().toString();
        } else {
            mUserPhoto = "";
        }

        mNavUserName.setText(mUsername);
        mNavUserEmail.setText(firebaseUser.getEmail());
        Glide.with(mNavImageView.getContext())
                .load(firebaseUser.getPhotoUrl())
                .into(mNavImageView);

        String prefUserUid = SharedPrefUtils.getUserUid(this);
        String token = SharedPrefUtils.getUserToken(this);
        String prefUserName = SharedPrefUtils.getUserName(this);
        String prefUserPhoto = SharedPrefUtils.getUserPhoto(this);
        int tokenState = SharedPrefUtils.getTokenState(this);
        if (!mUserUid.equals(prefUserUid) || tokenState > 0
                || !mUsername.equals(prefUserName) || !mUserPhoto.equals(prefUserPhoto)){
            Log.d(TAG, "User change requested");
            SharedPrefUtils.setUserName(this, mUsername);
            SharedPrefUtils.setTokenState(this, 0);
            SharedPrefUtils.setUserUid(this, mUserUid);
            SharedPrefUtils.setUserPhoto(this, mUserPhoto);

            User user = new User(mUsername, token, mUserUid, mUserPhoto);
            mUsersDatabaseReference.child(firebaseUser.getUid()).setValue(user);
        }
    }

    @OnItemClick(R.id.users_list_view)
    public void showMessages(AdapterView<?> parent, int position){
        User user = (User) parent.getItemAtPosition(position);
        Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
        intent.putExtra(userUidExtra, user.getUserUid());
        intent.putExtra(userNameExtra, user.getUserName());
        startActivity(intent);
    }

    private void initiallizeSigningOut() {
        mUsername = ANONYMOUS;
        mUserUid = "";

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_log_out:
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.nav_change_username:
                DialogFragment dialogFragment = new ChangeNameDialogFragment();
                dialogFragment.show(getFragmentManager(), ChangeNameDialogFragment.TAG);
                return true;
            case R.id.nav_change_profile_pic:
//                Intent intent = PhotoUtils.pickPhoto(this, this);
//                if (intent != null){
//                    startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)), RC_PHOTO_PICKER);
//                }
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle("My Crop")
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setFixAspectRatio(true)
                        .setRequestedSize(300, 400)
                        .start(this);
                return true;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onNameChanged(String changedName) {
        mNavUserName.setText(changedName);
        initializeSigningIn(mFirebaseAuth.getCurrentUser());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PhotoUtils.onRequestPermission(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(getApplicationContext(),greetings , Toast.LENGTH_LONG).show();
            } else if(resultCode == RESULT_CANCELED){
                finish();
            }
        } else if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
            Uri selectedPhotoUri = data.getData();
            StorageReference storageReference = mPhotosStorageReference.child(selectedPhotoUri.getLastPathSegment());
            UploadTask uploadTask = storageReference.putFile(selectedPhotoUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    if (downloadUri != null) {
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(downloadUri)
                                .build();

                        user.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");
                                    initializeSigningIn(user);
                                }
                            }
                        });
                    }
                }
            });
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
                Uri selectedPhotoUri = result.getUri();
                StorageReference storageReference = mPhotosStorageReference.child(selectedPhotoUri.getLastPathSegment());
                UploadTask uploadTask = storageReference.putFile(selectedPhotoUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        if (downloadUri != null) {
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri)
                                    .build();

                            user.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User profile updated.");
                                        initializeSigningIn(user);
                                    }
                                }
                            });
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
