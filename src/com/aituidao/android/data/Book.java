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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public int getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(int coverUrl) {
		this.coverUrl = coverUrl;
	}

	public int getPushCount() {
		return pushCount;
	}

	public void setPushCount(int pushCount) {
		this.pushCount = pushCount;
	}
}
