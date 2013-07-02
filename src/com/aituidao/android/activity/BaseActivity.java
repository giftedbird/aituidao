package com.aituidao.android.activity;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;

public abstract class BaseActivity extends Activity {
	@Override
	protected void onPause() {
		super.onPause();

		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		MobclickAgent.onResume(this);
	}
}
