package com.sky.bloggerapp;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.extensions.android3.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.blogger.model.Post;
import com.sky.bloggerapp.util.Constants;

public class PostDisplayActivity extends Activity
{

	private static String TAG = "PostDisplay";

	/** Logging level for HTTP requests/responses. */
	private static final Level LOGGING_LEVEL = Level.ALL;

	/** Choose the right HttpTransport depending on Android version. */
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();

	/** The JSON factory to use to marshall data onto the wire. */
	final JsonFactory jsonFactory = new AndroidJsonFactory();

	/** The facade object for accessing Blogger API v3. */
	com.google.api.services.blogger.Blogger service;

	/** Shared Preferences for storing auth credentials. */
	SharedPreferences settings;

	/** HTTP rewriter responsible for managing lifetime of oauth2 credentials. */
	GoogleCredential credential = new GoogleCredential();

	/** Selected blog name */
	String blogTitle;

	/**
	 * title: The TextView component which displays the blog title; 
	 */
	TextView title;
	
	/**
	 * postsBtn: The Button component which displays the blog posts; 
	 */
	Button postsBtn;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_display);
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		settings = getSharedPreferences("com.sky.bloggerapp", MODE_PRIVATE);
		
		getBlogTitle();
		postsBtn = (Button) findViewById(R.id.postsDisplayButton);
		postsBtn.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(PostDisplayActivity.this, PostListActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			String postId = extras.getString(Constants.POST_ID_KEY);
			Log.v(TAG, "postId: " + postId);

			// We shouldn't need ClientCredentials... but I can't figure out why it isn't binding.
			ClientCredentials.errorIfNotSpecified();

			service = new com.google.api.services.blogger.Blogger.Builder(transport, jsonFactory, credential).setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY)).build();
			Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
			new AsyncLoadPost(this).execute(postId);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_post_display, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void display(Post result)
	{
		Log.v(TAG, "Title: " + result.getTitle());
		Log.v(TAG, "Content: " + result.getContent());
		((TextView) findViewById(R.id.title)).setText(result.getTitle());
		((WebView) findViewById(R.id.content)).loadDataWithBaseURL(null, result.getContent(), "text/html", "utf-8", null);

	}
	
	private void getBlogTitle()
	{
		blogTitle = settings.getString(Constants.PREF_BLOG_NAME, "");
		if(!blogTitle.isEmpty())
		{
			title = (TextView) findViewById(R.id.blogTitle);
			title.setText(blogTitle);
		}
		
	}

}
