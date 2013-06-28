package com.aituidao.android.data;

import java.util.List;

public class BookListResponse {
	public static final int OK = 1;
	
	public int status;
	public long nextPageNum;
	public List<Book> bookList;
}
