package com.aituidao.android.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.aituidao.android.data.Book;

public class BookPushHelper {
	private Context mContext;
	private BookPushHelperCB mCB;
	
	private static final int PUSH_BOOK_SUCCESS = 1;
	private static final int PUSH_BOOK_ERROR = 2;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PUSH_BOOK_SUCCESS:
				if (mCB != null) {
					mCB.bookPushSuccess((Book) msg.obj);
				}
				break;
				
			case PUSH_BOOK_ERROR:
				if (mCB != null) {
					mCB.bookPushError((Book) msg.obj);
				}
				break;
			}
		}
	};
	
	public static interface BookPushHelperCB {
		public void bookPushSuccess(Book book);
		public void bookPushError(Book book);
	}
	
	public BookPushHelper(Context context) {
		mContext = context.getApplicationContext();
	}
	
	public void setBookPushHelperCB(BookPushHelperCB cb) {
		mCB = cb;
	}
	
	public void startToPushBook(final String addrHead, final String addrTail, final Book book) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO demo代码开始
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				
				mHandler.sendMessage(mHandler.obtainMessage(PUSH_BOOK_SUCCESS, book));
				// TODO demo代码结束
			}
		}).start();
	}
}
