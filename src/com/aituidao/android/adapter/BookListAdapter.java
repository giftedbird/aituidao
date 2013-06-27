package com.aituidao.android.adapter;

import java.util.List;

import com.aituidao.android.R;
import com.aituidao.android.activity.SetPushAddressActivity;
import com.aituidao.android.data.Book;
import com.aituidao.android.model.PushSettingModel;
import com.aituidao.android.model.PushSettingModel.PushAddress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookListAdapter extends BaseAdapter {
	private static final int NEED_MORE_DATA_NUM = 5;
	
	private Activity mActivity;
	private List<Book> mList;
	private LayoutInflater mLayoutInflater;
	private int mLastItemPos = -1;
	private PushSettingModel mPushSettingModel;
	
	public static interface NeedMoreDataCB {
		public void onNeedMoreData();
	}
	
	private NeedMoreDataCB mNeedMoreDataCB;
	
	public BookListAdapter(Activity activity, List<Book> list) {
		mActivity = activity;
		mList = list;
		mLayoutInflater = (LayoutInflater) mActivity.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mPushSettingModel = PushSettingModel.getInstance(mActivity);
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int pos) {
		return mList.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	public void clearLastItemPos() {
		mLastItemPos = -1;
	}
	
	public void setNeedMoreDataCB(NeedMoreDataCB cb) {
		mNeedMoreDataCB = cb;
	}
	
	private static class ViewHolder {
		private ImageView mCoverIv;
		private TextView mTitleTv;
		private TextView mAuthorTv;
		private TextView mIntroTv;
		private TextView mPushCountTv;
		private ImageView mHeadHandleIv;
		private ImageView mTailHandleIv;
		private View mContentContainer;
		private View mContentController;
	}
	
	private Runnable mNeedMoreDataCBRunnable = new Runnable() {
		@Override
		public void run() {
			if (mNeedMoreDataCB != null) {
				mNeedMoreDataCB.onNeedMoreData();
			}
		}
	};
	
	private Handler mHandler = new Handler(Looper.myLooper());
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.book_list_adapter_item, null);
			
			ViewHolder holder = new ViewHolder();
			holder.mCoverIv = (ImageView) convertView.findViewById(R.id.item_cover_iv);
			holder.mTitleTv = (TextView) convertView.findViewById(R.id.item_title_tv);
			holder.mAuthorTv = (TextView) convertView.findViewById(R.id.item_author_tv);
			holder.mIntroTv = (TextView) convertView.findViewById(R.id.item_intro_tv);
			holder.mPushCountTv = (TextView) convertView.findViewById(R.id.item_push_count_tv);
			holder.mHeadHandleIv = (ImageView) convertView.findViewById(R.id.item_head_handle_iv);
			holder.mTailHandleIv = (ImageView) convertView.findViewById(R.id.item_tail_handle_iv);
			holder.mContentContainer = convertView.findViewById(R.id.item_content_container);
			holder.mContentController = convertView.findViewById(R.id.item_inner_content_container_controller);
			
			convertView.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		final Book book = mList.get(position);
		
		if (position == 0) {
			holder.mHeadHandleIv.setImageResource(R.drawable.book_item_head_handle_for_first);
		} else {
			holder.mHeadHandleIv.setImageResource(R.drawable.book_item_head_handle);
		}
		
		if (position == mList.size() - 1) {
			holder.mTailHandleIv.setVisibility(View.INVISIBLE);
		} else {
			holder.mTailHandleIv.setVisibility(View.VISIBLE);
		}
		
		// TODO
		holder.mCoverIv.setImageResource(book.mCoverUrl);
		// TODO
		
		holder.mTitleTv.setText(book.mTitle);
		
		holder.mAuthorTv.setText(book.mAuthor);
		
		holder.mIntroTv.setText(book.mIntro);
		
		String pushCountStrTail = mActivity.getString(R.string.push_count_str_tail);
		holder.mPushCountTv.setText("" + book.mPushCount + pushCountStrTail);
		
		holder.mContentController.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				wantToPush(book);
			}
		});
		
		if (position > mLastItemPos) {
			mLastItemPos = position;

			startItemAnim(holder.mContentContainer);
		}
		
		if ((mNeedMoreDataCB != null)
			&& (mList.size() - position - 1 < NEED_MORE_DATA_NUM)) {
			mHandler.removeCallbacks(mNeedMoreDataCBRunnable);
			mHandler.post(mNeedMoreDataCBRunnable);
		}
		
		return convertView;
	}
	
	private void wantToPush(Book book) {
		List<PushAddress> addrList = mPushSettingModel.getPushAddressList();
		if (addrList.size() == 0) {
			startAddNewPushAddress(book);
		} else {
			showPushAddressChoiceDlg(addrList, book);
		}
	}
	
	private void startAddNewPushAddress(Book book) {
		Intent intent = new Intent(mActivity, SetPushAddressActivity.class);
		intent.putExtra(SetPushAddressActivity.KEY_BOOK, book);
		mActivity.startActivity(intent);
	}
	
	private void showPushAddressChoiceDlg(List<PushAddress> addrList, Book book) {
		// TODO
	}
	
	private void startItemAnim(final View view) {
		Animation rotateAnim = new ItemRotateAnimation(10, 0, view);
		rotateAnim.setDuration(350);
		rotateAnim.setInterpolator(new AccelerateInterpolator());

		Animation scaleAnim = new ScaleAnimation(0.95f, 1.0f, 0.95f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnim.setDuration(350);
		scaleAnim.setInterpolator(new AccelerateInterpolator());

		Animation translateAnim = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
				Animation.RELATIVE_TO_SELF, 0.2f, Animation.ABSOLUTE, 0);
		translateAnim.setDuration(400);
		translateAnim.setInterpolator(new LinearInterpolator());

		AnimationSet animSet = new AnimationSet(false);
		animSet.addAnimation(translateAnim);
		animSet.addAnimation(scaleAnim);
		animSet.addAnimation(rotateAnim);

		view.startAnimation(animSet);
	}
	
	private static class ItemRotateAnimation extends Animation {
		private float mFromDegree;
		private float mToDegree;
		private Camera mCamera;
		private View mView;

		private ItemRotateAnimation(float fromDegree, float toDegree, View view) {
			mFromDegree = fromDegree;
			mToDegree = toDegree;
			mView = view;
		}

		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			mCamera = new Camera();
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			final float degree = mFromDegree + ((mToDegree - mFromDegree) * interpolatedTime);
			final Matrix matrix = t.getMatrix();

			mCamera.save();
			mCamera.rotateX(degree);
			mCamera.getMatrix(matrix);
			mCamera.restore();

			matrix.preTranslate(-(mView.getWidth() / 2), -(mView.getHeight() / 2));
			matrix.postTranslate(mView.getWidth() / 2, mView.getHeight() / 2);
		}
	}
}
