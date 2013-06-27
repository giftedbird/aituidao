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
						book.mAuthor = "【港】徐中约";
						book.mTitle = "中国近代史";
						book.mPushCount = 37;
						book.mIntro = "自1970年面世后五次修订，销售数十万册，为欧美及东南亚等地中国近代史研究的权威著作及最畅销的学术教科书，是一本极具深远影响的经典作品。这部近代史自清朝立国起，下迄21世纪，缕述四百年来中国近代社会之巨变。然作者明确指出，这段艰难的历程并非如大多西方汉学家所言，是一段西方因素不断输入而中国仅仅被动回应的历史。作者拈出“政府的政策和制度”、“反对外来因素的民族或种族抗争”以及“在新的天地里寻求一条求生之道”三条线索，作为推动近代中国发展的三股最重要动力，并通过对近代中国内部社会动荡的描摹，向世界讲述了“一个古老的儒家帝国经无比艰难，蜕变为一个近代民族国家”的历史。";
						book.mCoverUrl = R.drawable.temp_1;
						break;
						
					case 1:
						book.mAuthor = "史玉柱口述 优米网编著";
						book.mTitle = "史玉柱自述";
						book.mPushCount = 5;
						book.mIntro = "史玉柱迄今为止唯一公开著作。亲口讲述24年创业历程与营销心得。中国商业思想史里程碑之作！24年跌宕起伏，功成身退，史玉柱向您娓娓道来，历经时间沉淀的商业智慧和人生感悟。在书中，史玉柱毫无保留地回顾了创业以来的经历和各阶段的思考。全书没有深奥的理论，铅华洗尽、朴实无华，往往在轻描淡写之间，一语道破营销的本质。关于产品开发、营销传播、广告投放、团队管理、创业投资等，史玉柱都做了独特而富有洞见的思考，在启迪读者的同时，也为中国商界留下了一份弥足珍贵的商业思想记录。";
						book.mCoverUrl = R.drawable.temp_2;
						break;
						
					case 2:
						book.mAuthor = "【日】松本行弘 ";
						book.mTitle = "代码的未来";
						book.mPushCount = 90;
						book.mIntro = "是Ruby之父松本行弘的又一力作。作者对云计算、大数据时代下的各种编程语言以及相关技术进行了剖析，并对编程语言的未来发展趋势做出预测，内容涉及Go、VoltDB、node.js、CoffeeScript、Dart、MongoDB、摩尔定律、编程语言、多核、NoSQL等当今备受关注的话题。";
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
