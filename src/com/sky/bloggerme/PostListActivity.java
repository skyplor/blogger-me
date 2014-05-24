package com.sky.bloggerme;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.extensions.android3.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.blogger.model.Post;
import com.sky.bloggerme.util.Constants;

// TODO: Auto-generated Javadoc
/**
 * Sample for Blogger API on Android. It shows how to get a list of posts.
 * <p>
 * To enable logging of HTTP requests/responses, change {@link #LOGGING_LEVEL} to {@link Level#CONFIG} or {@link Level#ALL} and run this command:
 * </p>
 * 
 * <pre>
 * adb shell setprop log.tag.HttpTransport DEBUG
 * </pre>
 * 
 * @author Sky Pay
 */

public class PostListActivity extends ListActivity
{
	/** TAG for logging. */
	private static final String TAG = "PostList";

	/** Choose the right HttpTransport depending on Android version. */
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();

	/** The JSON factory to use to marshall data onto the wire. */
	final JsonFactory jsonFactory = new AndroidJsonFactory();

	/** The facade object for accessing Blogger API v3. */
	com.google.api.services.blogger.Blogger service;

	/** HTTP rewriter responsible for managing lifetime of oauth2 credentials. */
	GoogleCredential credential = new GoogleCredential();

	/** Shared Preferences for storing auth credentials. */
	SharedPreferences settings;

	/** Shared Preferences editor for editing auth credentials. */
	SharedPreferences.Editor editor;

	/** The blog title. */
	String authToken, blogTitle;

	/** The title. */
	private TextView title;

	/** The posts. */
	private List<Post> posts;

	/** The blogs btn. */
	Button newPostBtn, blogsBtn;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_list);

		settings = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);
		// ClientCredentials.errorIfNotSpecified();
		getBlogTitle();

		newPostBtn = (Button) this.findViewById(R.id.createbutton);
		newPostBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(PostListActivity.this, EditorActivity.class);
				startActivity(intent);
			}

		});

		blogsBtn = (Button) this.findViewById(R.id.blogsbutton);
		blogsBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(PostListActivity.this, BlogListActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}

		});

		if (getAuthToken())
		{
			service = new com.google.api.services.blogger.Blogger.Builder(transport, jsonFactory, credential).setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY)).setApplicationName("Blogger-me/1.0").build();// null).setApplicationName("Google-BloggerAndroidSample/1.0").setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY)).build();
			Constants.BLOG_ID = getBlogID();
			Logger.getLogger("com.google.api.client").setLevel(Constants.LOGGING_LEVEL);
			new AsyncLoadPostList(this).execute();
		}
		else
		{
			Log.v(TAG, "Unable to obtain authentication token. Please login again.");
		}

	}

	/**
	 * Gets the blog title.
	 *
	 * @return the blog title
	 */
	private void getBlogTitle()
	{
		blogTitle = settings.getString(Constants.PREF_BLOG_NAME, "");
		if (!blogTitle.isEmpty())
		{
			title = (TextView) findViewById(R.id.blogTitle);
			title.setText(blogTitle);
		}

	}

	/**
	 * Gets the blog id.
	 *
	 * @return the blog id
	 */
	public String getBlogID()
	{
		return settings.getString(Constants.PREF_BLOG_ID, "");
	}

	/**
	 * Sets the model.
	 *
	 * @param result the new model
	 */
	public void setModel(List<Post> result)
	{
		this.posts = result;
		if (result != null && result.size() > 0)
		{
			List<String> titles = new ArrayList<String>(result.size());
			for (Post post : posts)
			{
				titles.add(post.getTitle());
			}
			this.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles));
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_blog_list, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_logout:
				doLogout();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Do logout.
	 */
	private void doLogout()
	{
		editor = settings.edit();
		editor.remove(Constants.PREF_ACCOUNT_NAME);
		editor.remove(Constants.PREF_AUTH_TOKEN);
		editor.remove(Constants.PREF_BLOG_ID);
		editor.remove(Constants.PREF_BLOG_NAME);
		editor.commit();
		Intent intent = new Intent(PostListActivity.this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		String postId = this.posts.get(position).getId().toString();
		String title = this.posts.get(position).getTitle();
		Log.v(TAG, "postId: " + postId + " selected '" + title + "'");

		Intent i = new Intent(getApplicationContext(), PostDisplayActivity.class);
		i.putExtra(Constants.POST_ID_KEY, postId);
		startActivity(i);

		super.onListItemClick(l, v, position, id);
	}

	/**
	 * Gets the auth token.
	 *
	 * @return the auth token
	 */
	boolean getAuthToken()
	{
		settings = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);
		authToken = settings.getString(Constants.PREF_AUTH_TOKEN, "");
		if (!authToken.isEmpty())
		{
			credential.setAccessToken(authToken);
			Log.v(TAG, "authToken exists");
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			// removeBlogChosen();
			finish();
			return true;
		}
		return false;
	}

	// private void removeBlogChosen()
	// {
	// SharedPreferences.Editor editor = settings.edit();
	// editor.remove(BlogListActivity.PREF_BLOG_NAME);
	// editor.remove(EditorActivity.PREF_BLOG_ID);
	// editor.commit();
	// }
}
