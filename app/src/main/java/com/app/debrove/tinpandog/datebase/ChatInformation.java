package com.app.debrove.tinpandog.datebase;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

/**
 * Created by NameTooLong on 2017/11/4.
 *
 * 本地存储的聊天信息的实例
 */

public class ChatInformation extends DataSupport {
    @Column(ignore = true)
    public static final int TEXT=1;
    @Column(ignore = true)
    public static final int IMAGE=2;

    /**
     * 聊天者的头像路径
     */
    private String profileUri;

    /**
     * 聊天的具体内容
     */
    private String content;

    /**
     * 聊天者的名字
     */
    private String userRealName;

    /**
     * 聊天者的组ID
     */
    private String groupsName;

    /**
     * 聊天的消息ID（环信）
     */
    private String msgId;

    /**
     * 聊天发送者的名字
     */
    private String userName;

    /**
     *是否为用户本人发送
     * true，他人发送；false，用户本人
     */
    private boolean is_others;

    /**
     * 聊天类型
     */
    private int chatType;

    /**
     * 信息发送时间
     */
    private long time;

    public String getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(String profileUri) {
        this.profileUri = profileUri;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getGroupsName() {
        return groupsName;
    }

    public void setGroupsName(String groupsName) {
        this.groupsName = groupsName;
    }

    public boolean isIs_others() {
        return is_others;
    }

    public void setIs_others(boolean is_others) {
        this.is_others = is_others;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
