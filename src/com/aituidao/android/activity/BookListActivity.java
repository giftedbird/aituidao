package com.aituidao.android.activity;

import java.util.List;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;
import com.aituidao.android.helper.BookListHelper;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.Bundle;
import android.util.Log;
import android.app.Activity;

public class BookListActivity extends Activity {
	private PullToRefreshListView mBookListView;
	private BookListHelper mBookListHelper;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        
        initData();
        initUi();
    }
    
    private void initData() {
		mBookListHelper = new BookListHelper(this);
		mBookListHelper.setBookListHelperCB(new BookListHelper.BookListHelperCB() {
			@Override
			public void refreshBookListDataSuccess(List<Book> data) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void refreshBookListDataError() {
				// TODO Auto-generated method stub
				mBookListView.onRefreshComplete();
			}
			
			@Override
			public void loadMoreBookListDataSuccess(List<Book> data) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void loadMoreBookListDataError() {
				// TODO Auto-generated method stub
			}
		});
	}
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void initUi() {
    		mBookListView = (PullToRefreshListView) findViewById(R.id.book_list_view);
    		mBookListView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			@Override
			public void onRefresh(PullToRefreshBase refreshView) {
				mBookListHelper.startRefreshBookListData();
			}
    		});
    }
}
