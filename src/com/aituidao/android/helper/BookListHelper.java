package com.aituidao.android.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;
import com.aituidao.android.data.BookListResponse;
import com.alibaba.fastjson.JSON;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class BookListHelper {
	public static final int SORT_TYPE_TIME = 1;
	public static final int SORT_YYPE_HOT = 2;
	
	private Context mContext;
	private BookListHelperCB mCB;
	private int mBookListSortType = SORT_TYPE_TIME;
	private long mPageNumber = 0;
	private boolean mIsLoadingMore = false;
	
	private static final int REFRESH_BOOK_LIST_SUCCESS = 1;
	private static final int REFRESH_BOOK_LIST_ERROR = 2;
	private static final int LOAD_MORE_BOOK_LIST_SUCCESS = 3;
	private static final int LOAD_MORE_BOOK_LIST_ERROR = 4;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_BOOK_LIST_SUCCESS:
				if (msg.arg1 == mBookListSortType) {
					BookListResponse r = (BookListResponse) msg.obj;
					
					mPageNumber = r.nextPageNum < 0 ? 0 : r.nextPageNum;
					
					if (mCB != null) {
						mCB.refreshBookListDataSuccess(r.bookList,
								r.nextPageNum < 0 ? false : true);
					}
				} else {
					if (mCB != null) {
						mCB.refreshBookListDataError();
					}
				}
				break;
				
			case REFRESH_BOOK_LIST_ERROR:
				if (mCB != null) {
					mCB.refreshBookListDataError();
				}
				break;
				
			case LOAD_MORE_BOOK_LIST_SUCCESS:
				mIsLoadingMore = false;
				
				if (msg.arg1 == mBookListSortType) {
					BookListResponse r = (BookListResponse) msg.obj;
					
					mPageNumber = r.nextPageNum < 0 ? 0 : r.nextPageNum;
					
					if (mCB != null) {
						mCB.loadMoreBookListDataSuccess(r.bookList,
								r.nextPageNum < 0 ? false : true);
					}
				} else {
					if (mCB != null) {
						mCB.loadMoreBookListDataError();
					}
				}
				break;
				
			case LOAD_MORE_BOOK_LIST_ERROR:
				mIsLoadingMore = false;
				
				if (mCB != null) {
					mCB.loadMoreBookListDataError();
				}
				break;
			}
		}
	};
	
	public static interface BookListHelperCB {
		public void refreshBookListDataSuccess(List<Book> data, boolean hasMore);
		public void refreshBookListDataError();
		public void loadMoreBookListDataSuccess(List<Book> data, boolean hasMore);
		public void loadMoreBookListDataError();
	}
	
	public BookListHelper(Context context) {
		mContext = context.getApplicationContext();
	}
	
	public void setBookListHelperCB(BookListHelperCB cb) {
		mCB = cb;
	}
	
	public void startRefreshBookListData(final int type) {
		if (type != mBookListSortType) {
			mPageNumber = 0;
		}
		
		mBookListSortType = type;
		
		new Thread(new GetBookListRunable(mBookListSortType, 0)).start();
	}
	
	public void startLoadMoreBookListData() {
		if (mIsLoadingMore) {
			return;
		}
		
		mIsLoadingMore = true;
		
		if (mPageNumber <= 0) {
			mHandler.sendMessage(mHandler.obtainMessage(LOAD_MORE_BOOK_LIST_ERROR));
			return;
		}
		
		new Thread(new GetBookListRunable(mBookListSortType, mPageNumber)).start();
	}
	
	private class GetBookListRunable implements Runnable {
		private int mSortType;
		private long mPageNo;
		
		private GetBookListRunable(int type, long pageNo) {
			mSortType = type;
			mPageNo = pageNo;
		}

		@Override
		public void run() {
			// TODO demo代码开始
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
			
			String responseStr = "{\"bookList\":[{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"}],\"nextPageNum\":1,\"status\":1}";
			// TODO demo代码结束
			
			BookListResponse response = JSON.parseObject(responseStr, BookListResponse.class);
			if (mPageNo <= 0) {
				if (response.status == BookListResponse.OK) {
					mHandler.sendMessage(mHandler.obtainMessage(REFRESH_BOOK_LIST_SUCCESS,
							mSortType, 0, response));
				} else {
					mHandler.sendMessage(mHandler.obtainMessage(REFRESH_BOOK_LIST_ERROR));
				}
			} else {
				if (response.status == BookListResponse.OK) {
					mHandler.sendMessage(mHandler.obtainMessage(LOAD_MORE_BOOK_LIST_SUCCESS,
							mSortType, 0, response));
				} else {
					mHandler.sendMessage(mHandler.obtainMessage(LOAD_MORE_BOOK_LIST_ERROR));
				}
			}
		}
	}
}
