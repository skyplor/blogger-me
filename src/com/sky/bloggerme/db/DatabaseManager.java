package com.sky.bloggerme.db;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.sky.bloggerme.model.DraftPost;
import com.sky.bloggerme.model.Label;

/**
 * The Class DatabaseManager.
 */
public class DatabaseManager
{

	/** The instance. */
	static private DatabaseManager instance;

	/**
	 * Creates a new database manager instance if none exists.
	 * 
	 * @param ctx
	 *            the context
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
	 * @param ctx
	 *            the ctx
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
	
	/**
	 * Gets the all labels.
	 * 
	 * @return all the labels
	 */
	public List<Label> getAllLabels()
	{
		List<Label> labels = null;
		try
		{
			labels = getHelper().getLabelsDao().queryForAll();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return labels;
	}

	/**
	 * Adds the draft post.
	 * 
	 * @param draftPost
	 *            the draft post
	 * @return true if db is updated successfully
	 */
	public Boolean addDraftPost(DraftPost draftPost)
	{
		Boolean updated = false;
		try
		{
			int rowsUpdated = getHelper().getDraftPostDao().create(draftPost);
			Log.d("DatabaseManager", "Rows Updated: " + rowsUpdated);
			if (rowsUpdated > 0)
			{
				updated = true;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return updated;
	}
	
	/**
	 * Adds the labels into DB.
	 * 
	 * @param label
	 *            the label
	 * @return true if db is updated successfully
	 */
	public Boolean addLabels(Label label)
	{
		Boolean updated = false;
		try
		{
			int rowsUpdated = getHelper().getLabelsDao().create(label);
			Log.d("DatabaseManager", "Rows Updated: " + rowsUpdated);
			if (rowsUpdated > 0)
			{
				updated = true;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return updated;
	}

	/**
	 * Update draft post.
	 * 
	 * @param draftPost
	 *            the draft post
	 * @return true if db is updated successfully
	 */
	public Boolean updateDraftPost(DraftPost draftPost)
	{
		Boolean updated = false;
		Log.d("DatabaseManager", "Content: " + draftPost.getContent());
		try
		{
			int rowsUpdated = getHelper().getDraftPostDao().update(draftPost);
			Log.d("DatabaseManager", "Rows Updated: " + rowsUpdated);
			if (rowsUpdated > 0)
			{
				updated = true;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		Log.d("DatabaseManager", "updated: " + updated);
		return updated;
	}

	/**
	 * Removes the draft post.
	 * 
	 * @param draftPost
	 *            the draft post
	 * @return true if db is draft post is deleted successfully from db
	 */
	public Boolean removeDraftPost(int draftPostID)
	{
		Boolean updated = false;
		try
		{
			int rowsUpdated = getHelper().getDraftPostDao().deleteById(draftPostID);
			Log.d("DatabaseManager", "Rows Updated: " + rowsUpdated);
			if (rowsUpdated > 0)
			{
				updated = true;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		Log.d("DatabaseManager", "updated: " + updated);
		return updated;

	}

	/**
	 * Gets the draft post with id.
	 * 
	 * @param draftPostId
	 *            the draft post id in database
	 * @return the draft post with the given id, null if there's zero or more than 1 draft post with that id
	 */
	public DraftPost getDraftPostWithId(int draftPostId)
	{
		DraftPost draftPost = null;
		try
		{
			draftPost = getHelper().getDraftPostDao().queryForId(draftPostId);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return draftPost;
	}

	public <T> boolean drop_recreateTable(Class<T> dataClass) throws SQLException
	{
		boolean success = false;
		int dropStatementsExecuted = getHelper().dropTable(dataClass);
		int recreateStatementsExecuted = getHelper().recreateTable(dataClass);
		if (dropStatementsExecuted > 0 && recreateStatementsExecuted > 0)
		{
			success = true;
		}
		return success;
	}

}
