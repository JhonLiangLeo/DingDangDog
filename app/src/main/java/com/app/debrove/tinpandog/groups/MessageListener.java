package com.app.debrove.tinpandog.groups;

import android.content.Context;
import android.util.Log;

import com.app.debrove.tinpandog.datebase.ChatInformation;
import com.app.debrove.tinpandog.datebase.GroupsMemberInformation;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NameTooLong on 2017/10/29.
 *
 * 1.使用环信API的聊天信息接受接口
 * 2.未被接入实际代码中
 * 3.实现功能：（1）群聊信息的接收（图片、文字）
 *            （2）被拉入某群聊的处理（直接加入）
 */

public class MessageListener implements EMMessageListener {
    private static final String TAG = "MessageListener";
    /**
     * 群聊信息的{@link android.support.v7.widget.RecyclerView.Adapter}
     */
    private ChatItemAdapter mChatItemAdapter;

    /**
     * 与用户正在交互的群聊名
     */
    private String mGroupsName;
    private Context mContext;

    public MessageListener(ChatItemAdapter chatItemAdapter,String groupsName,Context context){
        mChatItemAdapter=chatItemAdapter;
        mGroupsName=groupsName;
        mContext=context;
    }

    @Override
    public void onCmdMessageReceived(List<EMMessage> list) {
        Log.e(TAG,"onCmdMessageReceived");
    }

    @Override
    public void onMessageChanged(EMMessage emMessage, Object o) {

    }

    @Override
    public void onMessageDelivered(List<EMMessage> list) {

    }

    @Override
    public void onMessageRead(List<EMMessage> list) {

    }

    @Override
    public void onMessageRecalled(List<EMMessage> list) {

    }

    @Override
    public void onMessageReceived(List<EMMessage> list) {
        Log.e(TAG,"onMessageReceived");
        EMMessage emMessage=null;
        List<ChatItem> chatItemList=new ArrayList<>();
        ChatItem chatItem=null;
        ChatInformation chatInformation=null;
        for(int i=0;i<list.size();i++){
            emMessage=list.get(i);
            String groupsName=emMessage.getStringAttribute(GroupsMemberInformation.GROUPS_NAME,"");
            if("".equals(groupsName))
                continue;
            String userName=emMessage.getFrom();
            List<GroupsMemberInformation> groupsMemberInformationList= DataSupport.where("groupsName=? and userName=?",mGroupsName,userName).select("profileImagePath").find(GroupsMemberInformation.class);
            /*if(groupsMemberInformationList.isEmpty()) {
                Log.e(TAG,"user "+userName+" no exists");
                continue;
            }
            String profileImagePath=groupsMemberInformationList.get(i).getProfileImagePath();*/
            String profileImagePath="";
            chatItem=new ChatItem(
                    profileImagePath,true);

            chatInformation=new ChatInformation();
            chatInformation.setGroupsName(groupsName);
            chatInformation.setIs_others(true);
            chatInformation.setProfileUri(profileImagePath);
            chatInformation.setUserName(userName);
            chatInformation.setMsgId(emMessage.getMsgId());
            chatInformation.setTime(emMessage.getMsgTime());
            switch (emMessage.getType()){
                case TXT:
                    chatInformation.setChatType(ChatInformation.TEXT);
                    chatInformation.setContent(((EMTextMessageBody)emMessage.getBody()).getMessage());
                    chatItem.setContent(((EMTextMessageBody)emMessage.getBody()).getMessage()
                            ,ChatItem.ChatType.TEXT);
                    break;
                case IMAGE:
                    String imagePath=ImageLoader.saveImage(((EMImageMessageBody)emMessage.getBody())
                            .getThumbnailUrl(),mContext);
                    chatInformation.setChatType(ChatInformation.IMAGE);
                    chatInformation.setContent(imagePath);
                    chatItem.setContent(imagePath,ChatItem.ChatType.IMAGE);
                    break;
            }
            chatInformation.save();
            if(mGroupsName.equals(groupsName))
                chatItemList.add(chatItem);
            else
                chatItem=null;
        }
        mChatItemAdapter.addNewChatItem(chatItemList);
    }


    public class CmdAction{
        public static final String INVITE_INTO_GROUP="invite into group";
        public static final String LEAVE_GROUP="leave group";
    }
}
