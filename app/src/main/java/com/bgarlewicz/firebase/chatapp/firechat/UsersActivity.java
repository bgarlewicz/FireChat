package com.bgarlewicz.firebase.chatapp.firechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.bgarlewicz.firebase.chatapp.firechat.utils.SharedPrefUtils;

import java.util.Arrays;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class UsersActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseStateListener;

    private FirebaseListAdapter mUserAdapter;
    private String mUsername;
    private String mUserUid;

    @BindString(R.string.anonymus_user) String ANONYMOUS;
    @BindString(R.string.firebase_users_child) String firebaseUsersChild;
    @BindString(R.string.greetings) String greetings;
    @BindString(R.string.user_uid_extra) String userUidExtra;
    @BindString(R.string.user_name_extra) String userNameExtra;

    @BindView(R.id.userListView) ListView mUserListView;
    @BindView(R.id.userProgressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        ButterKnife.bind(this);

        mUsername = ANONYMOUS;
        mUserUid = "";

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(firebaseUsersChild);
        mFirebaseAuth = FirebaseAuth.getInstance();

        mFirebaseStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){
                    initializeSigningIn(firebaseAuth.getCurrentUser());
                } else {
                    initiallizeSigningOut();
                }
            }
        };

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                    view = null;
                    return super.getView(position, view, viewGroup);
                }
            }

            @Override
            protected void populateView(View view, User user, int position) {

                ImageView userImageView = (ImageView) view.findViewById(R.id.userPhotoImageView);
                TextView userTextView = (TextView) view.findViewById(R.id.userNameTextView);

                User currentUser = getItem(position);

//                Glide.with(userImageView.getContext())
//                            .load(currentUser.getPhotoUrl())
//                            .into(userImageView);

                userTextView.setText(currentUser.getUserName());
            }
        };
    }

    private void initializeSigningIn(FirebaseUser firebaseUser) {
        mUsername = firebaseUser.getDisplayName();
        mUserUid = firebaseUser.getUid();

        String prefUserUid = SharedPrefUtils.getUserUid(this);
        String token = SharedPrefUtils.getUserToken(this);
        int tokenState = SharedPrefUtils.getTokenState(this);
        if (!mUserUid.equals(prefUserUid) || tokenState > 0){
            SharedPrefUtils.setUserName(this, mUsername);
            SharedPrefUtils.setTokenState(this, 0);
            SharedPrefUtils.setUserUid(this, mUserUid);

            User user = new User(mUsername, token, mUserUid, null);
            mUsersDatabaseReference.child(firebaseUser.getUid()).setValue(user);
        }

        mUserAdapter = getUsersListAdapter();
        mUserListView.setAdapter(mUserAdapter);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    @OnItemClick(R.id.userListView)
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(getApplicationContext(),greetings , Toast.LENGTH_LONG).show();
            } else if(resultCode == RESULT_CANCELED){
                finish();
            }
        }
    }
}
