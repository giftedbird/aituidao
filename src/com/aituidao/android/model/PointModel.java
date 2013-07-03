package com.aituidao.android.model;

import net.youmi.android.offers.OffersManager;
import net.youmi.android.offers.PointsChangeNotify;
import net.youmi.android.offers.PointsManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aituidao.android.config.Config;

public class PointModel {
	private static PointModel mInstance = null;

	public static synchronized PointModel getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PointModel(context);
		}

		return mInstance;
	}

	private Context mContext;
	private PointsManager mPointsManager;

	private PointModel(Context context) {
		mContext = context.getApplicationContext();
		mPointsManager = PointsManager.getInstance(mContext);
	}

	private static final String HAS_POINT = "has_point";

	public int getCurrPoint() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext.getApplicationContext());

		if (!sp.getBoolean(HAS_POINT, false)) {
			sp.edit().putBoolean(HAS_POINT, true).commit();
			mPointsManager.awardPoints(Config.INITIAL_POINT);
		}

		return mPointsManager.queryPoints();
	}

	public boolean spendPoint(int points) {
		return mPointsManager.spendPoints(points);
	}

	public void awardPoint(int points) {
		mPointsManager.awardPoints(points);
	}

	public void startLaunchPoint(Activity activity) {
		OffersManager.getInstance(activity).showOffersWallDialog(activity);
	}

	public void registerNotify(PointsChangeNotify notify) {
		mPointsManager.registerNotify(notify);
	}

	public void unRegisterNotify(PointsChangeNotify notify) {
		mPointsManager.unRegisterNotify(notify);
	}
}
