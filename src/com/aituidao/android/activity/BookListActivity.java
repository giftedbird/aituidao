package com.aituidao.android.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.youmi.android.AdManager;
import net.youmi.android.offers.OffersManager;
import net.youmi.android.offers.PointsChangeNotify;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;
import android.widget.Toast;

import com.aituidao.android.R;
import com.aituidao.android.adapter.BookListAdapter;
import com.aituidao.android.config.Config;
import com.aituidao.android.config.PersonalConfig;
import com.aituidao.android.data.Book;
import com.aituidao.android.helper.BookListHelper;
import com.aituidao.android.helper.BookPushHelper;
import com.aituidao.android.helper.NetworkHelper;
import com.aituidao.android.model.NewUrlAccessModel;
import com.aituidao.android.model.PointModel;
import com.aituidao.android.model.PushSettingModel;
import com.aituidao.android.model.PushSettingModel.PushAddress;
import com.aituidao.android.model.SrcAddrTailModel;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class BookListActivity extends BaseActivity {
	private static final int REQUEST_FILE_PATH = 456;
	private static final long MAX_LOCAL_FILE_SIZE = 1024L * 1024 * 10;

	private PullToRefreshListView mBookListView;
	private BookListHelper mBookListHelper;
	private List<Book> mBookListData = new ArrayList<Book>();
	private BookListAdapter mListAdapter;
	private View mSortByHotBtn;
	private View mSortByTimeBtn;
	private PointModel mPointModel;
	private TextView mPointTv;
	private boolean mHasMore = false;
	private View mEarnPointBtn;
	private View mLocalUploadBtn;
	private PushSettingModel mPushSettingModel;
	private BookPushHelper mBookPushHelper;

	private int mSortType = BookListHelper.SORT_TYPE_TIME;

	private PointsChangeNotify mPointsChangeNotify = new PointsChangeNotify() {
		@Override
		public void onPointBalanceChange(int points) {
			mPointTv.setText(getString(R.string.curr_point).replace("####",
					mPointModel.getCurrPoint() + ""));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_list);

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

		if (!NetworkHelper.isConnectionAvailable(this)) {
			Toast.makeText(this, R.string.no_network, Toast.LENGTH_LONG).show();
		}

		initUMeng();
		initYM();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mListAdapter.onDestroy();

		destroyYM();
	}

	private void initYM() {
		AdManager.getInstance(this).setEnableDebugLog(false);
		AdManager.getInstance(this).init(PersonalConfig.YOUMI_KEY,
				PersonalConfig.YOUMI_PASSWD, false);
		OffersManager.getInstance(this).onAppLaunch();

		mPointModel.registerNotify(mPointsChangeNotify);
	}

	private void destroyYM() {
		mPointModel.unRegisterNotify(mPointsChangeNotify);

		OffersManager.getInstance(this).onAppExit();
	}

	private void initUMeng() {
		com.umeng.common.Log.LOG = false;
		MobclickAgent.setDebugMode(false);
		MobclickAgent.setSessionContinueMillis(10000);

		MobclickAgent.updateOnlineConfig(this);

		UmengUpdateAgent.update(this);
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

		mPointModel = PointModel.getInstance(this);

		mPushSettingModel = PushSettingModel.getInstance(this);

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

		mPointTv = (TextView) findViewById(R.id.curr_point_tv);
		mPointTv.setText(getString(R.string.curr_point).replace("####",
				mPointModel.getCurrPoint() + ""));

		mEarnPointBtn = findViewById(R.id.earn_point_btn);
		mEarnPointBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PointModel.getInstance(BookListActivity.this).startLaunchPoint(
						BookListActivity.this);

				MobclickAgent.onEvent(BookListActivity.this, "directEarnPoint");
			}
		});

		mLocalUploadBtn = findViewById(R.id.local_upload_btn);
		mLocalUploadBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("file/*");
					startActivityForResult(intent, REQUEST_FILE_PATH);
				} catch (Exception e) {
					Toast.makeText(BookListActivity.this,
							R.string.need_file_browser, Toast.LENGTH_LONG)
							.show();
				}
			}
		});

		mBookPushHelper = new BookPushHelper(this);
		mBookPushHelper
				.setBookPushHelperCB(new BookPushHelper.BookPushHelperCB() {
					@Override
					public void bookPushSuccess(Book book) {
						Toast.makeText(
								BookListActivity.this,
								BookListActivity.this.getString(
										R.string.push_book_success_str)
										.replace("####", book.title),
								Toast.LENGTH_LONG).show();
					}

					@Override
					public void bookPushError(Book book) {
						Toast.makeText(
								BookListActivity.this,
								BookListActivity.this.getString(
										R.string.push_book_error_str).replace(
										"####", book.title), Toast.LENGTH_SHORT)
								.show();
					}

					@Override
					public void filePushSuccess(File file) {
						Toast.makeText(
								BookListActivity.this,
								BookListActivity.this.getString(
										R.string.push_book_success_str)
										.replace("####", file.getName()),
								Toast.LENGTH_LONG).show();
					}

					@Override
					public void filePushError(File file) {
						Toast.makeText(
								BookListActivity.this,
								BookListActivity.this.getString(
										R.string.push_book_error_str).replace(
										"####", file.getName()),
								Toast.LENGTH_SHORT).show();
					}
				});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ((requestCode == REQUEST_FILE_PATH)
				&& (resultCode == Activity.RESULT_OK)) {
			Uri uri = data.getData();
			try {
				File file = new File(uri.getPath());

				if ((!file.exists()) || (!file.canRead())) {
					Toast.makeText(BookListActivity.this,
							R.string.unsupport_file, Toast.LENGTH_SHORT).show();
					return;
				}

				if (file.length() > MAX_LOCAL_FILE_SIZE) {
					String msg = getString(R.string.too_large_file).replace(
							"####", "" + (MAX_LOCAL_FILE_SIZE / 1024 / 1024));
					Toast.makeText(BookListActivity.this, msg,
							Toast.LENGTH_SHORT).show();
					return;
				}

				wantToLocalPush(file);
			} catch (Exception e) {
				Toast.makeText(BookListActivity.this, R.string.unsupport_file,
						Toast.LENGTH_SHORT).show();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void wantToLocalPush(File file) {
		List<PushAddress> addrList = mPushSettingModel.getPushAddressList();
		if (addrList.size() == 0) {
			startAddNewPushAddress(file);
		} else {
			showPushAddressChoiceDlg(addrList, file);
		}
	}

	private void startAddNewPushAddress(File file) {
		Intent intent = new Intent(this, SetPushAddressActivity.class);
		intent.putExtra(SetPushAddressActivity.KEY_FILE, file.getAbsolutePath());
		startActivity(intent);
	}

	private void showPushAddressChoiceDlg(final List<PushAddress> addrList,
			final File file) {
		CharSequence[] choice = new CharSequence[addrList.size() + 1];

		for (int i = 0; i < addrList.size(); i++) {
			PushAddress addr = addrList.get(i);
			String addrStr = addr.mHead + "@" + addr.mTail;
			choice[i] = addrStr;
		}

		choice[choice.length - 1] = getString(R.string.push_addr_choice_dlg_new_addr);

		new AlertDialog.Builder(this)
				.setTitle(R.string.push_addr_choice_dlg_title)
				.setSingleChoiceItems(choice, 0,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if ((which >= 0) && (which < addrList.size())) {
									PushAddress addr = addrList.get(which);
									if (addr.mTrusted) {
										startToPushBook(addr.mHead, addr.mTail,
												file);
									} else {
										enterConfirmPushAddrTrustActivity(
												addr.mHead, addr.mTail, file);
									}
								} else {
									startAddNewPushAddress(file);

									MobclickAgent.onEvent(
											BookListActivity.this,
											"otherAccount");
								}

								dialog.dismiss();
							}
						}).create().show();
	}

	private void enterConfirmPushAddrTrustActivity(String addrHead,
			String addrTail, File file) {
		Intent intent = new Intent(this, ConfirmPushAddrTrustActivity.class);
		intent.putExtra(ConfirmPushAddrTrustActivity.KEY_FILE,
				file.getAbsolutePath());
		intent.putExtra(ConfirmPushAddrTrustActivity.KEY_ADDR_HEAD, addrHead);
		intent.putExtra(ConfirmPushAddrTrustActivity.KEY_ADDR_TAIL, addrTail);

		startActivity(intent);
	}

	private void startToPushBook(String addrHead, String addrTail, File file) {
		if (mBookPushHelper.startToPushBook(addrHead, addrTail, file)) {
			Toast.makeText(
					this,
					getString(R.string.start_push_book_str).replace("####",
							file.getName()), Toast.LENGTH_SHORT).show();
		} else {
			new AlertDialog.Builder(this)
					.setTitle(
							getString(R.string.less_point_dialog_title)
									.replace("####", "" + Config.EACH_POINT))
					.setMessage(R.string.less_point_dialog_content)
					.setCancelable(false)
					.setPositiveButton(R.string.less_point_dialog_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									PointModel.getInstance(
											BookListActivity.this)
											.startLaunchPoint(
													BookListActivity.this);

									HashMap<String, String> map = new HashMap<String, String>();
									map.put("dest", "get more");
									MobclickAgent.onEvent(
											BookListActivity.this,
											"needMorePoint", map);
								}
							})
					.setNegativeButton(R.string.less_point_dialog_cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									HashMap<String, String> map = new HashMap<String, String>();
									map.put("dest", "later");
									MobclickAgent.onEvent(
											BookListActivity.this,
											"needMorePoint", map);
								}
							}).show();
		}

		MobclickAgent.onEvent(BookListActivity.this, "pushCount");
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
