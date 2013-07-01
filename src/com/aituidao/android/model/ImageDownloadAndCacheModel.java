package com.aituidao.android.model;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.aituidao.android.config.Config;

public class ImageDownloadAndCacheModel {
	private static ImageDownloadAndCacheModel mInstance = null;

	private static class BitmapHolder {
		private Bitmap mBitmap;
		private int mSize;
	}

	public static interface GetBitmapCB {
		public void onGetBitmapSuccess(String url, Bitmap bitmap);

		public void onGetBitmapError(String url);
	}

	private Context mContext;

	private LruCache<String, BitmapHolder> mCache;

	private Set<GetBitmapCB> mGetBitmapCBSet = new HashSet<GetBitmapCB>();

	private Set<String> mProcessingUrlSet = new HashSet<String>();

	private String mImageFolderPath;

	private ExecutorService mLoadingEs = Executors.newFixedThreadPool(3);
	private ExecutorService mDownloadingEs = Executors.newFixedThreadPool(3);

	private ImageDownloadAndCacheModel(Context context) {
		mContext = context.getApplicationContext();

		int memClass = ((ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		int cacheSizeMB = 1024 * 1024 * memClass / 8;
		mCache = new LruCache<String, BitmapHolder>(cacheSizeMB) {
			@Override
			protected int sizeOf(String key, BitmapHolder value) {
				if (value == null) {
					return 0;
				}

				return value.mSize;
			}
		};

		File file = mContext.getExternalCacheDir();
		if (file == null) {
			file = mContext.getCacheDir();
		}

		mImageFolderPath = file.getAbsolutePath() + Config.IMG_PATH;
	}

	public static synchronized ImageDownloadAndCacheModel getInstance(
			Context context) {
		if (mInstance == null) {
			mInstance = new ImageDownloadAndCacheModel(context);
		}

		return mInstance;
	}

	public void addGetBitmapCB(GetBitmapCB cb) {
		if (cb == null) {
			return;
		}

		mGetBitmapCBSet.add(cb);
	}

	public void removeGetBitmapCB(GetBitmapCB cb) {
		if (cb == null) {
			return;
		}

		mGetBitmapCBSet.remove(cb);
	}

	public Bitmap getBitmapFromMem(String url) {
		BitmapHolder holder = mCache.get(url);
		if (holder == null) {
			return null;
		} else {
			return holder.mBitmap;
		}
	}

	public Bitmap getBitmap(String url) {
		Bitmap bitmap = getBitmapFromMem(url);
		if (bitmap != null) {
			return bitmap;
		}

		if ((!url.startsWith("http://")) && (!url.startsWith("https://"))) {
			return null;
		}

		if (mProcessingUrlSet.contains(url)) {
			return null;
		}

		mProcessingUrlSet.add(url);

		if (needDownload(url)) {
			startDownloadImage(url);
		} else {
			startLoadImage(url);
		}

		return null;
	}

	private boolean needDownload(String url) {
		String path = mImageFolderPath + File.separator
				+ convertUrlToFilename(url);
		File file = new File(path);
		return (!(file.exists() && file.canRead()));
	}

	private static String convertUrlToFilename(String url) {
		return url.replaceAll("[:/.#]", "_");
	}
}
