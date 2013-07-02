package com.aituidao.android.helper;

import java.util.ArrayList;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;

import android.net.http.AndroidHttpClient;
import android.text.TextUtils;

import com.aituidao.android.config.Config;
import com.aituidao.android.data.Book;
import com.aituidao.android.data.BookListResponse;
import com.alibaba.fastjson.JSON;

public class HttpClientHelper {
	public static String requestStr(String userAgent, String url, String postStr) {
		if (userAgent == null) {
			userAgent = Config.DEFAULT_USER_AGENT;
		}

		String result = null;

		if (Config.DEBUG) {
			try {
				if (url.equals("1")) {
					Thread.sleep(1000);
					result = "{\"status\":1}";
				} else if (url.equals("2")) {
					Thread.sleep(2500);
					BookListResponse response = new BookListResponse();
					response.status = 1;
					response.nextPageNum = 1;
					response.bookList = new ArrayList<Book>();
					Random random = new Random(System.currentTimeMillis());
					for (int i = 0; i < 20; i++) {
						Book book = new Book();
						switch (random.nextInt(3)) {
						case 0:
							book.author = "（日）麻耶雄嵩 ";
							book.coverUrl = "http://d31i1rfrna7v3n.cloudfront.net/image/20130701/137266854547779.jpg";
							book.id = 50;
							book.intro = "新本格鬼才麻耶雄嵩力作！这个家伙还能写出“中规中矩”的推理？！不信的话，就请阅读这部《贵族侦探》。";
							book.pushCount = 4;
							book.title = "贵族侦探";
							break;

						case 1:
							book.author = "小泉吉宏";
							book.coverUrl = "http://d31i1rfrna7v3n.cloudfront.net/image/20130617/137146121452693.jpg";
							book.id = 52;
							book.intro = "★ 经典中的经典！畅销1800万册，横扫亚洲的“小幸福神书”首次登陆中国内地！";
							book.pushCount = 23;
							book.title = "佛陀与想太多的猪1";
							break;

						case 2:
							book.author = "阿丫";
							book.coverUrl = "http://d31i1rfrna7v3n.cloudfront.net/image/20130617/137146100086799.jpg";
							book.id = 59;
							book.intro = "本书是一本培养着装风格，提升个人品味的书。资深时装造型师阿丫以平实的视角，将看似复杂高深的穿衣之道回归简单，诠释了第一夫人风格、中产LADY风格、“白骨精”风格、文艺青年风格、摇滚GIRL风格等，35种时尚单品，28种混搭技巧，教你聪明搭衣，精明购衣。同时记录了自己的生活感悟、着装心得及一些时装品牌的风格，并在不经意间透露了当下时装行业的小秘密。浸润其中你会慢慢成长为褪去时装，依然FASHION的时尚ICON。";
							book.pushCount = 41;
							book.title = "人群中,你就是那个“例外”";
							break;
						}
						response.bookList.add(book);
					}

					result = JSON.toJSONString(response);
				} else if (url.equals("3")) {
					Thread.sleep(1000);
					result = "{\"status\":1,\"hasNew\":0}";
				}
			} catch (Exception e) {
				// do nothing
			}
		} else {
			AndroidHttpClient httpClient = null;

			try {
				httpClient = AndroidHttpClient.newInstance(userAgent);

				HttpUriRequest urlRequest = null;
				if (TextUtils.isEmpty(postStr)) {
					urlRequest = new HttpGet(url);
				} else {
					urlRequest = new HttpPost(url);
					((HttpPost) urlRequest)
							.setEntity(new StringEntity(postStr));
				}

				result = httpClient.execute(urlRequest,
						new BasicResponseHandler());
			} catch (Exception e) {
				// do nothing
			} finally {
				if (httpClient != null) {
					httpClient.close();
				}
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
