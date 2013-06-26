package com.aituidao.android.helper;

import java.util.ArrayList;
import java.util.List;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class BookListHelper {
	private Context mContext;
	private BookListHelperCB mCB;
	
	private static final int REFRESH_BOOK_LIST_SUCCESS = 1;
	private static final int REFRESH_BOOK_LIST_ERROR = 2;
	private static final int LOAD_MORE_BOOK_LIST_SUCCESS = 3;
	private static final int LOAD_MORE_BOOK_LIST_ERROR = 4;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_BOOK_LIST_SUCCESS:
				if (mCB != null) {
					mCB.refreshBookListDataSuccess((List<Book>) msg.obj);
				}
				break;
				
			case REFRESH_BOOK_LIST_ERROR:
				if (mCB != null) {
					mCB.refreshBookListDataError();
				}
				break;
				
			case LOAD_MORE_BOOK_LIST_SUCCESS:
				if (mCB != null) {
					mCB.loadMoreBookListDataSuccess((List<Book>) msg.obj);
				}
				break;
				
			case LOAD_MORE_BOOK_LIST_ERROR:
				if (mCB != null) {
					mCB.loadMoreBookListDataError();
				}
				break;
			}
		}
	};
	
	public static interface BookListHelperCB {
		public void refreshBookListDataSuccess(List<Book> data);
		public void refreshBookListDataError();
		public void loadMoreBookListDataSuccess(List<Book> data);
		public void loadMoreBookListDataError();
	}
	
	public BookListHelper(Context context) {
		mContext = context.getApplicationContext();
	}
	
	public void setBookListHelperCB(BookListHelperCB cb) {
		mCB = cb;
	}
	
	public void startRefreshBookListData() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO 
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				List<Book> list = new ArrayList<Book>();
				for (int i = 0; i < 20; i++) {
					Book book = new Book();
					book.mAuthor = "【港】徐中约";
					book.mTitle = "中国近代史";
					book.mPushCount = 37;
					book.mIntro = "中国近代史》自1970年面世后五次修订，销售数十万册，为欧美及东南亚等地中国近代史研究的权威著作及最畅销的学术教科书，是一本极具深远影响的经典作品。这部近代史自清朝立国起，下迄21世纪，缕述四百年来中国近代社会之巨变。然作者明确指出，这段艰难的历程并非如大多西方汉学家所言，是一段西方因素不断输入而中国仅仅被动回应的历史。作者拈出“政府的政策和制度”、“反对外来因素的民族或种族抗争”以及“在新的天地里寻求一条求生之道”三条线索，作为推动近代中国发展的三股最重要动力，并通过对近代中国内部社会动荡的描摹，向世界讲述了“一个古老的儒家帝国经无比艰难，蜕变为一个近代民族国家”的历史。";
					book.mCoverUrl = R.drawable.temp_1;
					
					list.add(book);
				}
				
				
				mHandler.sendMessage(mHandler.obtainMessage(REFRESH_BOOK_LIST_SUCCESS, list));
			}
		}).start();
	}
	
	public void startLoadMoreBookListData(final int pageNo) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO
			}
		}).start();
	}
}
