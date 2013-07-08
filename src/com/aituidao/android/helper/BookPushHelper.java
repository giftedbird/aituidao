package com.aituidao.android.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.aituidao.android.config.Config;
import com.aituidao.android.config.PersonalConfig;
import com.aituidao.android.data.Book;
import com.aituidao.android.data.BookPushRequest;
import com.aituidao.android.data.GeneralResponse;
import com.aituidao.android.model.PointModel;
import com.alibaba.fastjson.JSON;

public class BookPushHelper {
	private BookPushHelperCB mCB;
	private PointModel mPointModel;

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
				mPointModel.awardPoint(Config.EACH_POINT);

				if (mCB != null) {
					mCB.bookPushError((Book) msg.obj);
				}
				break;
			}
		}
	};

	public BookPushHelper(Context context) {
		mPointModel = PointModel.getInstance(context);
	}

	public static interface BookPushHelperCB {
		public void bookPushSuccess(Book book);

		public void bookPushError(Book book);
	}

	public void setBookPushHelperCB(BookPushHelperCB cb) {
		mCB = cb;
	}

	public boolean startToPushBook(String addrHead, String addrTail,
			final Book book) {
		if (!mPointModel.spendPoint(Config.EACH_POINT)) {
			return false;
		}

		final String addr = addrHead + "@" + addrTail;

		new Thread(new Runnable() {
			@Override
			public void run() {
				String postStr = JSON.toJSONString(new BookPushRequest(addr,
						book.id));
				String responseStr = HttpClientHelper.requestStrMe(
						PersonalConfig.PUSH_BOOK_URL, postStr);

				GeneralResponse response = null;
				try {
					response = JSON.parseObject(responseStr,
							GeneralResponse.class);
				} catch (Exception e) {
				}

				if ((response != null)
						&& (response.status == GeneralResponse.OK)) {
					mHandler.sendMessage(mHandler.obtainMessage(
							PUSH_BOOK_SUCCESS, book));
				} else {
					mHandler.sendMessage(mHandler.obtainMessage(
							PUSH_BOOK_ERROR, book));
				}
			}
		}).start();

		return true;
	}
}
