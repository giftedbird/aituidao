package com.aituidao.android.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class PushSettingModel {
	public static class PushAddress {
		public String mHead;
		public String mTail;
		public boolean mTrusted;
		public int mIndex;
	}

	public static synchronized PushSettingModel getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PushSettingModel(context);
		}

		return mInstance;
	}

	public void setNewPushAddress(String head, String tail) {
		List<PushAddress> addrList = getPushAddressList();
		int index = -1;
		for (PushAddress addr : addrList) {
			if ((head.equals(addr.mHead)) && (tail.equals(addr.mTail))) {
				index = addr.mIndex;
				break;
			}
		}

		if (index < 0) {
			index = getNextPushAddressIndex();
			setPushAddress(head, tail, index);
			setPushAddressTrusted(false, index);

			index = index + 1;
			if (index >= MAX_PUSH_ADDRESS_COUNT) {
				index = 0;
			}

			setNextPushAddressIndex(index);
		} else {
			setPushAddressTrusted(false, index);
		}
	}

	public void setPushAddressTrusted(String head, String tail, boolean truested) {
		List<PushAddress> addrList = getPushAddressList();
		for (PushAddress addr : addrList) {
			if ((head.equals(addr.mHead)) && (tail.equals(addr.mTail))) {
				setPushAddressTrusted(truested, addr.mIndex);
				break;
			}
		}
	}

	public void clearAllPushAddressTrusted() {
		for (int i = 0; i < MAX_PUSH_ADDRESS_COUNT; i++) {
			setPushAddressTrusted(false, i);
		}
	}

	public List<PushAddress> getPushAddressList() {
		List<PushAddress> result = new ArrayList<PushAddress>();

		for (int i = 0; i < MAX_PUSH_ADDRESS_COUNT; i++) {
			PushAddress pa = getPushAddress(i);

			if (pa == null) {
				continue;
			} else {
				result.add(pa);
			}
		}

		return result;
	}

	private static PushSettingModel mInstance = null;

	private static final int MAX_PUSH_ADDRESS_COUNT = 3;

	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;

	private PushSettingModel(Context context) {
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		mEditor = mSharedPreferences.edit();
	}

	private static final String NEXT_PUSH_ADDRESS_INDEX = "next_push_address_index";

	private int getNextPushAddressIndex() {
		return mSharedPreferences.getInt(NEXT_PUSH_ADDRESS_INDEX, 0);
	}

	private void setNextPushAddressIndex(int index) {
		mEditor.putInt(NEXT_PUSH_ADDRESS_INDEX, index).commit();
	}

	private static final String PUSH_ADDRESS_HEAD_FRONT = "push_address_head_front_";
	private static final String PUSH_ADDRESS_HEAD_END = "push_address_head_end_";
	private static final String PUSH_ADDRESS_TRUSTED = "push_address_trusted_";

	private PushAddress getPushAddress(int index) {
		String head = mSharedPreferences.getString(PUSH_ADDRESS_HEAD_FRONT
				+ index, null);
		String tail = mSharedPreferences.getString(PUSH_ADDRESS_HEAD_END
				+ index, null);
		boolean trusted = mSharedPreferences.getBoolean(PUSH_ADDRESS_TRUSTED
				+ index, false);

		if (TextUtils.isEmpty(head) || TextUtils.isEmpty(tail)) {
			return null;
		} else {
			PushAddress pa = new PushAddress();
			pa.mHead = head;
			pa.mTail = tail;
			pa.mTrusted = trusted;
			pa.mIndex = index;
			return pa;
		}
	}

	private void setPushAddress(String head, String tail, int index) {
		mEditor.putString(PUSH_ADDRESS_HEAD_FRONT + index, head).commit();
		mEditor.putString(PUSH_ADDRESS_HEAD_END + index, tail).commit();
	}

	private void setPushAddressTrusted(boolean trusted, int index) {
		mEditor.putBoolean(PUSH_ADDRESS_TRUSTED + index, trusted).commit();
	}
}
