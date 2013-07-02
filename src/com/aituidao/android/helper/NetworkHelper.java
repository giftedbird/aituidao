package com.aituidao.android.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper {
	public static boolean isConnectionAvailable(Context cotext) {
		boolean flag = false;

		ConnectivityManager connectivityManager = (ConnectivityManager) cotext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager != null) {
			NetworkInfo activeNetworkInfo = connectivityManager
					.getActiveNetworkInfo();
			if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
				flag = false;
			} else {
				flag = true;
			}
		}

		return flag;
	}
}
