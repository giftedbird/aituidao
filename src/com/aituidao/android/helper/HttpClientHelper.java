package com.aituidao.android.helper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;

import android.net.http.AndroidHttpClient;
import android.text.TextUtils;

import com.aituidao.android.config.Config;
import com.aituidao.android.config.PersonalConfig;

public class HttpClientHelper {

	public static String requestStrMe(String url, String postStr) {
		Map<String, String> postMap = null;
		if (!TextUtils.isEmpty(postStr)) {
			postMap = new HashMap<String, String>();
			postMap.put(PersonalConfig.URL_POST_DATA_KEY, postStr);
		}

		AndroidHttpClient httpClient = null;
		try {
			httpClient = getNewHttpClient(Config.DEFAULT_USER_AGENT);
			HttpUriRequest urlRequest = getHttpUriRequest(url, postMap);
			return httpClient.execute(urlRequest, new BasicResponseHandler());
		} catch (Exception e) {
			return null;
		} finally {
			if (httpClient != null) {
				httpClient.close();
			}
		}
	}

	public static int requestStatusCodeOther(String userAgent, String url,
			Map<String, String> postMap) {
		AndroidHttpClient httpClient = null;
		try {
			httpClient = getNewHttpClient(userAgent);
			HttpUriRequest urlRequest = getHttpUriRequest(url, postMap);
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

	private static AndroidHttpClient getNewHttpClient(String userAgent) {
		return AndroidHttpClient.newInstance(userAgent);
	}

	private static HttpUriRequest getHttpUriRequest(String url,
			Map<String, String> postMap) throws UnsupportedEncodingException {
		HttpUriRequest urlRequest = null;
		if (postMap == null) {
			urlRequest = new HttpGet(url);
		} else {
			urlRequest = new HttpPost(url);

			List<NameValuePair> list = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : postMap.entrySet()) {
				list.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
			}

			UrlEncodedFormEntity data = new UrlEncodedFormEntity(list);

			((HttpPost) urlRequest).setEntity(data);
		}

		return urlRequest;
	}
}
