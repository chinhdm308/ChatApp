package edu.chatapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.chatapp.Adapters.FriendRequestAdapter;
import edu.chatapp.Model.Friend;
import edu.chatapp.Model.User;
import edu.chatapp.R;

public class CustomBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private RecyclerView recyclerView;
    private FriendRequestAdapter friendRequestAdapter;
    private List<User> userList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        readFriendRequests();

        return view;
    }

    private void readFriendRequests() {
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

                        friendRequestAdapter = new FriendRequestAdapter(getContext(), userList);
                        recyclerView.setAdapter(friendRequestAdapter);
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
}
