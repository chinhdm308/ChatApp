package edu.chatapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.chatapp.Model.User;
import edu.chatapp.R;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder> {

    private Context mContext;
    private List<User> mData;

    public FriendRequestAdapter(Context mContext, List<User> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.row_friend_request, parent, false);
        return new FriendRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        final User user = mData.get(position);

        if (user.getImageURL().equals("default")) {
            holder.profileImage.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profileImage);
        }

        holder.username.setText(user.getUsername());

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptFriendRequest(user.getId());
                cancelFriendRequest(user.getId());
            }
        });

        holder.btnRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelFriendRequest(user.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public  class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView username;
        Button btnRefuse, btnAccept;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnRefuse = itemView.findViewById(R.id.btn_refuse);
        }
    }

    private void acceptFriendRequest(final String userId) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
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

    private void cancelFriendRequest(String id) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference removeFriendRequestsRef = FirebaseDatabase.getInstance().getReference("FriendRequests");
        removeFriendRequestsRef.child(currentUser.getUid()).child(id).removeValue();
    }
}
