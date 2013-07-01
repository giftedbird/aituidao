package com.aituidao.android.model;

import android.content.Context;

public class ImageDownloadAndCacheModel {
	private static ImageDownloadAndCacheModel mInstance = null;

	private Context mContext;

	private ImageDownloadAndCacheModel(Context context) {
		mContext = context.getApplicationContext();
	}

	public static synchronized ImageDownloadAndCacheModel getInstance(
			Context context) {
		if (mInstance == null) {
			mInstance = new ImageDownloadAndCacheModel(context);
		}

		return mInstance;
	}
}
