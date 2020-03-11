package edu.chatapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.chatapp.Model.Chat;
import edu.chatapp.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    Context mContext;
    List<Chat> mData;

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private String imageURL;

    FirebaseUser currentUser;

    public MessageAdapter(Context mContext, List<Chat> mData, String imageURL) {
        this.mContext = mContext;
        this.mData = mData;
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_LEFT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.row_msg_item_left, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.row_msg_item_right, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Chat chat = mData.get(position);
        holder.showMessage.setText(chat.getMessage());

        if (imageURL.equals("default")) {
            holder.profileImage.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageURL).into(holder.profileImage);
        }

        if (position == mData.size() - 1) {
            if (chat.isIsseen()) {
                holder.txtSeen.setText("Seen");
            } else {
                holder.txtSeen.setText("Delivered");
            }
        } else {
            holder.txtSeen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView showMessage, txtSeen;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            showMessage = itemView.findViewById(R.id.show_message);
            txtSeen = itemView.findViewById(R.id.txt_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mData.get(position).getSender().equals(currentUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
