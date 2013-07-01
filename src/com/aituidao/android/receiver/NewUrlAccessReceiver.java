package com.aituidao.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aituidao.android.config.Config;
import com.aituidao.android.model.NewUrlAccessModel;

public class NewUrlAccessReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (Config.NEW_URL_ACCESS_ACTION.equals(intent.getAction())) {
			NewUrlAccessModel.getInstance(context).checkAndStartNewUrlAccess();
		}
	}
}
