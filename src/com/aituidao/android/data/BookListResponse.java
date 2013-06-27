package com.aituidao.android.data;

import java.util.List;

public class BookListResponse {
	public static final int OK = 0;
	
	public int mState;
	public long mNextPageNum;
	public List<Book> mBookList;
}
