package com.app.debrove.tinpandog.groups;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.app.debrove.tinpandog.R;
import com.app.debrove.tinpandog.location.BdLocationActivity;
import com.app.debrove.tinpandog.util.ShareUtils;
import com.app.debrove.tinpandog.util.StaticClass;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by debrove on 2017/7/17.
 * Package Name : com.app.debrove.tinpandog.groups
 */

public class GroupsFragment extends Fragment implements GroupsContract.View, Toolbar.OnMenuItemClickListener {

    private GroupsContract.Presenter mPresenter;

    /**
     * 显示群组信息的{@link RecyclerView}的{@link android.support.v7.widget.RecyclerView.Adapter}
     */
    private GroupsAdapter mGroupsAdapter;

    Unbinder unbinder;
    @BindView(R.id.toolbar_groups)
    Toolbar mToolbarGroups;
    @BindView(R.id.groups_frame_button_createNewGroup)
    Button mButton;
    @BindView(R.id.groups_frame_button_getLocation)
    Button mButton_getLocation;
    /**
     * 显示群组信息
     */
    @BindView(R.id.groups_frame_recyclerView_groups)
    RecyclerView mRecyclerView_groups;

    private DrawerLayout mDrawerLayout;

    public static GroupsFragment newInstance() {
        return new GroupsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        unbinder = ButterKnife.bind(this, view);

        initView();
        return view;
    }

    private void initView() {
        //Toolbar
        setHasOptionsMenu(true);
        mToolbarGroups.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        mToolbarGroups.inflateMenu(R.menu.menu_groups);
        mToolbarGroups.setTitle("群组");
        mToolbarGroups.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mToolbarGroups.setOnMenuItemClickListener(this);
        mDrawerLayout = getActivity().findViewById(R.id.drawer);

        mGroupsAdapter=new GroupsAdapter(getActivity());
        mRecyclerView_groups.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView_groups.setAdapter(mGroupsAdapter);
    }

    @Override
    public void setPresenter(GroupsContract.Presenter presenter) {
        if (presenter != null) {
            mPresenter = presenter;
        }
    }

    /**
     * 在重新进入时检查群组信息是否变动，以更新UI
     */
    @Override
    public void onStart() {
        super.onStart();
        mGroupsAdapter.onActivityStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    //菜单栏的点击事件
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                Toast.makeText(getContext(), "add", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private static final String TAG = "GroupsFragment";

    /**
     * 进入群组创建活动
     */
    @OnClick(R.id.groups_frame_button_createNewGroup)
    public void onButtonClicked(){
        String userName= ShareUtils.getString(getContext(), StaticClass.KEY_USER_NUM,"");
        GroupsCreateActivity.startGroupsCreateActivity(new String[]{userName,""},getActivity());
    }

    @OnClick(R.id.groups_frame_button_getLocation)
    public void onButtonGetLocationClicked(){
        Intent intent=new Intent(getActivity(),BdLocationActivity.class);
        getActivity().startActivity(intent);
    }
}
