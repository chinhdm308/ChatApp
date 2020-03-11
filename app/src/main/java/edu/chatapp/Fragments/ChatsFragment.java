package edu.chatapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.chatapp.Adapters.ChatAdapter;
import edu.chatapp.Adapters.UserAdapter;
import edu.chatapp.Model.ChatList;
import edu.chatapp.Model.User;
import edu.chatapp.R;

public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;

    private ChatAdapter chatAdapter;
    private List<User> userList;

    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    private List<ChatList> chatUserIdList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        chatUserIdList = new ArrayList<>();
        userList = new ArrayList<>();

        databaseRef = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid());
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatUserIdList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatList chatlist = snapshot.getValue(ChatList.class);
                    chatUserIdList.add(chatlist);
                }

                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void chatList() {
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (ChatList chatlist : chatUserIdList) {
                        assert user != null;
                        if (user.getId().equals(chatlist.getId())) {
                            userList.add(user);
                        }
                    }
                }
                chatAdapter = new ChatAdapter(getContext(), userList);
                recyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
