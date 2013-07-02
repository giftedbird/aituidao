package com.aituidao.android.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.aituidao.android.R;
import com.aituidao.android.adapter.BookListAdapter;
import com.aituidao.android.config.Config;
import com.aituidao.android.data.Book;
import com.aituidao.android.helper.BookListHelper;
import com.aituidao.android.model.NewUrlAccessModel;
import com.aituidao.android.model.SrcAddrTailModel;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;

public class BookListActivity extends BaseActivity {
	private PullToRefreshListView mBookListView;
	private BookListHelper mBookListHelper;
	private List<Book> mBookListData = new ArrayList<Book>();
	private BookListAdapter mListAdapter;
	private View mSortByHotBtn;
	private View mSortByTimeBtn;
	private boolean mHasMore = false;

	private int mSortType = BookListHelper.SORT_TYPE_TIME;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_list);

		initUMeng();

		initData();
		initUi();

		mBookListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				startRefreshBySortType(mSortType);
			}
		}, 600);

		SrcAddrTailModel.getInstance(this).checkNewSrcAddrSilently();

		NewUrlAccessModel.getInstance(this).checkAndStartNewUrlAccess();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mListAdapter.onDestroy();
	}

	private void initUMeng() {
		com.umeng.common.Log.LOG = Config.DEBUG;
		MobclickAgent.setDebugMode(Config.DEBUG);
		MobclickAgent.setSessionContinueMillis(10000);

		MobclickAgent.updateOnlineConfig(this);
	}

	private void initData() {
		mBookListHelper = new BookListHelper(this);
		mBookListHelper
				.setBookListHelperCB(new BookListHelper.BookListHelperCB() {
					@Override
					public void refreshBookListDataSuccess(List<Book> data,
							boolean hasMore) {
						mHasMore = hasMore;

						enableSortTypeBtn();

						mBookListView.onRefreshComplete();

						mBookListData.clear();
						mBookListData.addAll(data);

						mListAdapter.clearLastItemPos();
						mListAdapter.notifyDataSetChanged();

						mListAdapter.setHasMore(mHasMore);
					}

					@Override
					public void refreshBookListDataError() {
						enableSortTypeBtn();

						mBookListView.onRefreshComplete();

						mListAdapter.clearLastItemPos();
						mListAdapter.notifyDataSetChanged();
					}

					@Override
					public void loadMoreBookListDataSuccess(List<Book> data,
							boolean hasMore) {
						mHasMore = hasMore;

						mBookListData.addAll(data);

						mListAdapter.notifyDataSetChanged();

						mListAdapter.setHasMore(mHasMore);
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
		mBookListView
				.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
					@Override
					public void onRefresh(PullToRefreshBase refreshView) {
						mBookListHelper.startRefreshBookListData(mSortType);
					}
				});

		mListAdapter = new BookListAdapter(this, mBookListData);
		mListAdapter.setNeedMoreDataCB(new BookListAdapter.NeedMoreDataCB() {
			@Override
			public void onNeedMoreData() {
				if (mHasMore) {
					mBookListHelper.startLoadMoreBookListData();
				}
			}
		});
		mListAdapter.setHasMore(mHasMore);

		mBookListView.setAdapter(mListAdapter);
		mBookListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// do nothing
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
					mListAdapter.setIsFling(true);
					break;

				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					mListAdapter.setIsFling(false);
					break;

				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					mListAdapter.setIsFling(true);
					break;
				}
			}
		});

		mSortByHotBtn = findViewById(R.id.book_sort_by_hot_iv);
		mSortByTimeBtn = findViewById(R.id.book_sort_by_time_iv);

		mSortByHotBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startRefreshBySortType(BookListHelper.SORT_TYPE_HOT);

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("sortType", "hot");
				MobclickAgent.onEvent(BookListActivity.this, "switchSortType",
						map);
			}
		});

		mSortByTimeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startRefreshBySortType(BookListHelper.SORT_TYPE_TIME);

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("sortType", "time");
				MobclickAgent.onEvent(BookListActivity.this, "switchSortType",
						map);
			}
		});
	}

	private void startRefreshBySortType(int type) {
		if (type != mSortType) {
			mHasMore = false;
			mBookListData.clear();
			mListAdapter.notifyDataSetChanged();

			mListAdapter.setHasMore(mHasMore);
		}

		mSortType = type;

		switch (mSortType) {
		case BookListHelper.SORT_TYPE_TIME:
			mSortByHotBtn.setSelected(false);
			mSortByTimeBtn.setSelected(true);
			break;

		case BookListHelper.SORT_TYPE_HOT:
			mSortByHotBtn.setSelected(true);
			mSortByTimeBtn.setSelected(false);
			break;
		}

		mSortByHotBtn.setEnabled(false);
		mSortByTimeBtn.setEnabled(false);

		ILoadingLayout proxy = mBookListView.getLoadingLayoutProxy();
		String pullLabel = null;
		String refreshingLabel = null;
		String releaseLabel = null;
		switch (mSortType) {
		case BookListHelper.SORT_TYPE_TIME:
			pullLabel = getString(R.string.sort_type_pull_label_for_time);
			refreshingLabel = getString(R.string.sort_type_refreshing_label_for_time);
			releaseLabel = getString(R.string.sort_type_release_label_for_time);
			break;

		case BookListHelper.SORT_TYPE_HOT:
			pullLabel = getString(R.string.sort_type_pull_label_for_hot);
			refreshingLabel = getString(R.string.sort_type_refreshing_label_for_hot);
			releaseLabel = getString(R.string.sort_type_release_label_for_hot);
			break;
		}

		proxy.setPullLabel(pullLabel);
		proxy.setRefreshingLabel(refreshingLabel);
		proxy.setReleaseLabel(releaseLabel);

		mBookListView.onRefreshComplete();
		mBookListView.setRefreshing();
	}

	private void enableSortTypeBtn() {
		mSortByHotBtn.setEnabled(true);
		mSortByTimeBtn.setEnabled(true);
	}
}
