package com.app.debrove.tinpandog.datebase;

import org.litepal.annotation.Column;
import org.litepal.annotation.Encrypt;
import org.litepal.crud.DataSupport;

/**
 * Created by NameTooLong on 2017/11/14.
 *
 * 存储用户加入的群的信息
 * 不含具体的群成员信息，群成员信息见于包com.app.debrove.tinpandog.groups.datebase中GroupsMemberInformation类
 */

public class GroupsInformation extends DataSupport {
    /**
     * 群ID
     * Notice：环信在这里注释的有问题，其文档中groupsName是指的群ID，非群名。
     * 由于历史原因，不做修改，与其保持一致。
     */
    @Column(unique = true)
    private String groupsName;

    /**
     * 群名称
     */
    private String groupsRealName;

    public String getGroupsRealName() {
        return groupsRealName;
    }

    public void setGroupsRealName(String groupsRealName) {
        this.groupsRealName = groupsRealName;
    }

    public String getGroupsName() {
        return groupsName;
    }

    public void setGroupsName(String groupsName) {
        this.groupsName = groupsName;
    }
}
