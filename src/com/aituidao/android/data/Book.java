package com.aituidao.android.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
	public long id;
	public String title;
	public String author;
	public String intro;
	// TODO
	public int coverUrl;
	public int pushCount;
	
	public Book() {
	}
	
	public Book(Parcel source) {
		id = source.readLong();
		title = source.readString();
		author = source.readString();
		intro = source.readString();
		// TODO
		coverUrl = source.readInt();
		pushCount = source.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(title);
		dest.writeString(author);
		dest.writeString(intro);
		// TODO
		dest.writeInt(coverUrl);
		dest.writeInt(pushCount);
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
