package com.aituidao.android.helper;

import android.content.Context;

public class HttpClientHelper {

	public static String request(Context context, String url, String postStr) {
		String result = "{}";

		try {
			// TODO
			if (url.equals("1")) {
				Thread.sleep(1000);
				result = "{\"status\":1}";
			} else if (url.equals("2")) {
				Thread.sleep(2500);
				result = "{\"bookList\":[{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"【日】松本行弘 \",\"coverUrl\":2130837531,\"id\":9748,\"intro\":\"超级书\",\"pushCount\":90,\"title\":\"代码的未来\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"【港】许中约\",\"coverUrl\":2130837529,\"id\":9304,\"intro\":\"好书\",\"pushCount\":37,\"title\":\"中国近代史\"},{\"author\":\"史玉柱口述 优米网编著\",\"coverUrl\":2130837530,\"id\":9334,\"intro\":\"牛逼书\",\"pushCount\":5,\"title\":\"史玉柱自述\"}],\"nextPageNum\":1,\"status\":1}";
			}
		} catch (Exception e) {
			// do nothing
		}

		return result;
	}
}
