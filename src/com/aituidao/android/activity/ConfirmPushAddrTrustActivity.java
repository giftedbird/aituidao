package com.aituidao.android.activity;

import java.io.File;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aituidao.android.R;
import com.aituidao.android.config.Config;
import com.aituidao.android.data.Book;
import com.aituidao.android.helper.BookPushHelper;
import com.aituidao.android.model.ImageDownloadAndCacheModel;
import com.aituidao.android.model.ImageDownloadAndCacheModel.GetBitmapCB;
import com.aituidao.android.model.PointModel;
import com.aituidao.android.model.PushSettingModel;
import com.aituidao.android.model.SrcAddrTailModel;
import com.umeng.analytics.MobclickAgent;

public class ConfirmPushAddrTrustActivity extends BaseActivity {
	public static final String KEY_BOOK = "key_book";
	public static final String KEY_FILE = "key_file";
	public static final String KEY_ADDR_HEAD = "key_addr_head";
	public static final String KEY_ADDR_TAIL = "key_addr_tail";

	private Book mBook;
	private String mFilePath;
	private String mAddrHead;
	private String mAddrTail;

	private TextView mTrustSourceTv;
	private View mHasSetTrustBtn;
	private View mLaterSetTrustBtn;
	private ImageView mBookCoverIv;
	private TextView mBookTitleTv;
	private TextView mBookAuthorTv;
	private TextView mBookIntroTv;
	private View mBackBtn;
	private View mWhyBtn;

	private BookPushHelper mBookPushHelper;

	private ImageDownloadAndCacheModel mImageCache;

	private GetBitmapCB mGetBitmapCB = new GetBitmapCB() {
		@Override
		public void onGetBitmapSuccess(String url, Bitmap bitmap) {
			if (mBook != null) {
				if (url.equals(mBook.coverUrl)) {
					mBookCoverIv.setImageBitmap(bitmap);
				}
			}
		}

		@Override
		public void onGetBitmapError(String url) {
			if (mBook != null) {
				if (url.equals(mBook.coverUrl)) {
					mImageCache.getBitmap(mBook.coverUrl);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_push_addr_trust);

		if (savedInstanceState != null) {
			mBook = savedInstanceState.getParcelable(KEY_BOOK);
			mFilePath = savedInstanceState.getString(KEY_FILE);
			mAddrHead = savedInstanceState.getString(KEY_ADDR_HEAD);
			mAddrTail = savedInstanceState.getString(KEY_ADDR_TAIL);
		} else {
			mBook = getIntent().getParcelableExtra(KEY_BOOK);
			mFilePath = getIntent().getStringExtra(KEY_FILE);
			mAddrHead = getIntent().getStringExtra(KEY_ADDR_HEAD);
			mAddrTail = getIntent().getStringExtra(KEY_ADDR_TAIL);
		}

		initData();
		initUi();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_BOOK, mBook);
		outState.putString(KEY_FILE, mFilePath);
		outState.putString(KEY_ADDR_HEAD, mAddrHead);
		outState.putString(KEY_ADDR_TAIL, mAddrTail);
		super.onSaveInstanceState(outState);
	}

	private void initData() {
		mBookPushHelper = new BookPushHelper(this);
		mBookPushHelper
				.setBookPushHelperCB(new BookPushHelper.BookPushHelperCB() {
					@Override
					public void bookPushSuccess(Book book) {
						Toast.makeText(
								ConfirmPushAddrTrustActivity.this,
								getString(R.string.push_book_success_str)
										.replace("####", book.title),
								Toast.LENGTH_LONG).show();
					}

					@Override
					public void bookPushError(Book book) {
						Toast.makeText(
								ConfirmPushAddrTrustActivity.this,
								getString(R.string.push_book_error_str)
										.replace("####", book.title),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void filePushSuccess(File file) {
						Toast.makeText(
								ConfirmPushAddrTrustActivity.this,
								getString(R.string.push_book_success_str)
										.replace("####", file.getName()),
								Toast.LENGTH_LONG).show();
					}

					@Override
					public void filePushError(File file) {
						Toast.makeText(
								ConfirmPushAddrTrustActivity.this,
								getString(R.string.push_book_error_str)
										.replace("####", file.getName()),
								Toast.LENGTH_SHORT).show();
					}
				});

		mImageCache = ImageDownloadAndCacheModel.getInstance(this);
		mImageCache.addGetBitmapCB(mGetBitmapCB);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mImageCache.removeGetBitmapCB(mGetBitmapCB);
	}

	private void initUi() {
		mTrustSourceTv = (TextView) findViewById(R.id.trust_source_tv);
		mHasSetTrustBtn = findViewById(R.id.has_set_btn);
		mLaterSetTrustBtn = findViewById(R.id.later_set_btn);
		mBookCoverIv = (ImageView) findViewById(R.id.item_cover_iv);
		mBookTitleTv = (TextView) findViewById(R.id.item_title_tv);
		mBookAuthorTv = (TextView) findViewById(R.id.item_author_tv);
		mBookIntroTv = (TextView) findViewById(R.id.item_intro_tv);
		mBackBtn = findViewById(R.id.back_btn);
		mWhyBtn = findViewById(R.id.why_need_trust_addr_btn);

		if (mBook != null) {
			Bitmap bitmap = mImageCache.getBitmap(mBook.coverUrl);
			if (bitmap != null) {
				mBookCoverIv.setImageBitmap(bitmap);
			} else {
				mBookCoverIv.setImageResource(R.drawable.book_default_cover);
			}

			mBookTitleTv.setText(mBook.title);

			mBookAuthorTv.setText(mBook.author);

			mBookIntroTv.setText(mBook.intro);
		} else {
			mBookCoverIv.setImageResource(R.drawable.book_default_cover);

			mBookTitleTv
					.setText(mFilePath.substring(mFilePath.lastIndexOf('/') + 1));

			mBookAuthorTv.setText(null);

			mBookIntroTv.setText(null);
		}

		String trustSourceStr = mAddrHead
				+ SrcAddrTailModel.getInstance(this).getSrcAddrTail();
		mTrustSourceTv.setText(trustSourceStr);

		mHasSetTrustBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PushSettingModel.getInstance(ConfirmPushAddrTrustActivity.this)
						.setPushAddressTrusted(mAddrHead, mAddrTail, true);

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("dest", "push");
				MobclickAgent.onEvent(ConfirmPushAddrTrustActivity.this,
						"confirmSourceForward", map);

				if (startToPushBook()) {
					finish();
				}
			}
		});

		mLaterSetTrustBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("dest", "delay");
				MobclickAgent.onEvent(ConfirmPushAddrTrustActivity.this,
						"confirmSourceForward", map);

				finish();
			}
		});

		mBackBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("dest", "delay");
				MobclickAgent.onEvent(ConfirmPushAddrTrustActivity.this,
						"confirmSourceForward", map);

				finish();
			}
		});

		mWhyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(Config.WHY_NEED_TRUST_ADDR_URL));
				try {
					startActivity(Intent.createChooser(intent,
							getString(R.string.which_app_open_help_doc)));
				} catch (Exception e) {
					// do nothing
				}

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("what", "add trust list");
				MobclickAgent.onEvent(ConfirmPushAddrTrustActivity.this,
						"needHelp", map);
			}
		});
	}

	private boolean startToPushBook() {
		boolean result;

		if (mBook != null) {
			if (mBookPushHelper.startToPushBook(mAddrHead, mAddrTail, mBook)) {
				Toast.makeText(
						this,
						getString(R.string.start_push_book_str).replace("####",
								mBook.title), Toast.LENGTH_SHORT).show();

				result = true;
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
										PointModel
												.getInstance(
														ConfirmPushAddrTrustActivity.this)
												.startLaunchPoint(
														ConfirmPushAddrTrustActivity.this);

										HashMap<String, String> map = new HashMap<String, String>();
										map.put("dest", "get more");
										MobclickAgent
												.onEvent(
														ConfirmPushAddrTrustActivity.this,
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
										MobclickAgent
												.onEvent(
														ConfirmPushAddrTrustActivity.this,
														"needMorePoint", map);
									}
								}).show();
				result = false;
			}
		} else {
			if (mBookPushHelper.startToPushBook(mAddrHead, mAddrTail, new File(
					mFilePath))) {
				Toast.makeText(
						this,
						getString(R.string.start_push_book_str).replace("####",
								new File(mFilePath).getName()),
						Toast.LENGTH_SHORT).show();

				result = true;
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
										PointModel
												.getInstance(
														ConfirmPushAddrTrustActivity.this)
												.startLaunchPoint(
														ConfirmPushAddrTrustActivity.this);

										HashMap<String, String> map = new HashMap<String, String>();
										map.put("dest", "get more");
										MobclickAgent
												.onEvent(
														ConfirmPushAddrTrustActivity.this,
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
										MobclickAgent
												.onEvent(
														ConfirmPushAddrTrustActivity.this,
														"needMorePoint", map);
									}
								}).show();
				result = false;
			}
		}

		MobclickAgent.onEvent(this, "pushCount");

		return result;
	}
}
