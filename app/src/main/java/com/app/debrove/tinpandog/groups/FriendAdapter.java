package com.app.debrove.tinpandog.groups;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.debrove.tinpandog.R;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by NameTooLong on 2017/11/4.
 *
 * 创建新群聊时用于显示好友的{@link RecyclerView}的{@link android.support.v7.widget.RecyclerView.Adapter}
 */

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private List<FriendItem> mFriendItemList;

    public FriendAdapter(List<FriendItem> friendItemList){
        mFriendItemList=friendItemList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox mCheckBox;
        TextView mTextView;
        ImageView mImageView;
        Context mContext;

        public ViewHolder(View view){
            super(view);
            mContext=view.getContext();
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FriendItem friendItem=mFriendItemList.get(position);
        holder.mTextView.setText(friendItem.getFriendName());
        Glide.with(holder.mContext).load(friendItem.getProfilePath()).into(holder.mImageView);
        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendItem.setChosen(holder.mCheckBox.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mFriendItemList==null)?0:mFriendItemList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        viewHolder.mCheckBox=(CheckBox)view.findViewById(R.id.friendItem_checkBox);
        viewHolder.mImageView=(ImageView)view.findViewById(R.id.friendItem_image);
        viewHolder.mTextView=(TextView)view.findViewById(R.id.friendItem_text);
        return viewHolder;
    }
}
