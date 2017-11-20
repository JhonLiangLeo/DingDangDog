package com.app.debrove.tinpandog.groups.datebase;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

/**
 * Created by NameTooLong on 2017/11/7.
 *
 * 存储群成员的具体信息于本地的实例
 */

public class GroupsMemberInformation extends DataSupport {
    @Column(ignore = true)
    public static final String  GROUPS_NAME="GROUPS_NAME";

    /**
     * 群成员所加入的群名
     */
    private String groupsName;

    /**
     * 群成员名
     */
    private String userName;

    /**
     * 群成员头像图片的路径
     */
    private String profileImagePath;

    public String getGroupsName() {
        return groupsName;
    }

    public void setGroupsName(String groupsName) {
        this.groupsName = groupsName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
}
