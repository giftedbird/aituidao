package com.aituidao.android.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;
import com.aituidao.android.data.BookListResponse;

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
					
					mPageNumber = r.mNextPageNum < 0 ? 0 : r.mNextPageNum;
					
					if (mCB != null) {
						mCB.refreshBookListDataSuccess(r.mBookList,
								r.mNextPageNum < 0 ? false : true);
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
					
					mPageNumber = r.mNextPageNum < 0 ? 0 : r.mNextPageNum;
					
					if (mCB != null) {
						mCB.loadMoreBookListDataSuccess(r.mBookList,
								r.mNextPageNum < 0 ? false : true);
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
			
			List<Book> list = new ArrayList<Book>();
			Random r = new Random(System.currentTimeMillis());
			for (int i = 0; i < 20; i++) {
				Book book = new Book();
				switch (r.nextInt(3)) {
				case 0:
					book.mAuthor = "【港】许中约";
					book.mTitle = "中国近代史";
					book.mPushCount = 37;
					book.mIntro = "《中国近代史》自1970年面世后五次修订，销售数十万册，为欧美及东南亚等地中国近代史研究的权威著作及最畅销的学术教科书，是一本极具深远影响的经典作品。";
					book.mCoverUrl = R.drawable.temp_1;
					break;
					
				case 1:
					book.mAuthor = "史玉柱口述 优米网编著";
					book.mTitle = "史玉柱自述";
					book.mPushCount = 5;
					book.mIntro = "史玉柱迄今为止唯一公开著作。亲口讲述24年创业历程与营销心得。中国商业思想史里程碑之作！24年跌宕起伏，功成身退，史玉柱向您娓娓道来，历经时间沉淀的商业智慧和人生感悟。在书中，史玉柱毫无保留地回顾了创业以来的经历和各阶段的思考。全书没有深奥的理论，铅华洗尽、朴实无华，往往在轻描淡写之间，一语道破营销的本质。关于产品开发、营销传播、广告投放、团队管理、创业投资等，史玉柱都做了独特而富有洞见的思考，在启迪读者的同时，也为中国商界留下了一份弥足珍贵的商业思想记录。随便翻翻就有收获，反复体会更觉深刻。";
					book.mCoverUrl = R.drawable.temp_2;
					break;
					
				case 2:
					book.mAuthor = "【日】松本行弘 ";
					book.mTitle = "代码的未来";
					book.mPushCount = 90;
					book.mIntro = "《代码的未来》是Ruby之父松本行弘的又一力作。作者对云计算、大数据时代下的各种编程语言以及相关技术进行了剖析，并对编程语言的未来发展趋势做出预测，。";
					book.mCoverUrl = R.drawable.temp_3;
					break;
				}
				
				list.add(book);
			}
			
			BookListResponse response = new BookListResponse();
			response.mState = BookListResponse.OK;
			response.mNextPageNum = mPageNo + 1 > 5 ? -1 : mPageNo + 1;
			response.mBookList = list;
			// TODO demo代码结束
			
			if (mPageNo <= 0) {
				if (response.mState == BookListResponse.OK) {
					mHandler.sendMessage(mHandler.obtainMessage(REFRESH_BOOK_LIST_SUCCESS,
							mSortType, 0, response));
				} else {
					mHandler.sendMessage(mHandler.obtainMessage(REFRESH_BOOK_LIST_ERROR));
				}
			} else {
				if (response.mState == BookListResponse.OK) {
					mHandler.sendMessage(mHandler.obtainMessage(LOAD_MORE_BOOK_LIST_SUCCESS,
							mSortType, 0, response));
				} else {
					mHandler.sendMessage(mHandler.obtainMessage(LOAD_MORE_BOOK_LIST_ERROR));
				}
			}
		}
	}
}
