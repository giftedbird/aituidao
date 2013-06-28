package com.aituidao.android.data;

import java.util.List;

public class BookListResponse {
	public static final int OK = 0;
	
	public int status;
	public long nextPageNum;
	public List<Book> bookList;
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public long getNextPageNum() {
		return nextPageNum;
	}
	
	public void setNextPageNum(long nextPageNum) {
		this.nextPageNum = nextPageNum;
	}
	
	public List<Book> getBookList() {
		return bookList;
	}
	
	public void setBookList(List<Book> bookList) {
		this.bookList = bookList;
	}
}
