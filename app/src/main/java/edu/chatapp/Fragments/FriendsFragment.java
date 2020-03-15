package edu.chatapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.chatapp.Adapters.FriendAdapter;
import edu.chatapp.Model.Friend;
import edu.chatapp.Model.User;
import edu.chatapp.R;

public class FriendsFragment extends Fragment {
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private Animation animFab;

    private FriendAdapter friendAdapter;
    private List<User> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        readFriends();

        fab = view.findViewById(R.id.fab);

        animFab = AnimationUtils.loadAnimation(getContext(), R.anim.anim_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialogFragment();
            }
        });

        countFrientRequests();

        return view;
    }

    public void showBottomSheetDialogFragment() {
        NotifyBottomSheetFragment bottomSheetFragment = new NotifyBottomSheetFragment();
        bottomSheetFragment.show(getFragmentManager(), "Custom Bottom Sheet");
    }

    private void readFriends() {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("Friends")
                .child(currentUser.getUid());
        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<Friend> friends = new ArrayList<>();
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    Friend friend = item.getValue(Friend.class);
                    friends.add(friend);
                }

                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userList.clear();
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            User user = item.getValue(User.class);
                            for (Friend friend : friends) {
                                if (user.getId().equals(friend.getId())) {
                                    userList.add(user);
                                }
                            }
                        }

                        friendAdapter = new FriendAdapter(getContext(), userList);
                        recyclerView.setAdapter(friendAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

                if (friends.size() > 0) {
                    fab.show();
                    fab.setAnimation(animFab);
                } else {
                    fab.hide();
                    if (fab.getAnimation() != null) {
                        fab.getAnimation().cancel();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
