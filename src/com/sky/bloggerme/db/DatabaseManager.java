package com.sky.bloggerme.db;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;

import com.sky.bloggerme.model.DraftPost;

/**
 * The Class DatabaseManager.
 */
public class DatabaseManager
{

	/** The instance. */
	static private DatabaseManager instance;

	/**
	 * Inits the.
	 *
	 * @param ctx the ctx
	 */
	static public void init(Context ctx)
	{
		if (null == instance)
		{
			instance = new DatabaseManager(ctx);
		}
	}

	/**
	 * Gets the single instance of DatabaseManager.
	 *
	 * @return single instance of DatabaseManager
	 */
	static public DatabaseManager getInstance()
	{
		return instance;
	}

	/** The helper. */
	private DatabaseHelper helper;

	/**
	 * Instantiates a new database manager.
	 *
	 * @param ctx the ctx
	 */
	private DatabaseManager(Context ctx)
	{
		helper = new DatabaseHelper(ctx);
	}

	/**
	 * Gets the helper.
	 *
	 * @return the helper
	 */
	private DatabaseHelper getHelper()
	{
		return helper;
	}

	/**
	 * Gets the all draft posts.
	 *
	 * @return the all draft posts
	 */
	public List<DraftPost> getAllDraftPosts()
	{
		List<DraftPost> draftPosts = null;
		try
		{
			draftPosts = getHelper().getDraftPostDao().queryForAll();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return draftPosts;
	}
}
