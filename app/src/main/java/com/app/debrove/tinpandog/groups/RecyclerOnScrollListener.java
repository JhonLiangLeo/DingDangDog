package com.app.debrove.tinpandog.groups;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by NameTooLong on 2017/12/13.
 *
 * 群组下拉加载所使用的监视器
 */

public abstract class RecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    protected LinearLayoutManager mLinearLayoutManager;
    protected boolean mIsLoading=false;
    protected int mFirstVisibleItem,mVisibleItemCount,mTotalItemCount;
    protected int mLoadedItemCount=0;

    public RecyclerOnScrollListener(LinearLayoutManager linearLayoutManager){
        mLinearLayoutManager=linearLayoutManager;
    }

    /**
     * 判断群组是否需要进行下拉加载
     * @param recyclerView 同父类
     * @param dx 同父类
     * @param dy 同父类
     */
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        mVisibleItemCount=recyclerView.getChildCount();
        mTotalItemCount=mLinearLayoutManager.getItemCount();
        mFirstVisibleItem=mLinearLayoutManager.findFirstVisibleItemPosition();
        if(mIsLoading){
            if(mLoadedItemCount!=mTotalItemCount){
                mIsLoading=false;
                mLoadedItemCount=mTotalItemCount;
            }
        }
        else if(mTotalItemCount-mVisibleItemCount<=mFirstVisibleItem){
            onLoadMore();
            mIsLoading=true;
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
    }

    /**
     * 下拉加载的具体实现
     */
    public abstract void onLoadMore();
}
