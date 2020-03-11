package edu.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import edu.chatapp.Activities.MessageActivity;
import edu.chatapp.Model.Chat;
import edu.chatapp.Model.User;
import edu.chatapp.R;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private Context mContext;
    private List<User> mData;
    private String theLastMessage;
    private FirebaseUser currentUser;

    public ChatAdapter(Context context, List<User> mData) {
        this.mContext = context;
        this.mData = mData;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.row_user_item_chats, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatViewHolder holder, final int position) {
        final User user = mData.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        lastMessage(user.getId(), holder.lastMsg);

        if (user.getStatus().equals("online")) {
            holder.userStatusImage.setVisibility(View.VISIBLE);
            holder.userStatusImage.setCircleBackgroundColorResource(R.color.status_user_on);
        } else {
            holder.userStatusImage.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent messageActivity = new Intent(mContext, MessageActivity.class);
                messageActivity.putExtra("userID", user.getId());
                mContext.startActivity(messageActivity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile_image, userStatusImage;
        TextView username, lastMsg;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            userStatusImage = itemView.findViewById(R.id.user_status_image);
            lastMsg = itemView.findViewById(R.id.last_msg);
        }
    }

    private void lastMessage(final String userId, final TextView lastMsg) {
        theLastMessage = "default";
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Chats");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getSender().equals(currentUser.getUid()) && chat.getReceiver().equals(userId) ||
                            chat.getSender().equals(userId) && chat.getReceiver().equals(currentUser.getUid())) {
                        if (chat.getSender().equals(currentUser.getUid())) {
                            theLastMessage = "You: " + chat.getMessage();
                        } else {
                            if (chat.isIsseen()) {
                                lastMsg.setTypeface(lastMsg.getTypeface(), Typeface.NORMAL);
                            } else {
                                lastMsg.setTypeface(lastMsg.getTypeface(), Typeface.BOLD);
                            }
                            theLastMessage = chat.getMessage();
                        }
                    }
                }

                if (theLastMessage.equals("default")) {
                    lastMsg.setText("No message");
                } else {
                    lastMsg.setText(theLastMessage);
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
