package com.app.debrove.tinpandog.groups;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.app.debrove.tinpandog.datebase.ChatInformation;
import com.app.debrove.tinpandog.util.ShareUtils;
import com.app.debrove.tinpandog.util.StaticClass;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.exceptions.HyphenateException;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by NameTooLong on 2017/12/4.
 *
 * 显示群组信息的{@link RecyclerView}的{@link android.support.v7.widget.RecyclerView.Adapter}
 */

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder>{
    private static final String TAG = "GroupsAdapter";
    private List<EMGroup> mEMGroupList,mShownEMGroupList=new ArrayList<>();
    private int mPageSize=5;
    private boolean mIsLoadFailed=false;
    private Activity mActivity;
    private static AsyncTask<Void,Boolean,Void> mRefreshTask;
    /**
     * 本地没有群组信息时从服务器加载并保存至本地
     */
    private AsyncTask<Void,Void,Void> mAsycTask=new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mEMGroupList= EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
            }
            catch (HyphenateException e){
                e.printStackTrace();
                return null;
            }
            if(mEMGroupList==null&&mEMGroupList.isEmpty())
                return null;
            for(int i=0;i<mPageSize&&i<mEMGroupList.size();i++)
                mShownEMGroupList.add(mEMGroupList.get(i));
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView mTextView_groupsRealName,mTextView_lastChatInformation,mTextView_lastChatInformationTime;
        String groupsName,groupsRealName;
        ImageView mImageView;

        public ViewHolder(View view){
            super(view);
            mTextView_lastChatInformation=(TextView)view.findViewById(R.id.groups_item_textView_lastChatInformation);
            mTextView_groupsRealName=(TextView)view.findViewById(R.id.groups_item_textView_groupsRealName);
            mTextView_lastChatInformationTime=(TextView)view.findViewById(R.id.groups_item_textView_lastChatInformationTime);
            mImageView=(ImageView)view.findViewById(R.id.groups_item_imageView);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String userName= ShareUtils.getString(mActivity, StaticClass.KEY_USER_NUM,"");
            Intent intent=new Intent(mActivity,GroupsActivity.class);
            GroupsActivity.startGroupsActivity(new String[]{groupsName,"","",groupsRealName},mActivity);
        }
    }


    /**
     * 优先本地加载群组信息，若没有，则再从服务器加载
     * @param activity 当前活动，用于启动{@link GroupsActivity}时的参数
     */
    public GroupsAdapter(Activity activity){
        mActivity=activity;
        mEMGroupList=EMClient.getInstance().groupManager().getAllGroups();
        if(mEMGroupList.isEmpty())
            mAsycTask.execute();
        else{
            for(int i=0;i<mPageSize&&i<mEMGroupList.size();i++)
                mShownEMGroupList.add(mEMGroupList.get(i));
        }
    }

    /**
     * 当前视图不在可见时，停止刷新群组信息
     */
    public void onPause(){
        if(mRefreshTask!=null)
            mRefreshTask.cancel(true);
    }

    /**
     * 上拉时刷新群组信息
     * @param swipeRefreshLayout 上拉的控件
     */
    public void onRefresh(final SwipeRefreshLayout swipeRefreshLayout){
        mRefreshTask=new AsyncTask<Void,Boolean,Void>(){
            OutTimeCancleThread mOutTimeCancleThread;

            @Override
            protected Void doInBackground(Void... params) {
                List<EMGroup> emGroups=null;
                mOutTimeCancleThread=new OutTimeCancleThread(mRefreshTask);
                mOutTimeCancleThread.start();
                try{
                    emGroups=EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
                }
                catch (HyphenateException e){
                    e.printStackTrace();
                    publishProgress(false);
                    return null;
                }
                if(emGroups.size()!=mEMGroupList.size()) {
                    mEMGroupList=emGroups;
                    mShownEMGroupList.clear();
                    for(int i=0;i<mPageSize&&i<mEMGroupList.size();i++)
                        mShownEMGroupList.add(mEMGroupList.get(i));
                    publishProgress(true);
                }
                return null;
            }

            @Override
            protected void onCancelled() {
                swipeRefreshLayout.setRefreshing(false);
                if(mIsLoadFailed) {
                    Toast.makeText(mActivity, "加载失败！", Toast.LENGTH_SHORT).show();
                    mIsLoadFailed=false;
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if(mOutTimeCancleThread!=null)
                    mOutTimeCancleThread.interrupt();
            }

            @Override
            protected void onProgressUpdate(Boolean... values) {
                if(values[0])
                    notifyDataSetChanged();
                else
                    Toast.makeText(mActivity, "加载失败！", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        }.execute();
    }

    /**
     * 下拉时展示更多的群组信息
     * 初始加载时，考虑性能，未全部展示
     */
    public void onLoadMore(){
        if(mEMGroupList.size()<=mShownEMGroupList.size())
            return;
        new AsyncTask<Void,Integer,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                int startPosition=mShownEMGroupList.size();
                int expectedEndPosition=startPosition+mPageSize,realEndPosition=-1;
                for(int i=startPosition;i<expectedEndPosition&&i<mEMGroupList.size();i++) {
                    mShownEMGroupList.add(mEMGroupList.get(i));
                    realEndPosition=i;
                }
                publishProgress(startPosition,realEndPosition-startPosition+1);
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                Log.e(TAG,"load successfully!");
                notifyItemRangeInserted(values[0],values[1]);
            }
        }.execute();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EMGroup emGroup=mShownEMGroupList.get(position);
        holder.mTextView_groupsRealName.setText(emGroup.getGroupName());
        holder.groupsName=emGroup.getGroupId();
        holder.groupsRealName=emGroup.getGroupName();

        ChatInformation chatInformation=DataSupport.where("groupsName=?",emGroup.getGroupId()).findLast(ChatInformation.class);
        if(chatInformation!=null) {
            String name=chatInformation.getUserName();
            if(name.length()>=5)
                name=name.substring(0,5)+"...";
            holder.mTextView_lastChatInformation.setText(name+ "：");
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy/mm/dd hh:mm");
            Date date=new Date(chatInformation.getTime());
            holder.mTextView_lastChatInformationTime.setText(simpleDateFormat.format(date));
            switch (chatInformation.getChatType()) {
                case ChatInformation.TEXT:
                    String s[]=chatInformation.getContent().split("\n");
                    if(s[0].length()<=10) {
                        holder.mTextView_lastChatInformation.append(s[0]);
                        if(s.length!=1)
                            holder.mTextView_lastChatInformation.append("...");
                    }
                    else
                        holder.mTextView_lastChatInformation.append(s[0].substring(0,10)+"...");
                    break;
                case ChatInformation.IMAGE:
                    holder.mTextView_lastChatInformation.append("[图片]");
                    break;
            }
        }
        else {
            holder.mTextView_lastChatInformation.setText("没有新消息");
            holder.mTextView_lastChatInformationTime.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return mShownEMGroupList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_item,parent,false);
        return new ViewHolder(view);
    }

    /**
     * 群组刷新的计时器，固定时间后终止刷新线程，并告知用户刷新失败
     */
    private class OutTimeCancleThread extends Thread{
        AsyncTask<Void,Boolean,Void> mAsyncTask;

        private OutTimeCancleThread(AsyncTask<Void,Boolean,Void> asyncTask){
            mAsyncTask=asyncTask;
        }

        @Override
        public void run() {
            try{
                Thread.sleep(1000*10);
            }
            catch (InterruptedException e){
                e.printStackTrace();
                mIsLoadFailed=false;
                return;
            }
            if(mAsyncTask!=null) {
                mIsLoadFailed=true;
                mAsyncTask.cancel(true);
            }
        }
    }
}
