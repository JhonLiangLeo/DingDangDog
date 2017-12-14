package com.app.debrove.tinpandog.groups;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private List<ChatItem> mShownList=new ArrayList(),mChatItemList;
    /**
     * 示例用的聊天信息
     */
    private static ChatItem mChatItem_example;
    private RecyclerView mRecyclerView;
    private Activity mActivity;
    private int a_icon=48,mPageSize=8,mCurrenPage=0;
    private float scale=0;
    public static int width_content=-1;
    static {
         mChatItem_example = new ChatItem(null, false);
         mChatItem_example.setContent("Hello,world!", ChatItem.ChatType.TEXT);
    };
    private static final String TAG = "ChatItemAdapter";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mIsFirstLoaded;

    /**
     *向聊天框内添加多条聊天信息，若为null，则添加示例信息
     */
    public void addNewChatItem(final List<ChatItem> list){
        if(list.size()!=0)
            mShownList.addAll(list);
        else
            mShownList.add(mChatItem_example);
        if(Looper.getMainLooper()!=Looper.myLooper()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int num=list.size()==0?1:list.size();
                    notifyItemRangeInserted(mShownList.size()-num,num);
                    moveToNewItem();
                }
            });
        }
        else {
            int num=list.size()==0?1:list.size();
            notifyItemRangeInserted(mShownList.size()-num,num);
            moveToNewItem();
        }
    }

    public void moveToNewItem(){
        int size = mShownList.size();
        if (mRecyclerView != null)
            mRecyclerView.scrollToPosition(size == 0 ? 0 : size - 1);

    }

    /**
     * 将历史消息加载入内存中保存，不展示全部
     * @param chatItems 历史消息
     */
    public void onLoadMoreHistoryChat(List<ChatItem> chatItems){
        StringBuilder stringBuilder=new StringBuilder();
        for(ChatItem chatItem:chatItems)
            stringBuilder.append(chatItem.getContent()+" ");
        Log.e(TAG,"mess:"+stringBuilder.toString());
        mChatItemList=chatItems;
        mIsFirstLoaded=true;
        onRefresh();
    }

    /**
     * 在用户向上滑动时，加载更多的历史消息
     */
    public void onRefresh(){
        new AsyncTask<Integer,List<ChatItem>,Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                mCurrenPage++;

                int expectedEndPosition=mChatItemList.size()-(mCurrenPage-1)*mPageSize;
                if(expectedEndPosition<=0){
                    publishProgress();
                    return null;
                }
                int startPosition=expectedEndPosition-mPageSize;
                if(startPosition<0)
                    startPosition=0;
                List<ChatItem> chatItems=new ArrayList<ChatItem>();
                for(int i=expectedEndPosition-1;i>=startPosition;i--)
                    chatItems.add(0,mChatItemList.get(i));

                publishProgress(chatItems);
                return null;
            }

            @Override
            protected void onProgressUpdate(List<ChatItem>... values) {
                Log.e(TAG,"load successfully!");
                if(values.length==1) {
                    Log.e(TAG, "len=" + values[0].size());
                    StringBuilder stringBuilder=new StringBuilder();
                    for(ChatItem chatItem:values[0])
                        stringBuilder.append(chatItem.getContent()+" ");
                    Log.e(TAG,"loadMess="+stringBuilder.toString());
                    mShownList.addAll(0,values[0]);
                    notifyItemRangeInserted(0,values[0].size());
                    if(mIsFirstLoaded){
                        moveToNewItem();
                        mIsFirstLoaded=false;
                    }
                }
                else
                    Toast.makeText(mActivity,"没有更多历史消息了。",Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }.execute();
    }

    public void setField(RecyclerView recyclerView,Activity activity,SwipeRefreshLayout swipeRefreshLayout){
        mRecyclerView=recyclerView;
        mActivity=activity;
        mSwipeRefreshLayout=swipeRefreshLayout;
    }

    /**
     * 加入一条消息
     * @param chatItem 加入的消息
     */
    public void addNewChatItem(ChatItem chatItem){
        mShownList.add(chatItem);
        //notifyDataSetChanged();
        if(Looper.getMainLooper()!=Looper.myLooper()){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyItemInserted(mShownList.size()-1);
                    moveToNewItem();
                }
            });
        }
        else {
            notifyItemInserted(mShownList.size()-1);
            moveToNewItem();
        }
    }

    /**
     * dp值转像素
     * @param dpValue dp值
     * @return 像素值
     */
    private int dpToPx(int dpValue){
        if(scale<=0)
            scale=mActivity.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatItem chatItem=mShownList.get(position);
        Log.e(TAG,"position="+position+",s="+chatItem.getContent());
        View view= holder.mView;
        Glide.with(view.getContext()).load(chatItem.getProfileUri()).into(holder.mImageView_profile);
        int height=0;
        switch (chatItem.getChatType()){
            case TEXT:
                TextView textView=(TextView)view.findViewById(R.id.chat_item_text);
                textView.setText(chatItem.getContent());
                break;
            case IMAGE:
                ImageView imageView=(ImageView)view.findViewById(R.id.chat_item_image);
                Bitmap bitmap=BitmapFactory.decodeFile(chatItem.getContent());
                height=bitmap.getHeight();
                if(bitmap.getWidth()>width_content-dpToPx(48)){
                    int w=width_content-dpToPx(48);
                    height=height*w/bitmap.getWidth();
                    Glide.with(view.getContext()).load(chatItem.getContent()).override(w,height).into(imageView);
                }
                else
                    Glide.with(view.getContext()).load(chatItem.getContent()).into(imageView);
                bitmap=null;
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatItem chatItem=mShownList.get(position);
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
        return mShownList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView mTextView_time;
        ImageView mImageView_profile;

        public ViewHolder(View view){
            super(view);
            mView=view;
            mTextView_time=(TextView)view.findViewById(R.id.chat_item_textView_time);
            mImageView_profile=(ImageView)view.findViewById(R.id.chat_item_profile);
        }
    }
}
