package com.aituidao.android.data;

public class BookPushRequest {
	public String addr;
	public long id;

	public BookPushRequest() {
	}

	public BookPushRequest(String addr, long id) {
		this.addr = addr;
		this.id = id;
	}
}
