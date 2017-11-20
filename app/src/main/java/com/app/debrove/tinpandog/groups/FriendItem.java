package com.app.debrove.tinpandog.groups;

/**
 * Created by NameTooLong on 2017/11/4.
 *
 * {@link FriendAdapter}存储信息所用的类
 */

public class FriendItem {
    /**
     *该好友在创建群聊时是否被选中
     */
    private boolean mIsChosen=false;

    /**
     * 好友头像的图片路径
     */
    private String mProfilePath;

    /**
     * 好友名
     */
    private String mFriendName;

    public FriendItem(String profilePath,String friendName){
        mFriendName=friendName;
        mProfilePath=profilePath;
    }

    public boolean isChosen() {
        return mIsChosen;
    }

    public void setChosen(boolean chosen) {
        mIsChosen = chosen;
    }

    public String getProfilePath() {
        return mProfilePath;
    }

    public String getFriendName() {
        return mFriendName;
    }
}
