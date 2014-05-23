package com.sky.bloggerme;

import java.util.Iterator;
import java.util.logging.Level;

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
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.model.Post;
import com.sky.bloggerme.util.Constants;

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

	/** Shared Preferences editor for editing auth credentials. */
	SharedPreferences.Editor editor;

	/** HTTP rewriter responsible for managing lifetime of oauth2 credentials. */
//	GoogleAccountCredential credential;
	GoogleCredential credential = new GoogleCredential();

	/** Selected blog name */
	String blogTitle;

	/**
	 * blgtitle: The TextView component which displays the blog title;
	 */
	TextView blgtitle;

	/**
	 * postsBtn: The Button component which displays the blog posts;
	 */
	Button postsBtn;

	/**
	 * editBtn: The Button component which allows user to edit the post;
	 */
	Button editBtn;
	/**
	 * postTitle: The TextView component which displays the post's title;
	 */
	TextView postTitle;

	/**
	 * content: The WebView component which displays the post's content;
	 */
	WebView content;

	/**
	 * postResult: The post object;
	 */
	Post postResult;

	/**
	 * postID: The ID of the post object;
	 */
	String postId;

	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_display);
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		settings = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);

		getBlogTitle();
		postsBtn = (Button) findViewById(R.id.postsDisplayButton);
		editBtn = (Button) findViewById(R.id.postEditButton);
		postTitle = ((TextView) findViewById(R.id.title));
		content = ((WebView) findViewById(R.id.content));

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
		
		editBtn.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
				intent.putExtra("Title", postResult.getTitle());
				intent.putExtra("Content", postResult.getContent());
				String labels = join(postResult.getLabels(), ", ");
				intent.putExtra("Labels", labels);

				Constants.POST_ID = postId;

				startActivity(intent);
				
			}
		});
//		credential = GoogleAccountCredential.usingOAuth2(this, BloggerScopes.BLOGGER);

		// credential.setSelectedAccountName(AccountManager.KEY_ACCOUNT_NAME);
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			postId = extras.getString(Constants.POST_ID_KEY);
			Log.v(TAG, "postId: " + postId);

			// We shouldn't need ClientCredentials... but I can't figure out why it isn't binding.
			ClientCredentials.errorIfNotSpecified();

			service = new com.google.api.services.blogger.Blogger.Builder(transport, jsonFactory, credential).setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY)).setApplicationName("Google-BloggerAndroidSample/1.0").build();
//			service = getBloggerService(credential);
//			Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
			Log.d(TAG, "After getting service");
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

			case R.id.menu_edit:
				Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
				intent.putExtra("Title", postResult.getTitle());
				intent.putExtra("Content", postResult.getContent());
				String labels = join(postResult.getLabels(), ", ");
				intent.putExtra("Labels", labels);

				Constants.POST_ID = postId;

				startActivity(intent);
				return true;

			case R.id.menu_logout:
				doLogout();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

//	@Override
//	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
//	{
//		switch (requestCode)
//		{
//			case REQUEST_ACCOUNT_PICKER:
//				if (resultCode == RESULT_OK && data != null && data.getExtras() != null)
//				{
//					String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
//					if (accountName != null)
//					{
//						credential.setSelectedAccountName(accountName);
//						service = getBloggerService(credential);
//						// startCameraIntent();
//					}
//				}
//				break;
//			case REQUEST_AUTHORIZATION:
//				if (resultCode == Activity.RESULT_OK)
//				{
//					// saveFileToDrive();
//				}
//				else
//				{
//					startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
//				}
//				break;
//		}
//	}

	private Blogger getBloggerService(GoogleAccountCredential credential)
	{
		// TODO Auto-generated method stub
		return new Blogger.Builder(AndroidHttp.newCompatibleTransport(), jsonFactory, credential).setApplicationName("Google-BloggerAndroidSample/1.0").build();
	}

	public void display(Post result)
	{
		postResult = result;
		Log.v(TAG, "Title: " + result.getTitle());
		Log.v(TAG, "Content: " + result.getContent());
		postTitle.setText(result.getTitle());
		content.loadDataWithBaseURL(null, result.getContent(), "text/html", "utf-8", null);

	}

	private void getBlogTitle()
	{
		blogTitle = settings.getString(Constants.PREF_BLOG_NAME, "");
		if (!blogTitle.isEmpty())
		{
			blgtitle = (TextView) findViewById(R.id.blogTitle);
			blgtitle.setText(blogTitle);
		}

	}

	private void doLogout()
	{
		editor = settings.edit();
		editor.remove(Constants.PREF_ACCOUNT_NAME);
		editor.remove(Constants.PREF_AUTH_TOKEN);
		editor.remove(Constants.PREF_BLOG_ID);
		editor.remove(Constants.PREF_BLOG_NAME);
		editor.commit();
		Intent intent = new Intent(PostDisplayActivity.this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	public static String join(Iterable<? extends CharSequence> s, String delimiter)
	{
		Iterator<? extends CharSequence> iter = s.iterator();

		if (!iter.hasNext())
		{
			return "";
		}

		StringBuilder buffer = new StringBuilder(iter.next());

		while (iter.hasNext())
		{
			buffer.append(delimiter).append(iter.next());
		}

		return buffer.toString();
	}

	public Post requestAuth(UserRecoverableAuthIOException e)
	{
		startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
		return postResult;
	}

}
