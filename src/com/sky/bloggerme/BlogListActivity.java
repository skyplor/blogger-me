package com.sky.bloggerme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.extensions.android3.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.blogger.model.Blog;
import com.sky.bloggerme.util.Constants;

/**
 * The Class BlogListActivity.
 */
public class BlogListActivity extends ListActivity
{
	
	/** The Constant TAG. */
	private static final String TAG = "BlogListActivity";

	// Configure the Java API Client for Installed Native App
	/** The http transport. */
	HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

	/** The JSON factory to use to marshall data onto the wire. */
	final JsonFactory JSON_FACTORY = new AndroidJsonFactory();

	/** The facade object for accessing Blogger API v3. */
	com.google.api.services.blogger.Blogger service;

	/** Shared Preferences for storing auth credentials. */
	SharedPreferences settings;

	/** Shared Preferences editor for editing auth credentials. */
	SharedPreferences.Editor editor;

	/** Selected account name we are authorizing as. */
	String accountName;

	/** Account Manager to request auth from for Google Accounts. */
	GoogleAccountManager accountManager;

	/** HTTP rewriter responsible for managing lifetime of oauth2 credentials. */
	GoogleCredential credential = new GoogleCredential();

	/** The auth token. */
	String authToken;

	/** Whether we have received a 401. Used to initiate re-authorising the authToken. */
	private boolean received401;

	// private static boolean accountChosen = false;

	/** The blogs. */
	private List<Blog> blogs;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blog_list);

		settings = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);
		removeBlogChosen();
		// gotAccount();
		accountManager = new GoogleAccountManager(this);
		if (getAuthToken())
		{
			// Construct the Blogger API access facade object.
			service = new com.google.api.services.blogger.Blogger.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Blogger-me/1.0").build();
			Log.v(TAG, "Constructed the Blogger API access facade object.");
			new AsyncLoadBlogList(this).execute();
			received401 = false;
		}
		else
		{
			Log.v(TAG, "Unable to obtain authentication token. Please login again.");
			Intent intent = new Intent(BlogListActivity.this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}

	}

	/**
	 * Got account.
	 */
	void gotAccount()
	{
		Log.v(TAG, "Retrieving the account for " + accountName);
		Account account = accountManager.getAccountByName(accountName);
		if (account == null)
		{
			Log.v(TAG, "account was null, go back to Login activity");
			// chooseAccount();
			doLogout();
			return;
		}
		if (credential.getAccessToken() != null)
		{
			Log.v(TAG, "We have an AccessToken");
			setToast("We have an AccessToken");
			onAuthToken();
			// accountChosen = true;
			return;
		}
		Log.v(TAG, "We have an account, but no stored Access Token. Requesting from the AccountManager");
		accountManager.getAccountManager().getAuthToken(account, Constants.AUTH_TOKEN_TYPE, null, true, new AccountManagerCallback<Bundle>()
		{

			public void run(AccountManagerFuture<Bundle> future)
			{
				try
				{
					Bundle bundle = future.getResult();
					if (bundle.containsKey(AccountManager.KEY_INTENT))
					{
						Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
						intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
						Log.v(TAG, "We need AccountManager to talk to the user. Starting Activity.");
						startActivityForResult(intent, Constants.REQUEST_AUTHENTICATE);
						// accountChosen = true;
					}
					else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN))
					{
						Log.v(TAG, "AccountManager handed us a AuthToken, storing for future reference");
						setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
						onAuthToken();
						// accountChosen = true;
					}
				}
				catch (Exception e)
				{
					Log.e(TAG, e.getMessage(), e);
					// accountChosen = false;
				}
			}
		}, null);
	}

	//
	// private void chooseAccount()
	// {
	// Log.v(TAG, "Asking the AccountManager to find us an account to auth as");
	// accountManager.getAccountManager().getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE, EditorActivity.AUTH_TOKEN_TYPE, null, BlogListActivity.this, null, null, new AccountManagerCallback<Bundle>()
	// {
	// public void run(AccountManagerFuture<Bundle> future)
	// {
	// Bundle bundle;
	// try
	// {
	// Log.v(TAG, "Requesting result");
	// bundle = future.getResult();
	// Log.v(TAG, "Retrieving Account Name");
	// setAccountName(bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
	// Log.v(TAG, "Retrieving Auth Token");
	// setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
	// Log.v(TAG, "Stored for future reference");
	// onAuthToken();
	// }
	// catch (OperationCanceledException e)
	// {
	// // user canceled
	// accountChosen = false;
	// }
	// catch (AuthenticatorException e)
	// {
	// Log.e(TAG, e.getMessage(), e);
	// accountChosen = false;
	// }
	// catch (IOException e)
	// {
	// Log.e(TAG, e.getMessage(), e);
	// accountChosen = false;
	// }
	// }
	// }, null);
	// }

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent data)
	// {
	// Log.v(TAG, "Returning from another Activity");
	// super.onActivityResult(requestCode, resultCode, data);
	// switch (requestCode)
	// {
	// case EditorActivity.REQUEST_AUTHENTICATE:
	// Log.v(TAG, "request code is REQUEST_AUTHENTICATE");
	// if (resultCode == RESULT_OK)
	// {
	// Log.v(TAG, "Result was RESULT_OK");
	// gotAccount();
	// }
	// else
	// {
	// Log.v(TAG, "Result was NOT RESULT_OK");
	// chooseAccount();
	// }
	// break;
	// }
	// }

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		removeBlogChosen();
		// if (getAuthToken())
		// {
		// // Construct the Blogger API access facade object.
		// service = new com.google.api.services.blogger.Blogger.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY)).build();
		// Log.v(TAG, "Constructed the Blogger API access facade object.");
		// new AsyncLoadBlogList(this).execute();
		// }
		// else
		// {
		// Log.v(TAG, "Unable to obtain authentication token. Please login again.");
		// }
	}

	// void setAccountName(String accountName)
	// {
	// editor = settings.edit();
	// editor.putString(EditorActivity.PREF_ACCOUNT_NAME, accountName);
	// editor.commit();
	// this.accountName = accountName;
	// Log.v(TAG, "Stored accountName: " + accountName);
	// }
	//
	/**
	 * Sets the auth token.
	 *
	 * @param authToken the new auth token
	 */
	void setAuthToken(String authToken)
	{
		editor = settings.edit();
		editor.putString(Constants.PREF_AUTH_TOKEN, authToken);
		editor.commit();
		credential.setAccessToken(authToken);
		Log.v(TAG, "Stored authToken");
	}

	/**
	 * On auth token.
	 */
	void onAuthToken()
	{
		Log.v(TAG, "In on Authentication Token");
	}

	/**
	 * Sets the model.
	 *
	 * @param result the new model
	 */
	public void setModel(List<Blog> result)
	{
		this.blogs = result;
		List<String> names = new ArrayList<String>(result.size());
		List<String> posts = new ArrayList<String>(result.size());
		for (Blog blog : blogs)
		{
			names.add(blog.getName());
			posts.add(blog.getPosts().getTotalItems().toString());
		}
		this.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names));

		// create the grid item mapping
		// String[] from = new String[] { "line1", "line3" };
		//
		// // prepare the list of all records
		// List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
		// for (int i = 0; i < 2; i++)
		// {
		// HashMap<String, String> map = new HashMap<String, String>();
		// map.put("line1", names.get(i));
		// map.put("line3", posts.get(i));
		// fillMaps.add(map);
		// }
		//
		// SimpleAdapter notes = new SimpleAdapter(this, fillMaps, R.layout.row, from, new int[] { R.id.text1, R.id.text3 });
		// setListAdapter(notes);
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
		Intent intent = new Intent(BlogListActivity.this, LoginActivity.class);
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
		String blogId = Long.toString(this.blogs.get(position).getId());
		String name = this.blogs.get(position).getName();
		Log.v(TAG, "blogId: " + blogId + " selected '" + name + "'");

		setBlogDetails(blogId, name);
		Intent i = new Intent(getApplicationContext(), PostListActivity.class);
		startActivity(i);

		super.onListItemClick(l, v, position, id);
	}

	/**
	 * Sets the blog details.
	 *
	 * @param blogId the blog id
	 * @param name the name
	 */
	private void setBlogDetails(String blogId, String name)
	{
		SharedPreferences settings = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Constants.PREF_BLOG_ID, blogId);
		editor.putString(Constants.PREF_BLOG_NAME, name);
		editor.commit();
		Log.v(TAG, "BlogDetails added into sharedpreferences.");
	}

	/**
	 * Gets the auth token.
	 *
	 * @return the auth token
	 */
	boolean getAuthToken()
	{
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
			finish();
			return true;
		}
		return false;
	}

	/**
	 * Removes the blog chosen.
	 */
	private void removeBlogChosen()
	{
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(Constants.PREF_BLOG_NAME);
		editor.remove(Constants.PREF_BLOG_ID);
		editor.commit();
	}

	/**
	 * Handle an IO exception encountered by the background AsyncTask. It may be because the stored AuthToken is stale.
	 * 
	 * @param e
	 *            The exception caught in the AsyncTask.
	 * @return Returns if we believe we have a valid AuthToken (for re-try purposes).
	 */
	void handleGoogleException(IOException e)
	{
//		setToast(e.getMessage());
		if (e instanceof GoogleJsonResponseException)
		{
			GoogleJsonResponseException exception = (GoogleJsonResponseException) e;
			if (exception.getStatusCode() == 401 && !received401)
			{
				Log.v(TAG, "Invalidating stored AuthToken due to received 401.");
				Log.v(TAG, "Remembering seen 401, to prevent re-auth loop");
				received401 = true;
				Log.v(TAG, "Invalidating AuthToken in AccountManager");
				accountManager.invalidateAuthToken(credential.getAccessToken());
				Log.v(TAG, "Deleting AccessToken from Credential");
				credential.setAccessToken(null);
				Log.v(TAG, "Removing AuthToken from private SharedPreferences");
				SharedPreferences.Editor editor2 = settings.edit();
				editor2.remove(Constants.PREF_AUTH_TOKEN);
				editor2.commit();
				Log.v(TAG, "Initiating authToken request from AccountManager");
//				setToast(e.getMessage());
				gotAccount();
				return;
			}
		}
		Log.e(TAG, e.getMessage(), e);
	}

	/**
	 * Sets the toast.
	 *
	 * @param string the new toast
	 */
	public void setToast(final String string)
	{
		// TODO Auto-generated method stub
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(BlogListActivity.this, string, Toast.LENGTH_SHORT).show();
			}
		});
		// Toast.makeText(this, string, Toast.LENGTH_LONG).show();
	}

}
