package com.sky.bloggerme;

import java.io.IOException;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.blogger.model.Post;
import com.sky.bloggerme.util.Constants;

/**
 * Asynchronously load a post with a progress dialog.
 * 
 * @author Sky Pay
 */
public class AsyncLoadPost extends AsyncTask<String, Void, Post>
{

	/** TAG for logging. */
	private static final String TAG = "AsyncLoadPostList";

	/** The post display activity. */
	private final PostDisplayActivity postDisplayActivity;
	
	/** The dialog. */
	private final ProgressDialog dialog;
	
	/** The service. */
	private com.google.api.services.blogger.Blogger service;

	/**
	 * Instantiates a new async load post.
	 *
	 * @param postDisplayActivity the post display activity
	 */
	AsyncLoadPost(PostDisplayActivity postDisplayActivity)
	{
		this.postDisplayActivity = postDisplayActivity;
		service = postDisplayActivity.service;
		dialog = new ProgressDialog(postDisplayActivity);
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute()
	{
		dialog.setMessage("Loading post list...");
		dialog.show();
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Post doInBackground(String... postIds)
	{
		try
		{
			String postId = postIds[0];
			return service.posts().get(Constants.BLOG_ID, postId).setFields("title,content,labels").execute();
		}
		catch (UserRecoverableAuthIOException e)
		{
			dialog.dismiss();
			return postDisplayActivity.requestAuth(e);
		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage());
			dialog.dismiss();
			return new Post().setTitle(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Post result)
	{
		dialog.dismiss();
		postDisplayActivity.display(result);
	}
}
