package com.sky.bloggerme.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class Contract
{
	/** The Constant DATABASE_NAME. */
	public static final String DATABASE_NAME = "BloggerMeDB.db";
	// any time you make changes to your database objects, you may have to increase the database version
	/** The Constant DATABASE_VERSION. */
	public static final int DATABASE_VERSION = 3;

	public static final String AUTHORITY = "com.sky.bloggerme.authority";

	// DraftPost table info
	public static class DraftPost implements BaseColumns
	{
		public static final String TABLENAME = "draftpost";

		public static final String CONTENT_URI_PATH = TABLENAME;

		public static final String MIMETYPE_TYPE = TABLENAME;
		public static final String MIMETYPE_NAME = AUTHORITY + ".provider";

		// field info
		public static final String LABELS = "labels";
		public static final String CREATEDAt = "createdAt";
		public static final String CONTENT = "content";

		// content uri pattern code
		public static final int CONTENT_URI_PATTERN_MANY = 1;
		public static final int CONTENT_URI_PATTERN_ONE = 2;

		// Refer to activity.
		public static final Uri contentUri = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY).appendPath(CONTENT_URI_PATH).build();

	}
}
