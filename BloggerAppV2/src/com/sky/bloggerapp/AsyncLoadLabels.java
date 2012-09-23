package com.sky.bloggerapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.blogger.model.Blog;
import com.google.api.services.blogger.model.BlogList;
import com.google.api.services.blogger.model.Post;
import com.google.api.services.blogger.model.PostList;
import com.sky.bloggerapp.util.Constants;

public class AsyncLoadLabels extends AsyncTask<Post, Void, List<String>>
{
	private static final String TAG = "AsyncLoadLabels";

	private final CreatePostActivity createPostActivity;
	private final ProgressDialog dialog;
	private com.google.api.services.blogger.Blogger service;

	AsyncLoadLabels(CreatePostActivity createPostActivity)
	{
		Log.v(TAG, "start of LoadLabels async task");
		this.createPostActivity = createPostActivity;
		service = createPostActivity.service;
		dialog = new ProgressDialog(createPostActivity);
	}

	@Override
	protected void onPreExecute()
	{
		Log.v(TAG, "Popping up waiting dialog");
		dialog.setMessage("Getting Labels...");
		dialog.show();
	}

	@Override
	protected List<String> doInBackground(Post... params)
	{
		try
		{
			List<String> result = new ArrayList<String>();
			com.google.api.services.blogger.Blogger.Posts.List postsListAction = service.posts().list(createPostActivity.getBlogID()).setFields("items/labels,nextPageToken");
			PostList posts = postsListAction.execute();

			boolean label_exist = false;

			// Set the total number of labels
			int totallabels = 0;

			while (posts.getItems() != null && !posts.getItems().isEmpty())
			{
				// Iterate through the all the posts
				for (int i = 0; i < posts.getItems().size(); i++)
				{
					// Iterate through the list of labels of each indivdual post
					for (String label : posts.getItems().get(i).getLabels())
					{
						if (totallabels == 0)
						{
							result.add(label);
							totallabels++;
						}
						else
						{
							// Compare with each label in the result list by iterating through the result list
							for (int j = 0; j < totallabels; j++)
							{
								if (label.equals(result.get(j)))
								{
									label_exist = true;
									break;
								}
								label_exist = false;
							}
							if (!label_exist)
							{
								result.add(label);
								totallabels++;
							}
						}
					}
				}
				// Pagination logic
				String pageToken = posts.getNextPageToken();
				if (pageToken == null)
				{
					break;
				}
				postsListAction.setPageToken(pageToken);
				posts = postsListAction.execute();
			}
			System.out.println("Size of result: " + result.size());
			for (int k = 0; k < result.size(); k++)
			{
				System.out.println("Result[" + k + "]: " + result.get(k));
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
	protected void onPostExecute(List<String> result)
	{
		Log.v(TAG, "Async complete, pulling down dialog");
		dialog.dismiss();
		createPostActivity.setModel(result);
	}
}
