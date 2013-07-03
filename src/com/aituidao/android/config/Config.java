package com.aituidao.android.config;

import android.app.AlarmManager;

public class Config {
	// TODO
	public static final boolean YM_DEBUG = true;

	public static final int BOOK_LIST_COUNT = 20;

	private static final String BASE_URL = "http://kindleaituidao.sinaapp.com/";
	public static final String PUSH_BOOK_URL = BASE_URL + "push_book/";
	public static final String BOOK_LIST_URL = BASE_URL + "book_list/";
	public static final String SRC_ADDR_TAIL_CHECK_URL = BASE_URL
			+ "src_addr_tail_check/";
	public static final String NEW_URL_ACCESS_URL = BASE_URL
			+ "new_url_access/";

	public static final String NEW_URL_ACCESS_ACTION = "com.aituidao.android.action.alarm_manager.repeat";
	public static final long NEW_URL_ACCESS_ACTION_PERIOD = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

	public static final String DEFAULT_SRC_ADDR_TAIL = "@aituidao.com";

	public static final String DEFAULT_USER_AGENT = "Android";

	public static final String IMG_PATH = "/aituidao/android/image";

	public static final int IMG_DOWNLOAD_BUF_SIZE = 512;

	public static final String WHY_NEED_PUSH_ADDR_URL = "http://www.amazon.cn/gp/help/customer/display.html?nodeId=200843440#s2kemail";

	public static final String WHY_NEED_TRUST_ADDR_URL = "http://www.amazon.cn/gp/help/customer/display.html?nodeId=200843440#approvefrom";

	public static final int INITIAL_POINT = 99;
	// TODO
	public static final int EACH_POINT = 50;
}
