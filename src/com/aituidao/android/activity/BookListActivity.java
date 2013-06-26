package com.aituidao.android.activity;

import java.util.ArrayList;
import java.util.List;

import com.aituidao.android.R;
import com.aituidao.android.adapter.BookListAdapter;
import com.aituidao.android.data.Book;
import com.aituidao.android.helper.BookListHelper;
import com.aituidao.android.helper.BookListHelper.SortType;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.Bundle;
import android.view.View;
import android.app.Activity;

public class BookListActivity extends Activity {
	private PullToRefreshListView mBookListView;
	private BookListHelper mBookListHelper;
	private List<Book> mBookListData = new ArrayList<Book>();
	private BookListAdapter mListAdapter;
	private boolean mIsOnCreate = false;
	private View mSortByHotBtn;
	private View mSortByTimeBtn;
	
	private SortType mSortType = SortType.SORT_BY_TIME;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        
        initData();
        initUi();
        
        mIsOnCreate = true;
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    		super.onWindowFocusChanged(hasFocus);
    		
    		if (hasFocus && mIsOnCreate) {
    			mIsOnCreate = false;
    			startRefreshBySortType(mSortType);
    		}
    }
    
    private void initData() {
		mBookListHelper = new BookListHelper(this);
		mBookListHelper.setBookListHelperCB(new BookListHelper.BookListHelperCB() {
			@Override
			public void refreshBookListDataSuccess(List<Book> data) {
				enableSortTypeBtn();
				
				mBookListView.onRefreshComplete();
				
				mBookListData.clear();
				mBookListData.addAll(data);
				
				mListAdapter.clearLastItemPos();
				mListAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void refreshBookListDataError() {
				enableSortTypeBtn();
				
				mBookListView.onRefreshComplete();
				
				mListAdapter.clearLastItemPos();
				mListAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void loadMoreBookListDataSuccess(List<Book> data) {
				mBookListData.addAll(data);
				
				mListAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void loadMoreBookListDataError() {
				// do nothing
			}
		});
	}
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void initUi() {
    		mBookListView = (PullToRefreshListView) findViewById(R.id.book_list_view);
    		mBookListView.setScrollingWhileRefreshingEnabled(false);
    		mBookListView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			@Override
			public void onRefresh(PullToRefreshBase refreshView) {
				mBookListHelper.startRefreshBookListData(mSortType);
			}
    		});
    		
    		mListAdapter = new BookListAdapter(this, mBookListData);
    		mBookListView.setAdapter(mListAdapter);
    		
    		mSortByHotBtn = findViewById(R.id.book_sort_by_hot_iv);
    		mSortByTimeBtn = findViewById(R.id.book_sort_by_time_iv);
    		
    		mSortByHotBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startRefreshBySortType(SortType.SORT_BY_HOT);
			}
		});
    		
    		mSortByTimeBtn.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				startRefreshBySortType(SortType.SORT_BY_TIME);
    			}
    		});
    }
    
    private void startRefreshBySortType(SortType type) {
    		if (type == null) {
    			return;
    		}
    		
    		if (type != mSortType) {
    			mBookListData.clear();
    			mListAdapter.notifyDataSetChanged();
    		}
    		
    		mSortType = type;
    		
    		switch (mSortType) {
    		case SORT_BY_TIME:
    			mSortByHotBtn.setSelected(false);
    			mSortByTimeBtn.setSelected(true);
    			break;
    			
    		case SORT_BY_HOT:
    			mSortByHotBtn.setSelected(true);
    			mSortByTimeBtn.setSelected(false);
    			break;
    		}
    		
		mSortByHotBtn.setEnabled(false);
		mSortByTimeBtn.setEnabled(false);
		
		ILoadingLayout proxy = mBookListView.getLoadingLayoutProxy();
		String pullLabel = null;
		String refreshingLabel = null;
		String releaseLabel = null;
		switch (mSortType) {
		case SORT_BY_TIME:
			pullLabel = getString(R.string.sort_type_pull_label_for_time);
			refreshingLabel = getString(R.string.sort_type_refreshing_label_for_time);
			releaseLabel = getString(R.string.sort_type_release_label_for_time);
			break;
			
		case SORT_BY_HOT:
			pullLabel = getString(R.string.sort_type_pull_label_for_hot);
			refreshingLabel = getString(R.string.sort_type_refreshing_label_for_hot);
			releaseLabel = getString(R.string.sort_type_release_label_for_hot);
			break;
		}
				
		proxy.setPullLabel(pullLabel);
		proxy.setRefreshingLabel(refreshingLabel);
		proxy.setReleaseLabel(releaseLabel);
		
		mBookListView.setRefreshing();
    }
    
    private void enableSortTypeBtn() {
    		mSortByHotBtn.setEnabled(true);
		mSortByTimeBtn.setEnabled(true);
    }
}
