package com.aituidao.android.activity;

import java.util.ArrayList;
import java.util.List;

import com.aituidao.android.R;
import com.aituidao.android.adapter.BookListAdapter;
import com.aituidao.android.data.Book;
import com.aituidao.android.helper.BookListHelper;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.Bundle;
import android.app.Activity;

public class BookListActivity extends Activity {
	private PullToRefreshListView mBookListView;
	private BookListHelper mBookListHelper;
	private List<Book> mBookListData = new ArrayList<Book>();
	private BookListAdapter mListAdapter;
	private boolean mIsOnCreate = false;
	
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
    			mBookListView.setRefreshing();
    		}
    }
    
    private void initData() {
		mBookListHelper = new BookListHelper(this);
		mBookListHelper.setBookListHelperCB(new BookListHelper.BookListHelperCB() {
			@Override
			public void refreshBookListDataSuccess(List<Book> data) {
				mBookListView.onRefreshComplete();
				
				mBookListData.clear();
				mBookListData.addAll(data);
				
				mListAdapter.clearLastItemPos();
				mListAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void refreshBookListDataError() {
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
				mBookListHelper.startRefreshBookListData();
			}
    		});
    		
    		mListAdapter = new BookListAdapter(this, mBookListData);
    		mBookListView.setAdapter(mListAdapter);
    }
}
