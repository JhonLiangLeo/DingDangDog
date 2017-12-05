package com.app.debrove.tinpandog.groups;

import android.util.Log;

import com.app.debrove.tinpandog.datebase.GroupsInformation;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMucSharedFile;
import com.hyphenate.exceptions.HyphenateException;


import java.util.List;

/**
 * Created by NameTooLong on 2017/11/26.
 *
 * 1.群组情况变化的监听器
 * 2.仅在{@link GroupsActivity}中实现，即在其它群组中时可能会有群组情况信息的丢失
 */

public class GroupChangeListener implements EMGroupChangeListener {
    private static final String TAG = "GroupChangeListener";

    @Override
    public void onInvitationReceived(String s, String s1, String s2, String s3) {
        Log.e(TAG,"onInvitationReceived");
        try {
            EMClient.getInstance().groupManager().joinGroup(s);
        }
        catch (HyphenateException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestToJoinReceived(String s, String s1, String s2, String s3) {

    }

    @Override
    public void onRequestToJoinAccepted(String s, String s1, String s2) {

    }

    @Override
    public void onRequestToJoinDeclined(String s, String s1, String s2, String s3) {

    }

    @Override
    public void onInvitationAccepted(String s, String s1, String s2) {

    }

    @Override
    public void onInvitationDeclined(String s, String s1, String s2) {

    }

    @Override
    public void onUserRemoved(String s, String s1) {

    }

    @Override
    public void onGroupDestroyed(String s, String s1) {

    }

    @Override
    public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
        Log.e(TAG,"onAutoAcceptInvitationFromGroup");
        try {
            EMClient.getInstance().groupManager().joinGroup(s);
            EMGroup emGroup=EMClient.getInstance().groupManager().getGroup(s);
            s2=emGroup.getGroupName();
        }
        catch (HyphenateException e){
            e.printStackTrace();
        }
        Log.e(TAG,"s="+s+",s1="+s1+",s2="+s2);
        if("".equals(s)||"".equals(s2))
            return;
        GroupsInformation groupsInformation=new GroupsInformation();
        groupsInformation.setGroupsName(s);
        groupsInformation.setGroupsRealName(s2);
        groupsInformation.save();
    }

    @Override
    public void onMuteListAdded(String s, List<String> list, long l) {

    }

    @Override
    public void onMuteListRemoved(String s, List<String> list) {

    }

    @Override
    public void onAdminAdded(String s, String s1) {

    }

    @Override
    public void onAdminRemoved(String s, String s1) {

    }

    @Override
    public void onOwnerChanged(String s, String s1, String s2) {

    }

    @Override
    public void onMemberJoined(String s, String s1) {

    }

    @Override
    public void onMemberExited(String s, String s1) {

    }

    @Override
    public void onAnnouncementChanged(String s, String s1) {

    }

    @Override
    public void onSharedFileAdded(String s, EMMucSharedFile emMucSharedFile) {

    }

    @Override
    public void onSharedFileDeleted(String s, String s1) {

    }
}
