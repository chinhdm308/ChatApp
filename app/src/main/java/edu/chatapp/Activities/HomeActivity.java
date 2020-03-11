package edu.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.chatapp.Adapters.ViewPagerAdapter;
import edu.chatapp.Fragments.ChatsFragment;
import edu.chatapp.Fragments.FriendsFragment;
import edu.chatapp.Fragments.UsersFragment;
import edu.chatapp.Model.Friend;
import edu.chatapp.Model.User;
import edu.chatapp.R;

public class HomeActivity extends AppCompatActivity {
    private CircleImageView profileImage;
    private TextView username;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // init views
        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileActivity = new Intent(HomeActivity.this, ProfileActivity.class);
                profileActivity.putExtra("userId", currentUser.getUid());
                startActivity(profileActivity);
            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter adater = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        adater.addFragment(new ChatsFragment(), "");
        adater.addFragment(new FriendsFragment(), "");
        adater.addFragment(new UsersFragment(), "");

        viewPager.setAdapter(adater);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.icons8_friends_50);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_black_24dp);

        countFrientRequests();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
//                finish();
                break;
        }
        return true;
    }

    private void updateStatus(String status) {
        databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        databaseRef.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("online");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateStatus("offline");
    }

    private void showBadge(int index, int number) {
        BadgeDrawable badgeDrawable = tabLayout.getTabAt(index).getOrCreateBadge();
        badgeDrawable.setVisible(true);
        badgeDrawable.setNumber(number);
    }

    private void hideBadge(int index) {
        BadgeDrawable badgeDrawable = tabLayout.getTabAt(index).getOrCreateBadge();
        badgeDrawable.setVisible(false);
    }


    private void countFrientRequests() {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("FriendRequests")
                .child(currentUser.getUid());
        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<Friend> friends = new ArrayList<>();
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    Friend friend = item.getValue(Friend.class);
                    friends.add(friend);
                }

                if (friends.size() != 0) {
                    showBadge(1, friends.size());
                } else {
                    hideBadge(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
