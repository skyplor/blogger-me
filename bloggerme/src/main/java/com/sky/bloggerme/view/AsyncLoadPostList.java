package com.sky.bloggerme.view;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.blogger.Blogger.Posts;
import com.google.api.services.blogger.model.Post;
import com.google.api.services.blogger.model.PostList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Asynchronously load the post list with a progress dialog.
 * 
 * @author Sky Pay
 */

public class AsyncLoadPostList extends AsyncTask<Void, Void, List<Post>>
{

	/** TAG for logging. */
	private static final String TAG = "AsyncLoadPostList";

	/** The post list activity. */
	private final PostListActivity postListActivity;
	
	/** The dialog. */
	private final ProgressDialog dialog;
	
	/** The service. */
	private com.google.api.services.blogger.Blogger service;

	/**
	 * Instantiates a new async load post list.
	 *
	 * @param postListActivity the post list activity
	 */
	AsyncLoadPostList(PostListActivity postListActivity)
	{
		this.postListActivity = postListActivity;
		service = postListActivity.service;
		dialog = new ProgressDialog(postListActivity);
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
	protected List<Post> doInBackground(Void... arg0)
	{
		try
		{
			List<Post> result = new ArrayList<Post>();
			Posts posts = service.posts();
			if (posts != null)
			{
				Posts.List postsListAction = posts.list(postListActivity.getBlogID()).setFields("items(id,title),nextPageToken");
				PostList postslist = postsListAction.execute();

				// Retrieve up to five pages of results.
				int page = 1;
				if (postslist != null && postslist.size() > 0)
				{
					while (postslist.getItems() != null && page < 10)
					{
						page++;
						result.addAll(postslist.getItems());
						String pageToken = postslist.getNextPageToken();
						if (pageToken == null)
						{
							break;
						}
						postsListAction.setPageToken(pageToken);
						postslist = postsListAction.execute();
					}
				}
			}
			return result;
		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage());
			return Collections.emptyList();
		}
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(List<Post> result)
	{
		dialog.dismiss();
//		Log.v(TAG, result.get(0).getTitle());
		postListActivity.setModel(result);
	}
}
