package com.app.debrove.tinpandog.groups;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.debrove.tinpandog.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NameTooLong on 2017/10/29.
 *
 * 聊天使用的{@link RecyclerView}所使用的{@link android.support.v7.widget.RecyclerView.Adapter}
 */

public class ChatItemAdapter extends RecyclerView.Adapter<ChatItemAdapter.ViewHolder> {
    private static final int IMAGE_USER=0;
    private static final int TEXT_USER=1;
    private static final int IMAGE_OTHERS=2;
    private static final int TEXT_OTHERS=3;

    /**
     * 聊天信息的储存
     */
    private List<ChatItem> mList=new ArrayList();
    /**
     * 示例用的聊天信息
     */
    private static ChatItem mChatItem_example;
    private RecyclerView mRecyclerView;
    private int a_icon=96;
    public int width_content=-1;
    static {
         mChatItem_example = new ChatItem(null, false);
         mChatItem_example.setContent("Hello,world!", ChatItem.ChatType.TEXT);
    };
    private static final String TAG = "ChatItemAdapter";

    /**
     *向聊天框内添加多条聊天信息，若为null，则添加示例信息
     */
    public void addNewChatItem(List<ChatItem> list){
        if(list.size()!=0)
            mList.addAll(list);
        else
            mList.add(mChatItem_example);
        moveToNewItem();
        notifyDataSetChanged();
    }


    public void moveToNewItem(){
        int size=mList.size();
        if(mRecyclerView!=null)
            mRecyclerView.scrollToPosition(size==0?0:size-1);
    }

    public void setRecyclerView(RecyclerView recyclerView){
        mRecyclerView=recyclerView;
    }

    public void addNewChatItem(ChatItem chatItem){
        mList.add(chatItem);
        //notifyDataSetChanged();
        notifyItemInserted(mList.size()-1);
        moveToNewItem();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatItem chatItem=mList.get(position);
        View view= holder.mView;
        View childView=null;
        FrameLayout frameLayout=(FrameLayout)view.findViewById(R.id.chat_item_frameLayout);
        ImageView profileImage=(ImageView)view.findViewById(R.id.chat_item_profile);
        Glide.with(view.getContext()).load(chatItem.getProfileUri()).into(profileImage);
        int height=0;
        switch (chatItem.getChatType()){
            case TEXT:
                TextView textView=(TextView)view.findViewById(R.id.chat_item_text);
                textView.setTextSize(30);
                String text[]=chatItem.getContent().split("\n");
                TextPaint textPaint=textView.getPaint();
                int line=0;
                Rect bounds=new Rect();
                int line_forEachString=0;
                for(int i=0;i<text.length;i++){
                    textPaint.getTextBounds(text[i],0,text[i].length(),bounds);
                    line_forEachString=bounds.width()/(width_content-a_icon);
                    line+=(bounds.width()%(width_content-a_icon)==0)?line_forEachString:line_forEachString+1;
                    textView.append(text+"\n");
                }
                height=line*a_icon;
                textView.setText(chatItem.getContent());
                childView=textView;
                break;
            case IMAGE:
                ImageView imageView=(ImageView)view.findViewById(R.id.chat_item_image);
                Bitmap bitmap=BitmapFactory.decodeFile(chatItem.getContent());
                height=bitmap.getHeight();
                Glide.with(view.getContext()).load(chatItem.getContent()).into(imageView);
                childView=imageView;
                break;
        }
        ViewGroup.LayoutParams layoutParams1=childView.getLayoutParams();
        layoutParams1.height=height;
        layoutParams1.width=width_content-48;
        childView.setLayoutParams(layoutParams1);
        Log.e(TAG,"h="+height);
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(width_content,height));
        view.setLayoutParams(new LinearLayout.LayoutParams(width_content,height+2));
    }

    @Override
    public int getItemViewType(int position) {
        ChatItem chatItem=mList.get(position);
        switch (chatItem.getChatType()){
            case TEXT:
                return chatItem.is_others()?TEXT_OTHERS:TEXT_USER;
            case IMAGE:
                return chatItem.is_others()?IMAGE_OTHERS:IMAGE_USER;
        }
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= null;
        switch (viewType){
            case IMAGE_OTHERS:
                view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_others_image,parent,false);
                break;
            case IMAGE_USER:
                view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_user_image,parent,false);
                break;
            case TEXT_OTHERS:
                view=LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_others_text,parent,false);
                break;
            case TEXT_USER:
                view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_user_text,parent,false);
                break;
        }
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public ViewHolder(View view){
            super(view);
            mView=view;
        }
    }
}
