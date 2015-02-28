package com.sky.bloggerme.view;

import com.google.api.services.blogger.model.Post;

/**
 * The Class AsyncCreatePostResult.
 */
public class AsyncCreatePostResult
{

	/**
	 * Instantiates a new async create post result.
	 *
	 * @param post the post
	 * @param resultDialogTitle the result dialog title
	 * @param resultDialogMessage the result dialog message
	 */
	public AsyncCreatePostResult(Post post, String resultDialogTitle, String resultDialogMessage)
	{
		super();
		this.post = post;
		this.resultDialogTitle = resultDialogTitle;
		this.resultDialogMessage = resultDialogMessage;
	}

	/**
	 * Gets the post.
	 *
	 * @return the post
	 */
	public Post getPost()
	{
		return post;
	}

	/**
	 * Gets the result dialog title.
	 *
	 * @return the result dialog title
	 */
	public String getResultDialogTitle()
	{
		return resultDialogTitle;
	}

	/**
	 * Gets the result dialog message.
	 *
	 * @return the result dialog message
	 */
	public String getResultDialogMessage()
	{
		return resultDialogMessage;
	}

	/** The post. */
	private Post post;
	
	/** The result dialog title. */
	private String resultDialogTitle;
	
	/** The result dialog message. */
	private String resultDialogMessage;
}
