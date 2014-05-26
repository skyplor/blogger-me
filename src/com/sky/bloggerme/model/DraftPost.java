package com.sky.bloggerme.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

// TODO: Auto-generated Javadoc
/**
 * The Class DraftPost.
 */
@DatabaseTable
public class DraftPost
{
	
	/** The id that is generated automatically in the table, which represents a unique draft. */
	@DatabaseField(generatedId=true)
	private int id;
	
	/** The title. */
	@DatabaseField
	private String title;
	
	/** The labels. */
	@DatabaseField
	private String labels; //json encoded labels
	
	/** The date the post is created / published */
	@DatabaseField
	private String createdAt;
	
	/** The content. */
	@DatabaseField
	private String content;

	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title the title to set
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
	 * @param labels the labels to set
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
	 * @param createdAt the createdAt to set
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
	 * @param content the content to set
	 */
	public void setContent(String content)
	{
		this.content = content;
	}
	
}
