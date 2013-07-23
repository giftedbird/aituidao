package com.aituidao.android.adapter;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.Build;
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
import android.widget.Toast;

import com.aituidao.android.R;
import com.aituidao.android.activity.ConfirmPushAddrTrustActivity;
import com.aituidao.android.activity.SetPushAddressActivity;
import com.aituidao.android.config.Config;
import com.aituidao.android.data.Book;
import com.aituidao.android.helper.BookPushHelper;
import com.aituidao.android.model.ImageDownloadAndCacheModel;
import com.aituidao.android.model.ImageDownloadAndCacheModel.GetBitmapCB;
import com.aituidao.android.model.PointModel;
import com.aituidao.android.model.PushSettingModel;
import com.aituidao.android.model.PushSettingModel.PushAddress;
import com.umeng.analytics.MobclickAgent;

public class BookListAdapter extends BaseAdapter {
	private static final int NEED_MORE_DATA_NUM = 5;

	private Activity mActivity;
	private List<Book> mList;
	private LayoutInflater mLayoutInflater;
	private int mLastItemPos = -1;
	private PushSettingModel mPushSettingModel;
	private boolean mHasMore = false;
	private boolean mIsFling = false;

	public static interface NeedMoreDataCB {
		public void onNeedMoreData();
	}

	private NeedMoreDataCB mNeedMoreDataCB;

	private BookPushHelper mBookPushHelper;

	private ImageDownloadAndCacheModel mImageCache;

	private GetBitmapCB mGetBitmapCB = new GetBitmapCB() {
		@Override
		public void onGetBitmapSuccess(String url, Bitmap bitmap) {
			if (containUrl(url)) {
				BookListAdapter.this.notifyDataSetChanged();
			}
		}

		@Override
		public void onGetBitmapError(String url) {
			// do nothing
		}
	};

	public BookListAdapter(Activity activity, List<Book> list) {
		mActivity = activity;
		mList = list;
		mLayoutInflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPushSettingModel = PushSettingModel.getInstance(mActivity);

		mBookPushHelper = new BookPushHelper(mActivity);
		mBookPushHelper
				.setBookPushHelperCB(new BookPushHelper.BookPushHelperCB() {
					@Override
					public void bookPushSuccess(Book book) {
						Toast.makeText(
								mActivity,
								mActivity.getString(
										R.string.push_book_success_str)
										.replace("####", book.title),
								Toast.LENGTH_LONG).show();
					}

					@Override
					public void bookPushError(Book book) {
						Toast.makeText(
								mActivity,
								mActivity.getString(
										R.string.push_book_error_str).replace(
										"####", book.title), Toast.LENGTH_SHORT)
								.show();
					}

					@Override
					public void filePushSuccess(File file) {
						Toast.makeText(
								mActivity,
								mActivity.getString(
										R.string.push_book_success_str)
										.replace("####", file.getName()),
								Toast.LENGTH_LONG).show();
					}

					@Override
					public void filePushError(File file) {
						Toast.makeText(
								mActivity,
								mActivity.getString(
										R.string.push_book_error_str).replace(
										"####", file.getName()),
								Toast.LENGTH_SHORT).show();
					}
				});

		mImageCache = ImageDownloadAndCacheModel.getInstance(mActivity);
		mImageCache.addGetBitmapCB(mGetBitmapCB);
	}

	@Override
	public int getCount() {
		return mList.size() + 1;
	}

	@Override
	public Object getItem(int pos) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == mList.size()) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	public void onDestroy() {
		mImageCache.removeGetBitmapCB(mGetBitmapCB);
	}

	public void clearLastItemPos() {
		mLastItemPos = -1;
	}

	public void setNeedMoreDataCB(NeedMoreDataCB cb) {
		mNeedMoreDataCB = cb;
	}

	public void setHasMore(boolean hasMore) {
		if (mHasMore != hasMore) {
			mHasMore = hasMore;

			notifyDataSetChanged();
		}
	}

	public void setIsFling(boolean isFling) {
		if (mIsFling != isFling) {
			mIsFling = isFling;
			notifyDataSetChanged();
		}
	}

	private boolean containUrl(String url) {
		if (url == null) {
			return false;
		}

		for (Book book : mList) {
			if (url.equals(book.coverUrl)) {
				return true;
			}
		}

		return false;
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
		private TextView mDoubanRateTv;
		private TextView mUploadUserTv;
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
		if (position == mList.size()) {
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(
						R.layout.book_list_adapter_item_loading, null);
			}

			if (mHasMore) {
				((TextView) convertView)
						.setText(R.string.book_list_getting_more);
			} else {
				((TextView) convertView)
						.setText(R.string.book_list_no_getting_more);
			}
		} else {
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(
						R.layout.book_list_adapter_item, null);

				ViewHolder holder = new ViewHolder();
				holder.mCoverIv = (ImageView) convertView
						.findViewById(R.id.item_cover_iv);
				holder.mTitleTv = (TextView) convertView
						.findViewById(R.id.item_title_tv);
				holder.mAuthorTv = (TextView) convertView
						.findViewById(R.id.item_author_tv);
				holder.mIntroTv = (TextView) convertView
						.findViewById(R.id.item_intro_tv);
				holder.mPushCountTv = (TextView) convertView
						.findViewById(R.id.item_push_count_tv);
				holder.mHeadHandleIv = (ImageView) convertView
						.findViewById(R.id.item_head_handle_iv);
				holder.mTailHandleIv = (ImageView) convertView
						.findViewById(R.id.item_tail_handle_iv);
				holder.mContentContainer = convertView
						.findViewById(R.id.item_content_container);
				holder.mContentController = convertView
						.findViewById(R.id.item_inner_content_container_controller);
				holder.mDoubanRateTv = (TextView) convertView
						.findViewById(R.id.item_douban_rate_tv);
				holder.mUploadUserTv = (TextView) convertView
						.findViewById(R.id.item_upload_user_tv);

				convertView.setTag(holder);
			}

			ViewHolder holder = (ViewHolder) convertView.getTag();
			final Book book = mList.get(position);

			if (position == 0) {
				holder.mHeadHandleIv
						.setImageResource(R.drawable.book_item_head_handle_for_first);
			} else {
				holder.mHeadHandleIv
						.setImageResource(R.drawable.book_item_head_handle);
			}

			if (position == mList.size() - 1) {
				holder.mTailHandleIv.setVisibility(View.INVISIBLE);
			} else {
				holder.mTailHandleIv.setVisibility(View.VISIBLE);
			}

			Bitmap bitmap;
			if (mIsFling) {
				bitmap = mImageCache.getBitmapFromMem(book.coverUrl);
			} else {
				bitmap = mImageCache.getBitmap(book.coverUrl);
			}

			if (bitmap != null) {
				holder.mCoverIv.setImageBitmap(bitmap);
			} else {
				holder.mCoverIv.setImageResource(R.drawable.book_default_cover);
			}

			int rate = book.doubanRate > 0 ? book.doubanRate : 0;
			String doubanRateStr = mActivity
					.getString(R.string.douban_rate_str).replace("####",
							"" + rate / 10 + "." + rate % 10);
			holder.mDoubanRateTv.setText(doubanRateStr);

			String uploadUserStr = mActivity
					.getString(R.string.upload_user_str).replace("####",
							book.uploadUserName);
			holder.mUploadUserTv.setText(uploadUserStr);

			holder.mTitleTv.setText(book.title);

			holder.mAuthorTv.setText(book.author);

			holder.mIntroTv.setText(book.intro);

			String pushCountStrTail = mActivity
					.getString(R.string.push_count_str_tail);
			holder.mPushCountTv.setText("" + book.pushCount + pushCountStrTail);

			holder.mContentController
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							wantToPush(book);
						}
					});

			if (position > mLastItemPos) {
				mLastItemPos = position;

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					startItemAnim(holder.mContentContainer);
				}
			}

			if ((mNeedMoreDataCB != null)
					&& (mList.size() - position - 1 < NEED_MORE_DATA_NUM)) {
				mHandler.removeCallbacks(mNeedMoreDataCBRunnable);
				mHandler.post(mNeedMoreDataCBRunnable);
			}
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

	private void showPushAddressChoiceDlg(final List<PushAddress> addrList,
			final Book book) {
		CharSequence[] choice = new CharSequence[addrList.size() + 1];

		for (int i = 0; i < addrList.size(); i++) {
			PushAddress addr = addrList.get(i);
			String addrStr = addr.mHead + "@" + addr.mTail;
			choice[i] = addrStr;
		}

		choice[choice.length - 1] = mActivity
				.getString(R.string.push_addr_choice_dlg_new_addr);

		new AlertDialog.Builder(mActivity)
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
												book);
									} else {
										enterConfirmPushAddrTrustActivity(
												addr.mHead, addr.mTail, book);
									}
								} else {
									startAddNewPushAddress(book);

									MobclickAgent.onEvent(mActivity,
											"otherAccount");
								}

								dialog.dismiss();
							}
						}).create().show();
	}

	private void enterConfirmPushAddrTrustActivity(String addrHead,
			String addrTail, Book book) {
		Intent intent = new Intent(mActivity,
				ConfirmPushAddrTrustActivity.class);
		intent.putExtra(ConfirmPushAddrTrustActivity.KEY_BOOK, book);
		intent.putExtra(ConfirmPushAddrTrustActivity.KEY_ADDR_HEAD, addrHead);
		intent.putExtra(ConfirmPushAddrTrustActivity.KEY_ADDR_TAIL, addrTail);

		mActivity.startActivity(intent);
	}

	private void startToPushBook(String addrHead, String addrTail, Book book) {
		if (mBookPushHelper.startToPushBook(addrHead, addrTail, book)) {
			Toast.makeText(
					mActivity,
					mActivity.getString(R.string.start_push_book_str).replace(
							"####", book.title), Toast.LENGTH_SHORT).show();
		} else {
			new AlertDialog.Builder(mActivity)
					.setTitle(
							mActivity.getString(
									R.string.less_point_dialog_title).replace(
									"####", "" + Config.EACH_POINT))
					.setMessage(R.string.less_point_dialog_content)
					.setCancelable(false)
					.setPositiveButton(R.string.less_point_dialog_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									PointModel.getInstance(mActivity)
											.startLaunchPoint(mActivity);

									HashMap<String, String> map = new HashMap<String, String>();
									map.put("dest", "get more");
									MobclickAgent.onEvent(mActivity,
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
									MobclickAgent.onEvent(mActivity,
											"needMorePoint", map);
								}
							}).show();
		}

		MobclickAgent.onEvent(mActivity, "pushCount");

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("from", "network");
		MobclickAgent.onEvent(mActivity, "pushType", map);
	}

	private void startItemAnim(final View view) {
		Animation rotateAnim = new ItemRotateAnimation(10, 0, view);
		rotateAnim.setDuration(350);
		rotateAnim.setInterpolator(new AccelerateInterpolator());

		Animation scaleAnim = new ScaleAnimation(0.95f, 1.0f, 0.95f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		scaleAnim.setDuration(350);
		scaleAnim.setInterpolator(new AccelerateInterpolator());

		Animation translateAnim = new TranslateAnimation(Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, 0, Animation.RELATIVE_TO_SELF, 0.2f,
				Animation.ABSOLUTE, 0);
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
		public void initialize(int width, int height, int parentWidth,
				int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			mCamera = new Camera();
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			final float degree = mFromDegree
					+ ((mToDegree - mFromDegree) * interpolatedTime);
			final Matrix matrix = t.getMatrix();

			mCamera.save();
			mCamera.rotateX(degree);
			mCamera.getMatrix(matrix);
			mCamera.restore();

			matrix.preTranslate(-(mView.getWidth() / 2),
					-(mView.getHeight() / 2));
			matrix.postTranslate(mView.getWidth() / 2, mView.getHeight() / 2);
		}
	}
}
