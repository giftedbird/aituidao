package com.aituidao.android.activity;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;

import android.app.Activity;
import android.os.Bundle;

public class SetPushAddressActivity extends Activity {
	public static final String KEY_BOOK = "key_book";
	
	private Book mBook;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_set_push_address);
	    
	    if (savedInstanceState != null) {
	    		mBook = savedInstanceState.getParcelable(KEY_BOOK);
	    } else {
	    	    mBook = getIntent().getParcelableExtra(KEY_BOOK);
	    }
	    
	    if (mBook == null) {
	    		finish();
	    		return;
	    }
	    
	    // TODO
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_BOOK, mBook);
		super.onSaveInstanceState(outState);
	}
}
