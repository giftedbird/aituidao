package com.aituidao.android.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;
import com.aituidao.android.model.ImageDownloadAndCacheModel;
import com.aituidao.android.model.ImageDownloadAndCacheModel.GetBitmapCB;
import com.aituidao.android.model.PushSettingModel;

public class SetPushAddressActivity extends BaseActivity {
	public static final String KEY_BOOK = "key_book";

	private EditText mAddrHeadEt;
	private Spinner mAddrTailSpinner;
	private View mNextStepBtn;
	private ImageView mBookCoverIv;
	private TextView mBookTitleTv;
	private TextView mBookAuthorTv;
	private TextView mBookIntroTv;
	private TextView mAddrHintTv;
	private View mBackBtn;

	private ArrayAdapter<CharSequence> mSpinnerAdapter;

	private Book mBook;
	private String mAddrTailStr = "";

	private ImageDownloadAndCacheModel mImageCache;

	private GetBitmapCB mGetBitmapCB = new GetBitmapCB() {
		@Override
		public void onGetBitmapSuccess(String url, Bitmap bitmap) {
			if (url.equals(mBook.coverUrl)) {
				mBookCoverIv.setImageBitmap(bitmap);
			}
		}

		@Override
		public void onGetBitmapError(String url) {
			if (url.equals(mBook.coverUrl)) {
				mImageCache.getBitmap(mBook.coverUrl);
			}
		}
	};

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

		initData();
		initUi();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_BOOK, mBook);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mImageCache.removeGetBitmapCB(mGetBitmapCB);
	}

	private void initData() {
		mImageCache = ImageDownloadAndCacheModel.getInstance(this);
		mImageCache.addGetBitmapCB(mGetBitmapCB);
	}

	private void initUi() {
		mAddrHeadEt = (EditText) findViewById(R.id.addr_head_et);
		mAddrTailSpinner = (Spinner) findViewById(R.id.addr_tail_spinner);
		mNextStepBtn = findViewById(R.id.next_step_btn);
		mBookCoverIv = (ImageView) findViewById(R.id.item_cover_iv);
		mBookTitleTv = (TextView) findViewById(R.id.item_title_tv);
		mBookAuthorTv = (TextView) findViewById(R.id.item_author_tv);
		mBookIntroTv = (TextView) findViewById(R.id.item_intro_tv);
		mAddrHintTv = (TextView) findViewById(R.id.addr_hint_tv);
		mBackBtn = findViewById(R.id.back_btn);

		mAddrTailSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						mAddrTailStr = mSpinnerAdapter.getItem(pos).toString();

						mAddrHintTv.setText(mAddrHeadEt.getEditableText()
								.toString() + "@" + mAddrTailStr);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// do nothing
					}
				});

		mSpinnerAdapter = ArrayAdapter.createFromResource(this,
				R.array.push_address_tail_array,
				android.R.layout.simple_spinner_item);
		mSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAddrTailSpinner.setAdapter(mSpinnerAdapter);

		mAddrHeadEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// do nothing
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// do nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mAddrHintTv.setText(mAddrHeadEt.getEditableText().toString()
						+ "@" + mAddrTailStr);
			}
		});

		Bitmap bitmap = mImageCache.getBitmap(mBook.coverUrl);
		if (bitmap != null) {
			mBookCoverIv.setImageBitmap(bitmap);
		} else {
			mBookCoverIv.setImageResource(R.drawable.book_default_cover);
		}

		mBookTitleTv.setText(mBook.title);

		mBookAuthorTv.setText(mBook.author);

		mBookIntroTv.setText(mBook.intro);

		mNextStepBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String addrHead = mAddrHeadEt.getEditableText().toString();
				if (TextUtils.isEmpty(addrHead)) {
					Toast.makeText(SetPushAddressActivity.this,
							R.string.please_input_addr, Toast.LENGTH_SHORT)
							.show();
				} else {
					PushSettingModel.getInstance(SetPushAddressActivity.this)
							.setNewPushAddress(addrHead, mAddrTailStr);

					enterConfirmPushAddrTrustActivityAndFinish(addrHead,
							mAddrTailStr);
				}
			}
		});

		mBackBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void enterConfirmPushAddrTrustActivityAndFinish(String addrHead,
			String addrTail) {
		Intent intent = new Intent(this, ConfirmPushAddrTrustActivity.class);
		intent.putExtra(ConfirmPushAddrTrustActivity.KEY_BOOK, mBook);
		intent.putExtra(ConfirmPushAddrTrustActivity.KEY_ADDR_HEAD, addrHead);
		intent.putExtra(ConfirmPushAddrTrustActivity.KEY_ADDR_TAIL, addrTail);

		startActivity(intent);

		finish();
	}
}
