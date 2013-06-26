package com.aituidao.android.listadapter;

import java.util.List;

import com.aituidao.android.R;
import com.aituidao.android.data.Book;

import android.content.Context;
import android.content.res.AssetManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookListAdapter extends BaseAdapter {
	private Context mContext;
	private List<Book> mList;
	private LayoutInflater mLayoutInflater;
	
	public BookListAdapter(Context context, List<Book> list) {
		mContext = context.getApplicationContext();
		mList = list;
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int pos) {
		return mList.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	private static class ViewHolder {
		private ImageView mCoverIv;
		private TextView mTitleTv;
		private TextView mAuthorTv;
		private TextView mIntroTv;
		private TextView mPushCountTv;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.book_list_adapter_item, null);
			
			ViewHolder holder = new ViewHolder();
			holder.mCoverIv = (ImageView) convertView.findViewById(R.id.item_book_cover_iv);
			holder.mTitleTv = (TextView) convertView.findViewById(R.id.item_book_title_tv);
			holder.mAuthorTv = (TextView) convertView.findViewById(R.id.item_book_author_tv);
			holder.mIntroTv = (TextView) convertView.findViewById(R.id.item_book_intro_tv);
			holder.mPushCountTv = (TextView) convertView.findViewById(R.id.item_book_push_count_tv);
			
			convertView.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		Book book = mList.get(position);
		
		// TODO 这里是url，要下载，现在只是临时代码
		holder.mCoverIv.setImageResource(book.mCoverUrl);
		// TODO
		
		holder.mTitleTv.setText(book.mTitle);
		
		holder.mAuthorTv.setText(book.mAuthor);
		
		holder.mIntroTv.setText(book.mIntro);
		
		String pushCountStrTail = mContext.getString(R.string.push_count_str_tail);
		holder.mPushCountTv.setText("" + book.mPushCount + pushCountStrTail);
		
		return convertView;
	}
}
