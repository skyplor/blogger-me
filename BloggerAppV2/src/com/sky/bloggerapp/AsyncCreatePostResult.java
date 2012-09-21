package com.sky.bloggerapp;

import com.google.api.services.blogger.model.Post;

public class AsyncCreatePostResult
{

	public AsyncCreatePostResult(Post post, String resultDialogTitle, String resultDialogMessage)
	{
		super();
		this.post = post;
		this.resultDialogTitle = resultDialogTitle;
		this.resultDialogMessage = resultDialogMessage;
	}

	public Post getPost()
	{
		return post;
	}

	public String getResultDialogTitle()
	{
		return resultDialogTitle;
	}

	public String getResultDialogMessage()
	{
		return resultDialogMessage;
	}

	private Post post;
	private String resultDialogTitle;
	private String resultDialogMessage;
}
