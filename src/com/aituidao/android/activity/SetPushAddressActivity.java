package com.aituidao.android.activity;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class SetPushAddressActivity extends Activity {
	public static final String KEY_BOOK = "key_book";
	
	private EditText mAddrHeadEt;
	private Spinner mAddrTailSpinner;
	private View mNextStepBtn;
	private ImageView mBookCoverIv;
	private TextView mBookTitleTv;
	private TextView mBookAuthorTv;
	private TextView mBookIntroTv;
	
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
	    
	    initUi();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_BOOK, mBook);
		super.onSaveInstanceState(outState);
	}
	
	private void initUi() {
		mAddrHeadEt = (EditText) findViewById(R.id.addr_head_et);
		mAddrTailSpinner = (Spinner) findViewById(R.id.addr_tail_spinner);
		mNextStepBtn = findViewById(R.id.next_step_btn);
		mBookCoverIv = (ImageView) findViewById(R.id.item_cover_iv);
		mBookTitleTv = (TextView) findViewById(R.id.item_title_tv);
		mBookAuthorTv = (TextView) findViewById(R.id.item_author_tv);
		mBookIntroTv = (TextView) findViewById(R.id.item_intro_tv);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.push_address_tail_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAddrTailSpinner.setAdapter(adapter);
		
		// TODO
		mBookCoverIv.setImageResource(mBook.mCoverUrl);
		// TODO
		
		mBookTitleTv.setText(mBook.mTitle);
		
		mBookAuthorTv.setText(mBook.mAuthor);
		
		mBookIntroTv.setText(mBook.mIntro);
	}
}
