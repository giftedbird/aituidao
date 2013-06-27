package com.aituidao.android.activity;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

public class ConfirmPushAddrTrustActivity extends Activity {
	public static final String KEY_BOOK = "key_book";
	public static final String KEY_ADDR_HEAD = "key_addr_head";
	public static final String KEY_ADDR_TAIL = "key_addr_tail";
	
	private static final String TRUST_TAIL_STR = ".guoyong@yuanzhe.com";
	
	private Book mBook;
	private String mAddrHead;
	private String mAddrTail;
	
	private TextView mTrustSourceTv;
	private TextView mHintTv;
	
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
	    
	    initUi();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_BOOK, mBook);
		outState.putString(KEY_ADDR_HEAD, mAddrHead);
		outState.putString(KEY_ADDR_TAIL, mAddrTail);
		super.onSaveInstanceState(outState);
	}
	
	private void initUi() {
		mTrustSourceTv = (TextView) findViewById(R.id.trust_source_tv);
		mHintTv = (TextView) findViewById(R.id.hint_tv);
		
		String trustSourceStr = mAddrHead + TRUST_TAIL_STR;
		mTrustSourceTv.setText(trustSourceStr);
		
		String pushAddr = mAddrHead + "@" + mAddrTail;
		String hintStr = getString(R.string.set_address_trust)
				.replace("####", pushAddr);
		mHintTv.setText(hintStr);
		
		// TODO
	}
}
