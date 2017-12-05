package com.app.debrove.tinpandog.groups;

import android.os.AsyncTask;

import com.app.debrove.tinpandog.BaseApplication;
import com.app.debrove.tinpandog.datebase.ChatInformation;
import com.app.debrove.tinpandog.datebase.GroupsMemberInformation;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NameTooLong on 2017/11/9.
 *
 * {@link GroupsActivity}创建时用于加载本地存储的聊天信息
 */

public class LoadChatTask extends AsyncTask<Void,List<ChatItem>,Void> {
    private String mUserName,mGroupsName;
    private ChatItemAdapter mChatItemAdapter;

    /**
     *
     * @param userName 用户名
     * @param groupsName 用户打开的群名
     * @param chatItemAdapter 展示聊天信息的{@link android.support.v7.widget.RecyclerView}
     *                        的{@link android.support.v7.widget.RecyclerView.Adapter}
     */
    public LoadChatTask(String userName,String groupsName,ChatItemAdapter chatItemAdapter){
        mUserName=userName;
        mGroupsName=groupsName;
        mChatItemAdapter=chatItemAdapter;
    }

    /**
     *将之前的聊天信息加载
     * 优先从本地加载，若不存在，则从网络中加载
     */
    @Override
    protected Void doInBackground(Void...voids) {
        LitePal.getDatabase();
        List<ChatInformation> list= DataSupport.where("groupsName='"+mGroupsName+"'").find(ChatInformation.class);
        List<ChatItem> chatItemList=null;
        if(list.isEmpty())
            chatItemList=getChatItemFromNet();
        else {
            chatItemList=new ArrayList<>();
            ChatItem chatItem=null;
            for(ChatInformation chatInformation:list){
                chatItem=new ChatItem(chatInformation.getProfileUri(),chatInformation.isIs_others());
                switch (chatInformation.getChatType()){
                    case ChatInformation.TEXT:
                        chatItem.setContent(chatInformation.getContent(), ChatItem.ChatType.TEXT);
                        break;
                    case ChatInformation.IMAGE:
                        chatItem.setContent(chatInformation.getContent(),ChatItem.ChatType.IMAGE);
                        break;
                }
                chatItemList.add(chatItem);
            }
        }
        publishProgress(chatItemList);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mChatItemAdapter=null;
        mGroupsName=null;
        mUserName=null;
    }

    @Override
    protected void onProgressUpdate(List<ChatItem>... values) {
        mChatItemAdapter.addNewChatItem(values[0]);
    }


    /**
     *如果本地不存在相关的聊天信息，则从网络中加载
     */
    private List<ChatItem> getChatItemFromNet(){
        List<ChatItem> chatItemList=new ArrayList<>();
        List<EMMessage> emMessageList=null;
        try {
            EMConversation emConversation = EMClient.getInstance().chatManager().getConversation(mUserName);
            emMessageList = emConversation.getAllMessages();
        }
        catch (Exception e){
            return chatItemList;
        }
        ChatItem chatItem=null;
        for(EMMessage emMessage:emMessageList){
            /**非本组信息不加载*/
            if(!mGroupsName.equals(emMessage.getStringAttribute(GroupsMemberInformation.GROUPS_NAME,"")))
                continue;
            chatItem=new ChatItem(mGroupsName,true);
            switch (emMessage.getType()){
                case TXT:
                    chatItem.setContent(((EMTextMessageBody)emMessage.getBody()).getMessage()
                            ,ChatItem.ChatType.TEXT);
                    break;
                case IMAGE:
                    String imagePath=ImageLoader.saveImage( ((EMImageMessageBody)emMessage.getBody()).getThumbnailUrl() , BaseApplication.getContext());
                    chatItem.setContent(imagePath, ChatItem.ChatType.IMAGE);
                    break;
            }
            chatItemList.add(chatItem);
        }
        return chatItemList;
    }
}
