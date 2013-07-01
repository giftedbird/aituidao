package com.aituidao.android.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;

import com.aituidao.android.config.Config;

public class ImageDownloadAndCacheModel {
	private static ImageDownloadAndCacheModel mInstance = null;

	private static class BitmapInfo {
		private Bitmap mBitmap;
		private int mSize;
	}

	private static class BitmapUrl {
		private BitmapInfo mBitmapInfo;
		private String mUrl;
	}

	public static interface GetBitmapCB {
		public void onGetBitmapSuccess(String url, Bitmap bitmap);

		public void onGetBitmapError(String url);
	}

	private Context mContext;

	private LruCache<String, BitmapInfo> mCache;

	private Set<GetBitmapCB> mGetBitmapCBSet = new HashSet<GetBitmapCB>();

	private Set<String> mProcessingUrlSet = new HashSet<String>();

	private String mImageFolderPath;

	private ExecutorService mLoadingEs = Executors.newFixedThreadPool(3);
	private ExecutorService mDownloadingEs = Executors.newFixedThreadPool(3);

	private static final int GOT_FROM_NETWORK = 1;
	private static final int GOT_FROM_LOCAL = 2;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GOT_FROM_NETWORK:
				startLoadImage((String) msg.obj);
				break;

			case GOT_FROM_LOCAL:
				BitmapUrl bitmapUrl = (BitmapUrl) msg.obj;

				mProcessingUrlSet.remove(bitmapUrl.mUrl);

				if (bitmapUrl.mBitmapInfo.mBitmap != null) {
					mCache.put(bitmapUrl.mUrl, bitmapUrl.mBitmapInfo);

					callGetBitmapCBSuccess(bitmapUrl.mUrl,
							bitmapUrl.mBitmapInfo.mBitmap);
				} else {
					callGetBitmapCBError(bitmapUrl.mUrl);
				}
				break;
			}
		}
	};

	private ImageDownloadAndCacheModel(Context context) {
		mContext = context.getApplicationContext();

		int memClass = ((ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		int cacheSizeMB = 1024 * 1024 * memClass / 10;
		mCache = new LruCache<String, BitmapInfo>(cacheSizeMB) {
			@Override
			protected int sizeOf(String key, BitmapInfo value) {
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

		File dir = new File(mImageFolderPath);
		dir.mkdirs();
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

	private void callGetBitmapCBSuccess(String url, Bitmap bitmap) {
		for (GetBitmapCB cb : mGetBitmapCBSet) {
			cb.onGetBitmapSuccess(url, bitmap);
		}
	}

	private void callGetBitmapCBError(String url) {
		for (GetBitmapCB cb : mGetBitmapCBSet) {
			cb.onGetBitmapError(url);
		}
	}

	public Bitmap getBitmapFromMem(String url) {
		if (url == null) {
			return null;
		}

		BitmapInfo info = mCache.get(url);
		if (info == null) {
			return null;
		} else {
			return info.mBitmap;
		}
	}

	public Bitmap getBitmap(String url) {
		if (url == null) {
			return null;
		}

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
		File file = getFileFromUrl(url);
		return (!(file.exists() && file.canRead()));
	}

	private void startLoadImage(final String url) {
		final File file = getFileFromUrl(url);

		mLoadingEs.execute(new Runnable() {
			@Override
			public void run() {
				Bitmap bitmap = null;
				try {
					bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
				} catch (Exception e) {
					// do nothing
				}

				BitmapUrl btUrl = new BitmapUrl();
				btUrl.mBitmapInfo = new BitmapInfo();

				btUrl.mUrl = url;
				btUrl.mBitmapInfo.mBitmap = bitmap;
				btUrl.mBitmapInfo.mSize = (int) file.length();

				mHandler.sendMessage(mHandler.obtainMessage(GOT_FROM_LOCAL,
						btUrl));
			}
		});
	}

	private void startDownloadImage(final String url) {
		mDownloadingEs.execute(new Runnable() {
			@Override
			public void run() {
				File tempFile = getTempFileFromUrl(url);

				long fileSize;
				if (tempFile.exists()) {
					fileSize = tempFile.length();
				} else {
					fileSize = 0;
				}

				OutputStream fileOutputStream = null;
				InputStream networkInputStream = null;
				HttpURLConnection httpConn = null;
				try {
					fileOutputStream = new BufferedOutputStream(
							new FileOutputStream(tempFile, true));

					URL urll = new URL(url);
					httpConn = (HttpURLConnection) urll.openConnection();
					httpConn.setRequestProperty("RANGE", "bytes=" + fileSize
							+ "-");
					networkInputStream = httpConn.getInputStream();

					while (true) {
						byte[] buffer = new byte[Config.IMG_DOWNLOAD_BUF_SIZE];
						int num = networkInputStream.read(buffer);
						if (num < 0) {
							if (fileOutputStream != null) {
								try {
									try {
										fileOutputStream.flush();
									} catch (IOException e) {
									}

									fileOutputStream.close();
								} catch (IOException e) {
								}

								fileOutputStream = null;
							}

							File newFile = getFileFromUrl(url);
							tempFile.renameTo(newFile);
							break;
						} else {
							fileOutputStream.write(buffer, 0, num);
							fileOutputStream.flush();
						}
					}
				} catch (IOException e) {
					// do nothing
				} finally {
					if (fileOutputStream != null) {
						try {
							fileOutputStream.flush();
						} catch (IOException e) {
						}

						try {
							fileOutputStream.close();
						} catch (IOException e) {
						}
					}

					if (httpConn != null) {
						httpConn.disconnect();
					}

					if (networkInputStream != null) {
						try {
							networkInputStream.close();
						} catch (IOException e) {
						}
					}
				}

				mHandler.sendMessage(mHandler.obtainMessage(GOT_FROM_NETWORK,
						url));
			}
		});
	}

	private File getFileFromUrl(String url) {
		String path = mImageFolderPath + File.separator
				+ convertUrlToFilename(url);
		return new File(path);
	}

	private File getTempFileFromUrl(String url) {
		String path = mImageFolderPath + File.separator
				+ convertUrlToFilename(url) + ".tmp.aituidao";
		return new File(path);
	}

	private static String convertUrlToFilename(String url) {
		return url.replaceAll("[:/.#]", "_");
	}
}
