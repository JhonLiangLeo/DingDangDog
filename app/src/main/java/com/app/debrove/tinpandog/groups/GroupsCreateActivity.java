package com.app.debrove.tinpandog.groups;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.debrove.tinpandog.R;
import com.app.debrove.tinpandog.groups.datebase.GroupsInformation;
import com.app.debrove.tinpandog.groups.datebase.GroupsMemberInformation;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chat.EMGroupOptions;
import com.hyphenate.chat.EMMessage;
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

    private FriendAdapter mFriendAdapter;
    private List<FriendItem> mFriendItemList;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_create);
        mRecyclerView=(RecyclerView)findViewById(R.id.activity_groupsCreate_recyclerView);
        mButton=(Button)findViewById(R.id.activity_groupsCreate_button_confirm);
        mButton.setOnClickListener(this);
        mEditText_groupsName=(EditText)findViewById(R.id.activity_groupsCreate_editText_groupsName);

        initFriendList();
        mFriendAdapter=new FriendAdapter(mFriendItemList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mFriendAdapter);
    }

    /**
     *加载好友列表中的好友，用于选择创建群聊成员
     */
    private void initFriendList(){
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
                friendItem=new FriendItem(groupsMemberInformationList.get(0).getProfileImagePath(),name);
                mFriendItemList.add(friendItem);
            }
        }
        else
            Toast.makeText(this,"You've no friends.",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        String groupsName=mEditText_groupsName.getText().toString();
        if("".equals(groupsName)){
            Toast.makeText(this,"群名不可为空！",Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> groupsMemberName=new ArrayList<>();
        for(FriendItem friendItem:mFriendItemList){
            if(friendItem.isChosen()) {
                groupsMemberName.add(friendItem.getFriendName());
                new SendGroupInviteMessThread(friendItem.getFriendName(),groupsName).start();
            }
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
        try{
            EMClient.getInstance().groupManager().createGroup(groupsName, "",names,"", option);
            GroupsInformation groupsInformation=new GroupsInformation();
            groupsInformation.setGroupsName(groupsName);
            groupsInformation.save();
            Intent intent=new Intent(this,GroupsActivity.class);
            intent.putExtra(GroupsActivity.GROUPS_NAME,groupsName);
            //intent.putExtra(GroupsActivity.USER_NAME,);
            //intent.putExtra(GroupsActivity.USER_PROFILE_PATH,);
            startActivity(intent);
            finish();
        }
        catch (HyphenateException e){
            e.printStackTrace();
        }
    }

    private class SendGroupInviteMessThread extends Thread{
        String mUserName,mGroupsName;

        private SendGroupInviteMessThread(String userName,String groupsName){
            mUserName=userName;
            mGroupsName=groupsName;
        }

        @Override
        public void run() {
            EMMessage emMessage=EMMessage.createSendMessage(EMMessage.Type.CMD);
            emMessage.setChatType(EMMessage.ChatType.GroupChat);
            emMessage.setTo(mUserName);
            emMessage.setAttribute(GroupsMemberInformation.GROUPS_NAME,mGroupsName);
            emMessage.addBody(new EMCmdMessageBody(MessageListener.CmdAction.INVITE_INTO_GROUP));
            EMClient.getInstance().chatManager().sendMessage(emMessage);
        }
    }
}
