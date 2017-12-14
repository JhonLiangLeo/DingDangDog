package com.app.debrove.tinpandog.groups;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.debrove.tinpandog.R;
import com.app.debrove.tinpandog.datebase.GroupsMemberInformation;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chat.EMGroupOptions;
import com.hyphenate.exceptions.HyphenateException;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NameTooLong on 2017/11/4.
 *
 * 群聊创建所用的{@link Activity}
 * 创建成功后自动跳转至{@link GroupsActivity}
 */

public class GroupsCreateActivity extends Activity implements View.OnClickListener{
    private RecyclerView mRecyclerView;
    private Button mButton;
    private EditText mEditText_groupsName;
    private TextView mTextView;

    private FriendAdapter mFriendAdapter;
    private List<FriendItem> mFriendItemList;
    private LoadFriendsFromNetTask mLoadFriendsFromNetTask;
    private SendGroupsInformationToServerThread mSendGroupsInformationToServerThread;
    private String mUserName,mProfilePath;

    private static final String TAG = "GroupsCreateActivity";

    private static final String USER_NAME="userName";
    private static final String PROFILE_PATH="profilePath";
    private static final String ACTIVITY_STARTED_BY_NEW_METHOD="method:startGroupsCreateActivity";

    @Override
    protected void onCreate ( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_create);

        Intent intent=getIntent();
        mProfilePath=intent.getStringExtra(PROFILE_PATH);
        mUserName=intent.getStringExtra(USER_NAME);
        if(!intent.getBooleanExtra(ACTIVITY_STARTED_BY_NEW_METHOD,false)) {
            Log.e(TAG, "use method:GroupsCreateActivity.startGroupsCreateActivity to start this activity");
            finish();
        }

        mRecyclerView=(RecyclerView)findViewById(R.id.activity_groupsCreate_recyclerView);
        mButton=(Button)findViewById(R.id.activity_groupsCreate_button_confirm);
        mButton.setOnClickListener(this);
        mEditText_groupsName=(EditText)findViewById(R.id.activity_groupsCreate_editText_groupsName);
        mTextView=(TextView)findViewById(R.id.activity_groupsCreate_textView);

        mLoadFriendsFromNetTask=new LoadFriendsFromNetTask();
        mLoadFriendsFromNetTask.execute();
    }

    public static void startGroupsCreateActivity(String s[],Activity activity){
        Log.e(TAG,"s.length="+s.length);
        if(s.length!=2)
            throw new IllegalArgumentException("Two strings are required.");
        Intent intent=new Intent(activity,GroupsCreateActivity.class);
        intent.putExtra(USER_NAME,s[0]);
        intent.putExtra(PROFILE_PATH,s[1]);
        intent.putExtra(ACTIVITY_STARTED_BY_NEW_METHOD,true);
        activity.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoadFriendsFromNetTask!=null)
            mLoadFriendsFromNetTask.cancel(true);
    }

    @Override
    public void onClick(View v) {
        if(mSendGroupsInformationToServerThread!=null&&mSendGroupsInformationToServerThread.isAlive()){
            Toast.makeText(this,"点击过于频繁，请之后重试。",Toast.LENGTH_SHORT).show();
            return;
        }
        String groupsName=mEditText_groupsName.getText().toString();
        if("".equals(groupsName)){
            Toast.makeText(this,"群名不可为空！",Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> groupsMemberName=new ArrayList<>();
        for(FriendItem friendItem:mFriendItemList){
            if(friendItem.isChosen())
                groupsMemberName.add(friendItem.getFriendName());
        }
        if(groupsMemberName.isEmpty()) {
            Toast.makeText(this,"群聊人数不可为零！",Toast.LENGTH_SHORT).show();
            return;
        }
        EMGroupOptions option = new EMGroupOptions();
        option.maxUsers = 200;
        option.style = EMGroupManager.EMGroupStyle.EMGroupStylePrivateMemberCanInvite;
        String names[]=new String[groupsMemberName.size()];
        for (int i=0;i<names.length;i++)
            names[i]=groupsMemberName.get(i);
        mSendGroupsInformationToServerThread=new SendGroupsInformationToServerThread(new String[]{groupsName,"",""},names,option);
        mSendGroupsInformationToServerThread.start();
    }

    private class LoadFriendsFromNetTask extends AsyncTask<Void,Boolean,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            mFriendItemList=new ArrayList<>();
            List<String> friendNameList=null;
            try {
                friendNameList=EMClient.getInstance().contactManager().getAllContactsFromServer();
            }
            catch (HyphenateException e){
                e.printStackTrace();
            }
            if(friendNameList!=null){
                FriendItem friendItem=null;
                List<GroupsMemberInformation> groupsMemberInformationList=null;
                for(String name:friendNameList){
                    groupsMemberInformationList= DataSupport.select("profileImagePath").find(GroupsMemberInformation.class);
                    if(!groupsMemberInformationList.isEmpty())
                        friendItem=new FriendItem(groupsMemberInformationList.get(0).getProfileImagePath(),name);
                    else
                        friendItem=new FriendItem("",name);
                    mFriendItemList.add(friendItem);
                }
                publishProgress(true);
            }
            else
                publishProgress(false);
            return null;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            if(values[0]) {
                mFriendAdapter = new FriendAdapter(mFriendItemList);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(GroupsCreateActivity.this));
                mRecyclerView.setAdapter(mFriendAdapter);
            }
            else
                Toast.makeText(GroupsCreateActivity.this,"You've no friends.",Toast.LENGTH_SHORT).show();
            mTextView.setText("加载完成");
        }
    }

    private class SendGroupsInformationToServerThread extends Thread{
        String mString[],mNames[];
        EMGroupOptions mEMGroupOptions;

        private SendGroupsInformationToServerThread(String s[],String names[],EMGroupOptions emGroupOptions){
            mString=s;
            mNames=names;
            mEMGroupOptions=emGroupOptions;
        }

        @Override
        public void run() {
            try{
                EMGroup emGroup=EMClient.getInstance().groupManager().createGroup(mString[0], "desc",mNames,"reason", mEMGroupOptions);
                String groupsName=emGroup.getGroupId();

                if(!GroupsCreateActivity.this.isDestroyed()) {
                    GroupsActivity.startGroupsActivity(new String[]{groupsName,mUserName,mProfilePath,mString[0]},GroupsCreateActivity.this);
                    finish();
                }
            }
            catch (HyphenateException e){
                e.printStackTrace();
            }
        }
    }
}
