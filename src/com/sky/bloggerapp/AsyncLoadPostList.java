package com.sky.bloggerapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.blogger.model.Post;
import com.google.api.services.blogger.model.PostList;

/**
 * Asynchronously load the post list with a progress dialog.
 * 
 * @author Sky Pay
 */

public class AsyncLoadPostList extends AsyncTask<Void, Void, List<Post>>
{

	/** TAG for logging. */
	private static final String TAG = "AsyncLoadPostList";

	private final PostListActivity postListActivity;
	private final ProgressDialog dialog;
	private com.google.api.services.blogger.Blogger service;

	AsyncLoadPostList(PostListActivity postListActivity)
	{
		this.postListActivity = postListActivity;
		service = postListActivity.service;
		dialog = new ProgressDialog(postListActivity);
	}

	@Override
	protected void onPreExecute()
	{
		dialog.setMessage("Loading post list...");
		dialog.show();
	}

	@Override
	protected List<Post> doInBackground(Void... arg0)
	{
		try
		{
			List<Post> result = new ArrayList<Post>();
			com.google.api.services.blogger.Blogger.Posts.List postsListAction = service.posts().list(postListActivity.getBlogID()).setFields("items(id,title),nextPageToken");
			PostList posts = postsListAction.execute();

			// Retrieve up to five pages of results.
			int page = 1;

			while (posts.getItems() != null && page < 10)
			{
				page++;
				result.addAll(posts.getItems());
				String pageToken = posts.getNextPageToken();
				if (pageToken == null)
				{
					break;
				}
				postsListAction.setPageToken(pageToken);
				posts = postsListAction.execute();
			}
			return result;
		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage());
			return Collections.emptyList();
		}
	}

	@Override
	protected void onPostExecute(List<Post> result)
	{
		dialog.dismiss();
		Log.v(TAG, result.get(0).getTitle());
		postListActivity.setModel(result);
	}
}
