package com.aituidao.android.data;

public class BookListRequest {
	public static final int SORT_TYPE_TIME = 1;
	public static final int SORT_TYPE_HOT = 2;

	public int sortType;
	public long pageNo;
	public int count;

	public BookListRequest() {
	}

	public BookListRequest(int sortType, long pageNo, int count) {
		this.sortType = sortType;
		this.pageNo = pageNo;
		this.count = count;
	}
}
