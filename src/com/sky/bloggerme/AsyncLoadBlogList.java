package com.sky.bloggerme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.api.services.blogger.model.Blog;
import com.google.api.services.blogger.model.BlogList;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Asynchronously load the blog list with a progress dialog.
 * 
 * @author Sky Pay
 */

public class AsyncLoadBlogList extends AsyncTask<Void, Void, List<Blog>>
{

	/** TAG for logging. */
	private static final String TAG = "AsyncLoadBlogList";

	private final BlogListActivity blogListActivity;
	private final ProgressDialog dialog;
	private com.google.api.services.blogger.Blogger service;

	AsyncLoadBlogList(BlogListActivity blogListActivity)
	{
		this.blogListActivity = blogListActivity;
		service = blogListActivity.service;
		dialog = new ProgressDialog(blogListActivity);
	}

	@Override
	protected void onPreExecute()
	{
		dialog.setMessage("Loading blog list...");
		dialog.show();
	}

	@Override
	protected List<Blog> doInBackground(Void... arg0)
	{
		try
		{
			List<Blog> result = new ArrayList<Blog>();
			com.google.api.services.blogger.Blogger.Blogs.ListByUser blogListByUserAction = service.blogs().listByUser("self").setFields("items(description,id,name,posts/totalItems,updated)");
			// This step sends the request to the server.
			BlogList blogList = blogListByUserAction.execute();

			// Now we can navigate the response.
			if (blogList.getItems() != null && !blogList.getItems().isEmpty())
			{
				int blogCount = 0;
				for (Blog blog : blogList.getItems())
				{
					System.out.println("Blog #" + ++blogCount);
					System.out.println("\tID: " + blog.getId());
					System.out.println("\tName: " + blog.getName());
					System.out.println("\tDescription: " + blog.getDescription());
					System.out.println("\tPost Count: " + blog.getPosts().getTotalItems());
					System.out.println("\tLast Updated: " + blog.getUpdated());
				}
				result.addAll(blogList.getItems());
			}
			return result;
		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage());
			blogListActivity.handleGoogleException(e);
			return Collections.emptyList();
		}
	}

	@Override
	protected void onPostExecute(List<Blog> result)
	{
		dialog.dismiss();
		if (result != null)
		{
			Log.v(TAG, result.get(0).getName());
			blogListActivity.setModel(result);
		}
	}
}
