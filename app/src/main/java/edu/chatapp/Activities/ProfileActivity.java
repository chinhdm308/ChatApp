package edu.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.chatapp.Model.User;
import edu.chatapp.R;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView profileImage;
    private TextView username;
    private Button btnUpdateProfile, btnAddFriend, btnCancelFriend, btnUnfriend, btnAccept, btnRefuse;
    private RelativeLayout group1, group2;

    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    private static final int IMAGE_REQUEST_CODE = 1;
    private static final int PReqCode = 2;
    private Uri imageUri = null;

    private Intent intent;
    private String userId;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mapping();

        init();

        if (currentUser.getUid().equals(userId)) {
            group1.setVisibility(View.INVISIBLE);
            group2.setVisibility(View.INVISIBLE);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadProfileImage();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestForPermission();
            }
        });

        DatabaseReference databaseRef1 = FirebaseDatabase.getInstance().getReference("FriendRequests")
                .child(currentUser.getUid()).child(userId);
        databaseRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    group1.setVisibility(View.INVISIBLE);
                    group2.setVisibility(View.VISIBLE);
                } else {
                    group1.setVisibility(View.VISIBLE);
                    group2.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference addFriendsRef = FirebaseDatabase.getInstance().getReference("FriendRequests")
                .child(userId).child(currentUser.getUid());
        addFriendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    btnAddFriend.setVisibility(View.INVISIBLE);
                    btnCancelFriend.setVisibility(View.VISIBLE);
                } else {
                    btnAddFriend.setVisibility(View.VISIBLE);
                    btnCancelFriend.setVisibility(View.INVISIBLE);
                }

                if (currentUser.getUid().equals(userId)) {
                    group1.setVisibility(View.INVISIBLE);
                    group2.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("Friends")
                .child(currentUser.getUid()).child(userId);
        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    group1.setVisibility(View.VISIBLE);
                    group2.setVisibility(View.INVISIBLE);
                    btnAddFriend.setVisibility(View.INVISIBLE);
                    btnCancelFriend.setVisibility(View.INVISIBLE);
                    btnUnfriend.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriendRequest();
                btnAddFriend.setVisibility(View.INVISIBLE);
                btnCancelFriend.setVisibility(View.VISIBLE);
                btnUnfriend.setVisibility(View.INVISIBLE);
            }
        });

        btnCancelFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelFriendRequest(userId, currentUser.getUid());
                btnCancelFriend.setVisibility(View.INVISIBLE);
                btnAddFriend.setVisibility(View.VISIBLE);
                btnUnfriend.setVisibility(View.INVISIBLE);
            }
        });

        btnRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelFriendRequest(currentUser.getUid(), userId);
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
                cancelFriendRequest(currentUser.getUid(), userId);
            }
        });

        btnUnfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unFriend();
                btnAddFriend.setVisibility(View.VISIBLE);
                btnCancelFriend.setVisibility(View.INVISIBLE);
                btnUnfriend.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void unFriend() {
        final DatabaseReference removeFriendRequestsRef = FirebaseDatabase.getInstance().getReference("Friends");
        removeFriendRequestsRef.child(currentUser.getUid()).child(userId).removeValue();
    }

    private void addFriend() {
        final DatabaseReference addFriendRef1 = FirebaseDatabase.getInstance().getReference("Friends")
                .child(userId).child(currentUser.getUid());
        addFriendRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    addFriendRef1.child("id").setValue(currentUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference addFriendRef2 = FirebaseDatabase.getInstance().getReference("Friends")
                .child(currentUser.getUid()).child(userId);
        addFriendRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    addFriendRef2.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadProfileImage() {
        databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(ProfileActivity.this).load(user.getImageURL()).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void cancelFriendRequest(String id1, String id2) {
        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("FriendRequests");
        databaseRef.child(id1).child(id2).removeValue();
    }

    private void addFriendRequest() {
        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("FriendRequests")
                .child(userId).child(currentUser.getUid());
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    databaseRef.child("id").setValue(currentUser.getUid());
                    showMessage("Sent friend request to " + user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void init() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        intent = getIntent();
        userId = intent.getStringExtra("userId");
    }

    private void mapping() {
        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btnAddFriend = findViewById(R.id.btn_add_friend);
        btnCancelFriend = findViewById(R.id.btn_cancel_friend);
        group1 = findViewById(R.id.group_1);
        group2 = findViewById(R.id.group_2);
        btnAccept = findViewById(R.id.btn_accept);
        btnRefuse = findViewById(R.id.btn_refuse);
        btnUnfriend = findViewById(R.id.btn_unfriend);
    }

    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessage("Please accept for required permission");
            } else {
                ActivityCompat.requestPermissions(ProfileActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_REQUEST_CODE);
    }

    private void updateUserInfo(final String username, Uri imageUri, final FirebaseUser currentUser) {
        final ProgressDialog progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.show();
        // Update user photo and name
        // First we need to upload user photo to firebase storage and get url
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference imageFilePath = storageRef.child("users-photos/" + imageUri.getLastPathSegment());
        imageFilePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
                        databaseRef.child("imageURL").setValue(uri.toString());
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_REQUEST_CODE && data != null) {
            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void showMessage(String message) {
        Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
