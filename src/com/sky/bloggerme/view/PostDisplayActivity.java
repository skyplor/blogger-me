package com.sky.bloggerme.view;

import java.util.Iterator;
import java.util.logging.Level;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import com.sky.bloggerme.ClientCredentials;
import com.sky.bloggerme.R;
import com.sky.bloggerme.util.Constants;

/**
 * The Class PostDisplayActivity.
 */
public class PostDisplayActivity extends Activity
{

	/** The tag. */
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
	// GoogleAccountCredential credential;
	GoogleCredential credential = new GoogleCredential();

	/** Selected blog name. */
	// String blogTitle;

	/** blgtitle: The TextView component which displays the blog title;. */
	// TextView blgtitle;

	/** postsBtn: The Button component which displays the blog posts;. */
	// Button postsBtn;

	/** editBtn: The Button component which allows user to edit the post;. */
	// Button editBtn;

	/** postTitle: The TextView component which displays the post's title;. */
	TextView postTitle;

	/** content: The WebView component which displays the post's content;. */
	WebView content;

	/** postResult: The post object;. */
	Post postResult;

	/** postID: The ID of the post object;. */
	String postId;

	/** The Constant REQUEST_ACCOUNT_PICKER. */
	static final int REQUEST_ACCOUNT_PICKER = 1;

	/** The Constant REQUEST_AUTHORIZATION. */
	static final int REQUEST_AUTHORIZATION = 2;

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
		setContentView(R.layout.activity_post_display);
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		settings = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);

		postTitle = ((TextView) findViewById(R.id.title));
		content = ((WebView) findViewById(R.id.content));
		/*getBlogTitle();
		postsBtn = (Button) findViewById(R.id.postsDisplayButton);
		editBtn = (Button) findViewById(R.id.postEditButton);

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
		});*/

		/*editBtn.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
				intent.putExtra("Title", postResult.getTitle());
				intent.putExtra("Content", postResult.getContent());
				String labels = join(postResult.getLabels(), ", ");
				intent.putExtra("Labels", labels);
				intent.putExtra("editExisting", true);

				Constants.POST_ID = postId;

				startActivity(intent);

			}
		});*/
		// credential = GoogleAccountCredential.usingOAuth2(this, BloggerScopes.BLOGGER);

		// credential.setSelectedAccountName(AccountManager.KEY_ACCOUNT_NAME);
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			postId = extras.getString(Constants.POST_ID_KEY);
			Log.v(TAG, "postId: " + postId);

			// We shouldn't need ClientCredentials... but I can't figure out why it isn't binding.
			ClientCredentials.errorIfNotSpecified();

			service = new com.google.api.services.blogger.Blogger.Builder(transport, jsonFactory, credential).setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY)).setApplicationName("Blogger-me/1.0").build();
			// service = getBloggerService(credential);
			// Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
			Log.d(TAG, "After getting service");
			new AsyncLoadPost(this).execute(postId);
		}

		initDrawer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_post_display, menu);
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
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;

			case R.id.menu_edit:
				Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
				intent.putExtra("Title", postResult.getTitle());
				intent.putExtra("Content", postResult.getContent());
				String labels = join(postResult.getLabels(), ", ");
				intent.putExtra("Labels", labels);
				intent.putExtra("editExisting", true);

				Constants.POST_ID = postId;

				startActivity(intent);
				return true;

			case R.id.menu_logout:
				doLogout();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// @Override
	// protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	// {
	// switch (requestCode)
	// {
	// case REQUEST_ACCOUNT_PICKER:
	// if (resultCode == RESULT_OK && data != null && data.getExtras() != null)
	// {
	// String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
	// if (accountName != null)
	// {
	// credential.setSelectedAccountName(accountName);
	// service = getBloggerService(credential);
	// // startCameraIntent();
	// }
	// }
	// break;
	// case REQUEST_AUTHORIZATION:
	// if (resultCode == Activity.RESULT_OK)
	// {
	// // saveFileToDrive();
	// }
	// else
	// {
	// startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	// }
	// break;
	// }
	// }

	/**
	 * Gets the blogger service.
	 * 
	 * @param credential
	 *            the credential
	 * @return the blogger service
	 */
	private Blogger getBloggerService(GoogleAccountCredential credential)
	{
		// TODO Auto-generated method stub
		return new Blogger.Builder(AndroidHttp.newCompatibleTransport(), jsonFactory, credential).setApplicationName("Google-BloggerAndroidSample/1.0").build();
	}

	/**
	 * Display.
	 * 
	 * @param result
	 *            the result
	 */
	public void display(Post result)
	{
		postResult = result;
		Log.v(TAG, "Title: " + result.getTitle());
		Log.v(TAG, "Content: " + result.getContent());
		postTitle.setText(result.getTitle());
		content.loadDataWithBaseURL(null, result.getContent(), "text/html", "utf-8", null);

	}

	/**
	 * Gets the blog title.
	 * 
	 * @return the blog title
	 */
	/*
	 * private void getBlogTitle() { blogTitle = settings.getString(Constants.PREF_BLOG_NAME, ""); if (!blogTitle.isEmpty()) { blgtitle = (TextView) findViewById(R.id.blogTitle); blgtitle.setText(blogTitle); }
	 * 
	 * }
	 */

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
		Intent intent = new Intent(PostDisplayActivity.this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	/**
	 * Join.
	 * 
	 * @param s
	 *            the string to split
	 * @param delimiter
	 *            the delimiter
	 * @return the string
	 */
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

	/**
	 * Request auth.
	 * 
	 * @param e
	 *            the e
	 * @return the post
	 */
	public Post requestAuth(UserRecoverableAuthIOException e)
	{
		startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
		return postResult;
	}

	/**
	 * initialise the left drawer
	 */
	public void initDrawer()
	{
		mTitle = mDrawerTitle = getTitle();
		drawerPages = getResources().getStringArray(R.array.drawer_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.post_drawer);

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

//		selectItem(Constants.DRAWERLIST.DRAFTS.getDrawerList());

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
				Intent editorIntent = new Intent(PostDisplayActivity.this, EditorActivity.class);
				startActivity(editorIntent);
				finish();
				break;
			case 1:
				// Blogs Activity
				Intent blogIntent = new Intent(PostDisplayActivity.this, BlogListActivity.class);
				blogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(blogIntent);
				finish();
				break;
			case 2:
				// Posts Activity
				Intent postIntent = new Intent(PostDisplayActivity.this, PostListActivity.class);
				postIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(postIntent);
				finish();
				break;
			case 3:
				// Drafts Activity
				Intent draftIntent = new Intent(PostDisplayActivity.this, DraftListActivity.class);
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
}
