package com.app.debrove.tinpandog.groups;

/**
 * Created by NameTooLong on 2017/10/29.
 *
 * {@link ChatItemAdapter#mList}中的泛型参数，
 * 存储相关的聊天信息
 */

public class ChatItem {
    /**
     *聊天者的头像图片路径
     */
    private String mProfileUri;

    /**
     * 具体的聊天内容
     */
    private String mContent;

    /**
     * 该消息是否是用户本人发出的
     * true，其他人发送的；false，用户发送的
     */
    private boolean mIs_others;

    /**
     * 具体的聊天类型
     */
    private ChatType mChatType;

    public enum ChatType{
        IMAGE,
        TEXT
    }

    public ChatItem(String profileUri,boolean is_others){
        mProfileUri=profileUri;
        mIs_others=is_others;
    }

    public ChatType getChatType() {
        return mChatType;
    }

    public void setContent(String content,ChatType chatType) {
        mChatType = chatType;
        mContent=content;
    }

    public String getProfileUri() {
        return mProfileUri;
    }

    public String getContent() {
        return mContent;
    }

    public boolean is_others() {
        return mIs_others;
    }
}
