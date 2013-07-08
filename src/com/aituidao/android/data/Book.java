package com.aituidao.android.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
	public long id;
	public String title;
	public String author;
	public String intro;
	public String coverUrl;
	public int pushCount;
	public int doubanRate;
	public String uploadUserName;

	public Book() {
	}

	public Book(Parcel source) {
		id = source.readLong();
		title = source.readString();
		author = source.readString();
		intro = source.readString();
		coverUrl = source.readString();
		pushCount = source.readInt();
		doubanRate = source.readInt();
		uploadUserName = source.readString();
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
		dest.writeString(coverUrl);
		dest.writeInt(pushCount);
		dest.writeInt(doubanRate);
		dest.writeString(uploadUserName);
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
