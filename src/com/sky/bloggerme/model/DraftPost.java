package com.sky.bloggerme.model;

import java.util.Date;

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
	private Date createdAt;
	
	/** The content. */
	@DatabaseField
	private String content;
	
}
