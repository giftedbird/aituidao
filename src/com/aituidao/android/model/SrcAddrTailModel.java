package com.aituidao.android.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.aituidao.android.config.Config;
import com.aituidao.android.data.SrcAddrTailCheckRequest;
import com.aituidao.android.data.SrcAddrTailCheckResponse;
import com.aituidao.android.helper.HttpClientHelper;
import com.alibaba.fastjson.JSON;

public class SrcAddrTailModel {
	private static final int NEW_TAIL = 1;

	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;

	private static SrcAddrTailModel mInstance = null;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NEW_TAIL:
				setSrcAddrTailAndInvlidPushAddr((String) msg.obj);
				break;
			}
		}
	};

	private SrcAddrTailModel(Context context) {
		mContext = context.getApplicationContext();

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		mEditor = mSharedPreferences.edit();
	}

	public synchronized static SrcAddrTailModel getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SrcAddrTailModel(context);
		}

		return mInstance;
	}

	private static final String SRC_ADDR_TAIL = "src_addr_tail";

	public String getSrcAddrTail() {
		String tail = mSharedPreferences.getString(SRC_ADDR_TAIL,
				Config.DEFAULT_SRC_ADDR_TAIL);

		if (TextUtils.isEmpty(tail)) {
			tail = Config.DEFAULT_SRC_ADDR_TAIL;
		}

		return tail;
	}

	private void setSrcAddrTailAndInvlidPushAddr(String newTail) {
		PushSettingModel.getInstance(mContext).clearAllPushAddressTrusted();

		mEditor.putString(SRC_ADDR_TAIL, newTail).commit();
	}

	public void checkNewSrcAddrSilently() {
		final String tail = getSrcAddrTail();

		new Thread(new Runnable() {
			@Override
			public void run() {
				String postStr = JSON.toJSONString(new SrcAddrTailCheckRequest(
						tail));
				String responseStr = HttpClientHelper.requestStr(mContext,
						Config.DEFAULT_USER_AGENT,
						Config.SRC_ADDR_TAIL_CHECK_URL, postStr);

				SrcAddrTailCheckResponse response = null;
				try {
					response = JSON.parseObject(responseStr,
							SrcAddrTailCheckResponse.class);
				} catch (Exception e) {
				}

				if (response != null) {
					if (response.status == SrcAddrTailCheckResponse.OK) {
						String newTail = response.newTail;

						if ((newTail != null) && (!newTail.equals(tail))) {
							if (newTail.contains("@")) {
								mHandler.sendMessage(mHandler.obtainMessage(
										NEW_TAIL, newTail));
							}
						}
					}
				}
			}
		}).start();
	}
}
