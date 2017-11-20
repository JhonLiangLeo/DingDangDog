package com.app.debrove.tinpandog.groups.datebase;

import org.litepal.crud.DataSupport;

/**
 * Created by NameTooLong on 2017/11/14.
 *
 * 存储用户加入的群的信息
 * 不含具体的群成员信息，群成员信息见于包com.app.debrove.tinpandog.groups.datebase中GroupsMemberInformation类
 */

public class GroupsInformation extends DataSupport {
    /**
     * 群名称
     */
    private String groupsName;

    public String getGroupsName() {
        return groupsName;
    }

    public void setGroupsName(String groupsName) {
        this.groupsName = groupsName;
    }
}
