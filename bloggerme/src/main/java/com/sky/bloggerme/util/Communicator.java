package com.sky.bloggerme.util;

import com.sky.bloggerme.model.DraftPost;

public interface Communicator
{
	public void respond(DraftPost dPost);
	
	public void restartLoader();
	
}
