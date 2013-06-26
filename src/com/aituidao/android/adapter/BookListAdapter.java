package com.aituidao.android.adapter;

import java.util.List;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
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
	private Context mContext;
	private List<Book> mList;
	private LayoutInflater mLayoutInflater;
	private int mLastItemPos = -1;
	
	public BookListAdapter(Context context, List<Book> list) {
		mContext = context.getApplicationContext();
		mList = list;
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
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
		Book book = mList.get(position);
		
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
		
		// TODO 这里是url，要下载，现在只是临时代码
		holder.mCoverIv.setImageResource(book.mCoverUrl);
		// TODO
		
		holder.mTitleTv.setText(book.mTitle);
		
		holder.mAuthorTv.setText(book.mAuthor);
		
		holder.mIntroTv.setText(book.mIntro);
		
		String pushCountStrTail = mContext.getString(R.string.push_count_str_tail);
		holder.mPushCountTv.setText("" + book.mPushCount + pushCountStrTail);
		
		holder.mContentController.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
		
		if (position > mLastItemPos) {
			mLastItemPos = position;

			startItemAnim(holder.mContentContainer);
		}
		
		return convertView;
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
