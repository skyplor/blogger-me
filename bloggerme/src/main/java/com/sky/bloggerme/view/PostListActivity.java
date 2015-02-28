package com.sky.bloggerme.view;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.blogger.model.Post;
import com.sky.bloggerme.ClientCredentials;
import com.sky.bloggerme.R;
import com.sky.bloggerme.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    /** Logging level for HTTP requests/responses. */
    private static final Level LOGGING_LEVEL = Level.OFF;

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
	String authToken;// , blogTitle;

	/** The title. */
	// private TextView title;

	/** The posts. */
	private List<Post> posts;

	/** The blogs btn. */
	// Button newPostBtn, blogsBtn;

	/**
	 * Drawer variables
	 */
	private String[] drawerPages;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_list);

		settings = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);
		// ClientCredentials.errorIfNotSpecified();
		// getBlogTitle();
		//
		// newPostBtn = (Button) this.findViewById(R.id.createbutton);
		// newPostBtn.setOnClickListener(new OnClickListener()
		// {
		//
		// @Override
		// public void onClick(View v)
		// {
		// Intent intent = new Intent(PostListActivity.this, EditorActivity.class);
		// startActivity(intent);
		// }
		//
		// });

		// blogsBtn = (Button) this.findViewById(R.id.blogsbutton);
		// blogsBtn.setOnClickListener(new OnClickListener()
		// {
		//
		// @Override
		// public void onClick(View v)
		// {
		// Intent intent = new Intent(PostListActivity.this, BlogListActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// startActivity(intent);
		// finish();
		// }
		//
		// });

		if (getAuthToken())
		{
			service = new com.google.api.services.blogger.Blogger.Builder(transport, jsonFactory, credential).setGoogleClientRequestInitializer(new CommonGoogleClientRequestInitializer(ClientCredentials.KEY)).setApplicationName("Blogger-me/1.0").build();// null).setApplicationName("Google-BloggerAndroidSample/1.0").setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY)).build();
			Constants.BLOG_ID = getBlogID();
			Logger.getLogger("com.google.api.client").setLevel(Constants.LOGGING_LEVEL);
			new AsyncLoadPostList(this).execute();
		}
		else
		{
			Log.v(TAG, "Unable to obtain authentication token. Please login again.");
		}

		initDrawer();
	}

	/**
	 * Gets the blog title.
	 * 
	 * @return the blog title
	 */
	/*
	 * private void getBlogTitle() { blogTitle = settings.getString(Constants.PREF_BLOG_NAME, ""); if (!blogTitle.isEmpty()) { title = (TextView) findViewById(R.id.blogTitle); title.setText(blogTitle); }
	 * 
	 * }
	 */

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
	 * @param result
	 *            the new model
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_blog_list, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		switch (item.getItemId())
		{
			case R.id.menu_settings:
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				startActivity(settingsIntent);
				break;
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

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
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

	/**
	 * initialise the left drawer
	 */
	public void initDrawer()
	{
		mTitle = mDrawerTitle = getTitle();
		drawerPages = getResources().getStringArray(R.array.drawer_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.postslist_drawer);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerPages));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		)
		{
			public void onDrawerClosed(View view)
			{
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView)
			{
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		selectItem(Constants.DRAWERLIST.POSTS.getDrawerList());

	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			selectItem(position);
		}
	}

	/** Go to another activity */
	private void selectItem(int position)
	{
		// TODO: Change to the relevant activity on select
		switch (position)
		{
			case 0:
				// Editor Activity
				Intent editorIntent = new Intent(PostListActivity.this, EditorActivity.class);
				startActivity(editorIntent);
				finish();
				break;
			case 1:
				// Blogs Activity
				Intent blogIntent = new Intent(PostListActivity.this, BlogListActivity.class);
				blogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(blogIntent);
				finish();
				break;
			case 2:
				// Posts Activity

				break;
			case 3:
				// Drafts Activity
				Intent draftIntent = new Intent(PostListActivity.this, DraftListActivity.class);
				startActivity(draftIntent);
				finish();
				break;
			default:
				break;
		}
		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(drawerPages[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title)
	{
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		selectItem(Constants.DRAWERLIST.POSTS.getDrawerList());
	}
}
