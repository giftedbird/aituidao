package com.aituidao.android.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
	public long mId;
	public String mTitle;
	public String mAuthor;
	public String mIntro;
	// TODO
	public int mCoverUrl;
	public int mPushCount;
	
	public Book() {
	}
	
	public Book(Parcel source) {
		mId = source.readLong();
		mTitle = source.readString();
		mAuthor = source.readString();
		mIntro = source.readString();
		// TODO
		mCoverUrl = source.readInt();
		mPushCount = source.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		dest.writeString(mTitle);
		dest.writeString(mAuthor);
		dest.writeString(mIntro);
		// TODO
		dest.writeInt(mCoverUrl);
		dest.writeInt(mPushCount);
	}
	
	public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
		@Override
		public Book createFromParcel(Parcel source) {
			return new Book(source);
		}

		@Override
		public Book[] newArray(int size) {
			return new Book[size];
		}
	};
}
