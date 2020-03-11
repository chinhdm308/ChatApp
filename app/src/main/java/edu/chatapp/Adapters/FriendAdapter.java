package edu.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.chatapp.Activities.MessageActivity;
import edu.chatapp.Model.User;
import edu.chatapp.R;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewholder> {
    private Context mContext;
    private List<User> mData;

    public FriendAdapter(Context mContext, List<User> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public FriendViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_user_item_friend, parent, false);
        return new FriendViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewholder holder, int position) {
        final User user = mData.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

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

    public class FriendViewholder extends RecyclerView.ViewHolder {
        CircleImageView profile_image, userStatusImage;
        TextView username;
        public FriendViewholder(@NonNull View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            userStatusImage = itemView.findViewById(R.id.user_status_image);
        }
    }
}
