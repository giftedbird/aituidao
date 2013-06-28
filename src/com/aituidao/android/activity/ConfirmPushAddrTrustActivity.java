package com.aituidao.android.activity;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;
import com.aituidao.android.helper.BookPushHelper;
import com.aituidao.android.model.PushSettingModel;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ConfirmPushAddrTrustActivity extends Activity {
	public static final String KEY_BOOK = "key_book";
	public static final String KEY_ADDR_HEAD = "key_addr_head";
	public static final String KEY_ADDR_TAIL = "key_addr_tail";
	
	private static final String TRUST_TAIL_STR = ".guoyong@yuanzhe.com";
	
	private Book mBook;
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
	
	private BookPushHelper mBookPushHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_confirm_push_addr_trust);
	    
	    if (savedInstanceState != null) {
			mBook = savedInstanceState.getParcelable(KEY_BOOK);
			mAddrHead = savedInstanceState.getString(KEY_ADDR_HEAD);
			mAddrTail = savedInstanceState.getString(KEY_ADDR_TAIL);
	    } else {
			mBook = getIntent().getParcelableExtra(KEY_BOOK);
			mAddrHead = getIntent().getStringExtra(KEY_ADDR_HEAD);
			mAddrTail = getIntent().getStringExtra(KEY_ADDR_TAIL);
	    }
	    
	    if ((mBook == null)
	    		|| (TextUtils.isEmpty(mAddrHead))
	    		|| (TextUtils.isEmpty(mAddrTail))) {
			finish();
			return;
	    }
	    
	    initData();
	    initUi();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_BOOK, mBook);
		outState.putString(KEY_ADDR_HEAD, mAddrHead);
		outState.putString(KEY_ADDR_TAIL, mAddrTail);
		super.onSaveInstanceState(outState);
	}
	
	private void initData() {
		mBookPushHelper = new BookPushHelper(this);
		mBookPushHelper.setBookPushHelperCB(new BookPushHelper.BookPushHelperCB() {
			@Override
			public void bookPushSuccess(Book book) {
				Toast.makeText(ConfirmPushAddrTrustActivity.this, getString(
						R.string.push_book_success_str).replace("####",
								book.title), Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void bookPushError(Book book) {
				Toast.makeText(ConfirmPushAddrTrustActivity.this, getString(
						R.string.push_book_error_str).replace("####",
								book.title), Toast.LENGTH_SHORT).show();
			}
		});
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
		
		// TODO
		mBookCoverIv.setImageResource(mBook.coverUrl);
		// TODO
		
		mBookTitleTv.setText(mBook.title);
		
		mBookAuthorTv.setText(mBook.author);
		
		mBookIntroTv.setText(mBook.intro);
		
		String trustSourceStr = mAddrHead + TRUST_TAIL_STR;
		mTrustSourceTv.setText(trustSourceStr);
		
		mHasSetTrustBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PushSettingModel.getInstance(
						ConfirmPushAddrTrustActivity.this).setPushAddressTrusted(
								mAddrHead, mAddrTail, true);
				
				startToPushBook();
				
				finish();
			}
		});
		
		mLaterSetTrustBtn.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mBackBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void startToPushBook() {
		Toast.makeText(this, getString(
				R.string.start_push_book_str).replace("####",
						mBook.title), Toast.LENGTH_SHORT).show();
		
		mBookPushHelper.startToPushBook(mAddrHead, mAddrTail, mBook);
	}
}
