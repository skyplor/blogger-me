package com.sky.bloggerme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.blogger.Blogger.Blogs;
import com.google.api.services.blogger.Blogger.Blogs.ListByUser;
import com.google.api.services.blogger.model.Blog;
import com.google.api.services.blogger.model.BlogList;

/**
 * Asynchronously load the blog list with a progress dialog.
 * 
 * @author Sky Pay
 */

public class AsyncLoadBlogList extends AsyncTask<Void, Void, List<Blog>>
{

	/** TAG for logging. */
	private static final String TAG = "AsyncLoadBlogList";

	/** The blog list activity. */
	private final BlogListActivity blogListActivity;
	
	/** The dialog. */
	private final ProgressDialog dialog;
	
	/** The service. */
	private com.google.api.services.blogger.Blogger service;

	/**
	 * Instantiates a new async load blog list.
	 *
	 * @param blogListActivity the blog list activity
	 */
	AsyncLoadBlogList(BlogListActivity blogListActivity)
	{
		this.blogListActivity = blogListActivity;
		service = blogListActivity.service;
		dialog = new ProgressDialog(blogListActivity);
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute()
	{
		dialog.setMessage("Loading blog list...");
		dialog.show();
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected List<Blog> doInBackground(Void... arg0)
	{
		try
		{
			List<Blog> result = new ArrayList<Blog>();
			Blogs blogs = service.blogs();
			if (blogs != null)
			{
				ListByUser blogListByUserAction = blogs.listByUser("self").setFields("items(description,id,name,posts/totalItems,updated)");
				if (blogListByUserAction != null)
				{
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
				}
			}
			return result;
		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage());
			// blogListActivity.setToast(e.getMessage());
			blogListActivity.handleGoogleException(e);
			return Collections.emptyList();
		}
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(List<Blog> result)
	{
		dialog.dismiss();
		if (result != null && result.size() > 0)
		{
			// Log.v(TAG, result.get(0).getName());
			blogListActivity.setModel(result);
		}
		// blogListActivity.setToast("I couldn't find any of your blogs. Please try again later");
	}
}
