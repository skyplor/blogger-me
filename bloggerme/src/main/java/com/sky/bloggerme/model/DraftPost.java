package com.sky.bloggerme.model;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sky.bloggerme.db.Contract;
import com.tojc.ormlite.android.annotation.AdditionalAnnotation.DefaultContentMimeTypeVnd;
import com.tojc.ormlite.android.annotation.AdditionalAnnotation.DefaultContentUri;
import com.tojc.ormlite.android.annotation.AdditionalAnnotation.DefaultSortOrder;

// TODO: Auto-generated Javadoc
/**
 * The Class DraftPost.
 */
@DatabaseTable
@DefaultContentUri(authority = Contract.AUTHORITY, path = Contract.DraftPost.CONTENT_URI_PATH)
@DefaultContentMimeTypeVnd(name = Contract.DraftPost.MIMETYPE_NAME, type = Contract.DraftPost.MIMETYPE_TYPE)
public class DraftPost
{

	/** The id that is generated automatically in the table, which represents a unique draft. */
	@DatabaseField(generatedId = true, columnName = BaseColumns._ID)
	@DefaultSortOrder
	private int _id;

	/** The title. */
	@DatabaseField
	private String title;

	/** The labels. */
	@DatabaseField
	private String labels; // json encoded labels

	/** The date the post is created / published */
	@DatabaseField
	private String createdAt;

	/** The content. */
	@DatabaseField
	private String content;

	/** Existing Post in Blogger? If yes, this field will store the postid */
	@DatabaseField
	private String blogPostId;

	/**
	 * @return the _id
	 */
	public int get_Id()
	{
		return _id;
	}

	/**
	 * @param _id
	 *            the id to set
	 */
	public void set_Id(int _id)
	{
		this._id = _id;
	}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @return the labels
	 */
	public String getLabels()
	{
		return labels;
	}

	/**
	 * @param labels
	 *            the labels to set
	 */
	public void setLabels(String labels)
	{
		this.labels = labels;
	}

	/**
	 * @return the createdAt
	 */
	public String getCreatedAt()
	{
		return createdAt;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(String createdAt)
	{
		this.createdAt = createdAt;
	}

	/**
	 * @return the content
	 */
	public String getContent()
	{
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content)
	{
		this.content = content;
	}

	/**
	 * @return the blogPostId
	 */
	public String getBlogPostId()
	{
		return blogPostId;
	}

	/**
	 * @param blogPostId
	 *            the blogPostId to set
	 */
	public void setBlogPostId(String blogPostId)
	{
		this.blogPostId = blogPostId;
	}

	public DraftPost()
	{
		// ORMLite needs a no-arg constructor
	}

	public static DraftPost newInstance(Cursor c)
	{
		DraftPost draft = new DraftPost();
		if (c != null)
		{
			draft.set_Id(c.getInt(c.getColumnIndex("_id")));
			draft.setTitle(c.getString(c.getColumnIndex("title")));
			draft.setLabels(c.getString(c.getColumnIndex("labels")));
			draft.setContent(c.getString(c.getColumnIndex("content")));
			draft.setCreatedAt(c.getString(c.getColumnIndex("createdAt")));
			draft.setBlogPostId(c.getString(c.getColumnIndex("blogPostId")));
		}
		return draft;
	}

}
