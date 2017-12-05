package com.app.debrove.tinpandog.groups;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.debrove.tinpandog.R;
import com.app.debrove.tinpandog.datebase.ChatInformation;
import com.app.debrove.tinpandog.datebase.GroupsInformation;
import com.app.debrove.tinpandog.util.ShareUtils;
import com.app.debrove.tinpandog.util.StaticClass;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.exceptions.HyphenateException;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by NameTooLong on 2017/12/4.
 *
 * 显示群组信息的{@link RecyclerView}的{@link android.support.v7.widget.RecyclerView.Adapter}
 */

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder>{
    private static final String TAG = "GroupsAdapter";
    private List<GroupsInformation> mGroupsInformationList;
    private int mListSize=-1;
    private boolean isFirstCreated=true;
    private Activity mActivity;
    /**
     * 本地没有群组信息时从服务器加载并保存至本地
     */
    private AsyncTask<Void,Void,Void> mAsycTask=new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            List<EMGroup> list=null;
            try {
                list= EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
            }
            catch (HyphenateException e){
                e.printStackTrace();
                return null;
            }
            if(list==null&&list.isEmpty()){
                mListSize=0;
                return null;
            }
            for(EMGroup emGroup:list){
                GroupsInformation groupsInformation=new GroupsInformation();
                groupsInformation.setGroupsName(emGroup.getGroupId());
                groupsInformation.setGroupsRealName(emGroup.getGroupName());
                groupsInformation.save();
            }
            mGroupsInformationList=DataSupport.findAll(GroupsInformation.class);
            mListSize=mGroupsInformationList.size();
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView mTextView_groupsRealName,mTextView_lastChatInformation;
        String groupsName,groupsRealName;

        public ViewHolder(View view){
            super(view);
            mTextView_lastChatInformation=(TextView)view.findViewById(R.id.groups_item_textView_lastChatInformation);
            mTextView_groupsRealName=(TextView)view.findViewById(R.id.groups_item_textView_groupsRealName);
            mTextView_groupsRealName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String userName= ShareUtils.getString(mActivity, StaticClass.KEY_USER_NUM,"");
            Intent intent=new Intent(mActivity,GroupsActivity.class);
            GroupsActivity.startGroupsActivity(new String[]{groupsName,"","",groupsRealName},mActivity);
        }
    }

    /**
     * UI再次向用户展示时，判断群组信息是否变动并为此更新UI
     */
    public void onActivityStart(){
        if(isFirstCreated)
           isFirstCreated=false;
        else {
            mGroupsInformationList=DataSupport.findAll(GroupsInformation.class);
            if(mGroupsInformationList.size()!=mListSize){
                mListSize=mGroupsInformationList.size();
                notifyDataSetChanged();
            }
        }
    }

    /**
     * 优先本地加载群组信息，若没有，则再从服务器加载
     * @param activity 当前活动，用于启动{@link GroupsActivity}时的参数
     */
    public GroupsAdapter(Activity activity){
        mActivity=activity;
        mGroupsInformationList= DataSupport.findAll(GroupsInformation.class);
        if(mGroupsInformationList.isEmpty())
            mAsycTask.execute();
        else
            mListSize=mGroupsInformationList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GroupsInformation groupsInformation=mGroupsInformationList.get(position);
        ChatInformation chatInformation=DataSupport.where("groupsName=?",groupsInformation.getGroupsName()).findLast(ChatInformation.class);
        holder.mTextView_groupsRealName.setText("群名："+groupsInformation.getGroupsRealName());
        holder.groupsName=groupsInformation.getGroupsName();
        holder.groupsRealName=groupsInformation.getGroupsRealName();
        if(chatInformation!=null) {
            holder.mTextView_lastChatInformation.setText(chatInformation.getUserName() + "：");
            switch (chatInformation.getChatType()) {
                case ChatInformation.TEXT:
                    holder.mTextView_lastChatInformation.append(chatInformation.getContent());
                    break;
                case ChatInformation.IMAGE:
                    holder.mTextView_lastChatInformation.append("[图片]");
                    break;
            }
        }
        else
            holder.mTextView_lastChatInformation.setText("没有新消息");
    }

    @Override
    public int getItemCount() {
        return mGroupsInformationList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_item,parent,false);
        return new ViewHolder(view);
    }
}
