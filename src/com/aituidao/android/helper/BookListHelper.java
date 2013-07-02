package com.aituidao.android.helper;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.aituidao.android.config.Config;
import com.aituidao.android.data.Book;
import com.aituidao.android.data.BookListRequest;
import com.aituidao.android.data.BookListResponse;
import com.alibaba.fastjson.JSON;
import com.umeng.analytics.MobclickAgent;

public class BookListHelper {
	public static final int SORT_TYPE_TIME = BookListRequest.SORT_TYPE_TIME;
	public static final int SORT_TYPE_HOT = BookListRequest.SORT_TYPE_HOT;

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

					mPageNumber = r.nextPageNum <= 0 ? 0 : r.nextPageNum;

					if (mCB != null) {
						mCB.refreshBookListDataSuccess(r.bookList,
								r.nextPageNum <= 0 ? false : true);
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

					mPageNumber = r.nextPageNum <= 0 ? 0 : r.nextPageNum;

					if (mCB != null) {
						mCB.loadMoreBookListDataSuccess(r.bookList,
								r.nextPageNum <= 0 ? false : true);
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

	public void startLoadMoreBookListData(Activity activity) {
		if (mIsLoadingMore) {
			return;
		}

		mIsLoadingMore = true;

		if (mPageNumber <= 0) {
			mHandler.sendMessage(mHandler
					.obtainMessage(LOAD_MORE_BOOK_LIST_ERROR));
			return;
		}

		new Thread(new GetBookListRunable(mBookListSortType, mPageNumber))
				.start();

		MobclickAgent.onEvent(activity, "loadMoreBookList");
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
			String postStr = JSON.toJSONString(new BookListRequest(mSortType,
					mPageNo, Config.BOOK_LIST_COUNT));
			String responseStr = HttpClientHelper.requestStr(
					Config.DEFAULT_USER_AGENT, Config.BOOK_LIST_URL, postStr);

			BookListResponse response = null;
			try {
				response = JSON
						.parseObject(responseStr, BookListResponse.class);
			} catch (Exception e) {
			}

			if (mPageNo <= 0) {
				if ((response != null)
						&& (response.status == BookListResponse.OK)) {
					mHandler.sendMessage(mHandler.obtainMessage(
							REFRESH_BOOK_LIST_SUCCESS, mSortType, 0, response));
				} else {
					mHandler.sendMessage(mHandler
							.obtainMessage(REFRESH_BOOK_LIST_ERROR));
				}
			} else {
				if ((response != null)
						&& (response.status == BookListResponse.OK)) {
					mHandler.sendMessage(mHandler
							.obtainMessage(LOAD_MORE_BOOK_LIST_SUCCESS,
									mSortType, 0, response));
				} else {
					mHandler.sendMessage(mHandler
							.obtainMessage(LOAD_MORE_BOOK_LIST_ERROR));
				}
			}
		}
	}
}
