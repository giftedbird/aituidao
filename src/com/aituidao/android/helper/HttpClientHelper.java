package com.aituidao.android.helper;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;

import android.net.http.AndroidHttpClient;
import android.text.TextUtils;

import com.aituidao.android.config.Config;

public class HttpClientHelper {
	public static String requestStr(String userAgent, String url, String postStr) {
		if (userAgent == null) {
			userAgent = Config.DEFAULT_USER_AGENT;
		}

		String result = null;

		AndroidHttpClient httpClient = null;
		try {
			httpClient = AndroidHttpClient.newInstance(userAgent);

			HttpUriRequest urlRequest = null;
			if (TextUtils.isEmpty(postStr)) {
				urlRequest = new HttpGet(url);
			} else {
				urlRequest = new HttpPost(url);
				((HttpPost) urlRequest).setEntity(new StringEntity(postStr));
			}

			result = httpClient.execute(urlRequest, new BasicResponseHandler());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null) {
				httpClient.close();
			}
		}

		return result;
	}

	public static int requestStatusCode(String userAgent, String url,
			String postStr) {
		if (userAgent == null) {
			userAgent = Config.DEFAULT_USER_AGENT;
		}

		AndroidHttpClient httpClient = null;

		try {
			httpClient = AndroidHttpClient.newInstance(userAgent);

			HttpUriRequest urlRequest = null;
			if (TextUtils.isEmpty(postStr)) {
				urlRequest = new HttpGet(url);
			} else {
				urlRequest = new HttpPost(url);
				((HttpPost) urlRequest).setEntity(new StringEntity(postStr));
			}

			HttpResponse response = httpClient.execute(urlRequest);

			return response.getStatusLine().getStatusCode();
		} catch (Exception e) {
			return -1;
		} finally {
			if (httpClient != null) {
				httpClient.close();
			}
		}
	}
}
