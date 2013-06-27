package com.aituidao.android.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class BookListHelper {
	public static enum SortType {
		SORT_BY_TIME,
		SORT_BY_HOT,
	}
	
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
	
	public void startRefreshBookListData(SortType type) {
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
				Random r = new Random(System.currentTimeMillis());
				for (int i = 0; i < 20; i++) {
					Book book = new Book();
					switch (r.nextInt(3)) {
					case 0:
						book.mAuthor = "���ۡ�����Լ";
						book.mTitle = "�й�����ʷ";
						book.mPushCount = 37;
						book.mIntro = "��1970������������޶���������ʮ��ᣬΪŷ���������ǵȵ��й�����ʷ�о���Ȩ�������������ѧ���̿��飬��һ��������ԶӰ��ľ�����Ʒ���ⲿ����ʷ���峯����������21���ͣ������İ������й��������֮�ޱ䡣Ȼ������ȷָ������μ��ѵ����̲�������������ѧ�����ԣ���һ���������ز���������й�����������Ӧ����ʷ��������������������ߺ��ƶȡ����������������ص���������忹�����Լ������µ������Ѱ��һ������֮����������������Ϊ�ƶ������й���չ����������Ҫ��������ͨ���Խ����й��ڲ���ᶯ������ġ�������署���ˡ�һ�����ϵ���ҵ۹����ޱȼ��ѣ��ɱ�Ϊһ������������ҡ�����ʷ��";
						book.mCoverUrl = R.drawable.temp_1;
						break;
						
					case 1:
						book.mAuthor = "ʷ�������� ����������";
						book.mTitle = "ʷ��������";
						book.mPushCount = 5;
						book.mIntro = "ʷ��������ΪֹΨһ�����������׿ڽ���24�괴ҵ������Ӫ���ĵá��й���ҵ˼��ʷ��̱�֮����24����������������ˣ�ʷ���������渵���������ʱ��������ҵ�ǻۺ��������������У�ʷ�������ޱ����ػع��˴�ҵ�����ľ����͸��׶ε�˼����ȫ��û����µ����ۣ�Ǧ��ϴ������ʵ�޻������������赭д֮�䣬һ�����Ӫ���ı��ʡ����ڲ�Ʒ������Ӫ�����������Ͷ�š��Ŷӹ�����ҵͶ�ʵȣ�ʷ���������˶��ض����ж�����˼���������϶��ߵ�ͬʱ��ҲΪ�й��̽�������һ������������ҵ˼���¼��";
						book.mCoverUrl = R.drawable.temp_2;
						break;
						
					case 2:
						book.mAuthor = "���ա��ɱ��к� ";
						book.mTitle = "�����δ��";
						book.mPushCount = 90;
						book.mIntro = "��Ruby֮���ɱ��к����һ���������߶��Ƽ��㡢������ʱ���µĸ��ֱ�������Լ���ؼ������������������Ա�����Ե�δ����չ��������Ԥ�⣬�����漰Go��VoltDB��node.js��CoffeeScript��Dart��MongoDB��Ħ�����ɡ�������ԡ���ˡ�NoSQL�ȵ����ܹ�ע�Ļ��⡣";
						book.mCoverUrl = R.drawable.temp_3;
						break;
					}
					
					list.add(book);
				}
				
				
				mHandler.sendMessage(mHandler.obtainMessage(REFRESH_BOOK_LIST_SUCCESS, list));
			}
		}).start();
	}
	
	public void startLoadMoreBookListData(SortType type, final int pageNo) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO
			}
		}).start();
	}
}
