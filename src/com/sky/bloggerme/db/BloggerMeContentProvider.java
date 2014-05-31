package com.sky.bloggerme.db;

import com.sky.bloggerme.model.DraftPost;
import com.tojc.ormlite.android.OrmLiteSimpleContentProvider;
import com.tojc.ormlite.android.framework.MatcherController;
import com.tojc.ormlite.android.framework.MimeTypeVnd.SubType;

public class BloggerMeContentProvider extends OrmLiteSimpleContentProvider<DatabaseHelper>
{

	@Override
	protected Class<DatabaseHelper> getHelperClass()
	{
		return DatabaseHelper.class;
	}

	@Override
	public boolean onCreate()
	{
		setMatcherController(new MatcherController().add(DraftPost.class, SubType.DIRECTORY, "", Contract.DraftPost.CONTENT_URI_PATTERN_MANY).add(DraftPost.class, SubType.ITEM, "#", Contract.DraftPost.CONTENT_URI_PATTERN_ONE));
		return true;
	}

}