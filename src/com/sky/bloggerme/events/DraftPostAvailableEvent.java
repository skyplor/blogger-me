package com.sky.bloggerme.events;

import com.sky.bloggerme.model.DraftPost;

/**
 * Simple event class that will be used in our event bus to carry a jUser object
 *
 */
public class DraftPostAvailableEvent
{
	// the contact object being sent using the bus
	private DraftPost dPost;

	public DraftPostAvailableEvent(DraftPost dPost)
	{
		this.dPost = dPost;
	}

	/**
	 * @return the dPost
	 */
	public DraftPost getDPost()
	{
		return dPost;
	}
}
