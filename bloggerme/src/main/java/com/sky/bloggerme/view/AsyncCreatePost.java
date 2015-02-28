package com.sky.bloggerme.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.blogger.Blogger.Posts.Update;
import com.google.api.services.blogger.model.Post;
import com.sky.bloggerme.util.Constants;

import java.io.IOException;

/**
 * Asynchronously load a create with a progress dialog.
 * 
 * @author Sky Pay
 */
public class AsyncCreatePost extends AsyncTask<Post, Void, AsyncCreatePostResult>
{

	/** TAG for logging. */
	private static final String TAG = "AsyncCreatePost";

	/** The editor activity. */
	private final EditorActivity editorActivity;

	/** The dialog. */
	private final ProgressDialog dialog;

	/** The service. */
	private com.google.api.services.blogger.Blogger service;

	/** The result post. */
	private Post resultPost;

	/** Whether user is updating or creating a post. */
	private Boolean create = true;

	/**
	 * Instantiates a new async create post.
	 * 
	 * @param editorActivity
	 *            the editor activity
	 * @param create
	 *            the create
	 */
	AsyncCreatePost(EditorActivity editorActivity, Boolean create)
	{
		Log.v(TAG, "start of CreatePost async task");
		this.editorActivity = editorActivity;
		service = editorActivity.service;
		dialog = new ProgressDialog(editorActivity);
		this.create = create;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute()
	{
		Log.v(TAG, "Popping up waiting dialog");
		if (create)
		{
			dialog.setMessage("Creating post...");
		}
		else
		{
			dialog.setMessage("Updating post...");
		}
		dialog.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected AsyncCreatePostResult doInBackground(Post... args)
	{
		Post post = args[0];
		try
		{
			Log.v(TAG, "executing the posts.insert call on Blogger API v3");
			Post postResult;
			if (create)
			{
				postResult = service.posts().insert(Constants.BLOG_ID, post).execute();
			}
			else
			{
				Log.d(TAG, "Blog_id: " + Constants.BLOG_ID + "\nPost_id: " + editorActivity.getBlogPostId() + "\nPost: " + post.getContent());
				Update postsUpdateAction = service.posts().update(Constants.BLOG_ID, editorActivity.getBlogPostId(), post);
				Log.d(TAG, "Updated....");
				postsUpdateAction.setFields("author/displayName,content,published,title,url");
				Log.d(TAG, "Set result fields....");
				Log.d(TAG, "HTTP: " + postsUpdateAction.buildHttpRequestUrl().build());
				postResult = postsUpdateAction.execute();
				Log.d(TAG, "Title: " + postResult.getTitle());
				// Log.d(TAG, "Author: " + post.getAuthor().getDisplayName());
				Log.d(TAG, "Content: " + post.getContent());
				Log.d(TAG, "ID: " + postResult.getId());
			}
			Log.v(TAG, "call succeeded");
			return new AsyncCreatePostResult(postResult, "Post Created/Updated", "Your post is created/updated successfully!");
		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage() == null ? "null" : e.getMessage());
			editorActivity.handleGoogleException(e);

			// This is a less than optimal way of handling this situation.
			// A more elegant solution would involve using a SyncAdaptor...
			return new AsyncCreatePostResult(post, "Create/Update Failed", "Please Retry");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(AsyncCreatePostResult result)
	{
		Log.v(TAG, "Async complete, pulling down dialog");
		dialog.dismiss();
		if (result != null && result.getResultDialogTitle().equals("Post Created/Updated"))
		{
			Log.v(TAG, "Create/Update Post Result is: " + result);
			editorActivity.display(result.getPost());
			createAlertDialog(result.getResultDialogTitle(), result.getResultDialogMessage());
			// editorActivity.onRequestCompleted(result.getPost());
			resultPost = result.getPost();
			if (editorActivity.getBlogPostId() != null)
			{
				resultPost.setId(editorActivity.getBlogPostId());
			}
		}
	}

	/**
	 * Creates the alert dialog.
	 * 
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 */
	private void createAlertDialog(String title, String message)
	{
		final AlertDialog alertDialog = new AlertDialog.Builder(editorActivity).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new Dialog.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				alertDialog.dismiss();
				editorActivity.onRequestCompleted(resultPost);
			}
		});
		alertDialog.show();

	}

}
