package com.aituidao.android.listadapter;

import java.util.List;

import com.aituidao.android.data.Book;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class BookListAdapter extends BaseAdapter {
	private Context mContext;
	private List<Book> mList;
	
	public BookListAdapter(Context context, List<Book> list) {
		mContext = context.getApplicationContext();
		mList = list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}
}
