package com.sky.bloggerme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.extensions.android3.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.model.Post;
import com.sky.bloggerme.util.Constants;
import com.sky.bloggerme.util.DroidWriterEditText;

/**
 * Sample for Blogger API on Android. It shows how to create a post.
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
public class EditorActivity extends Activity
{

	/** Logging tag. */
	private final String TAG = "CreatePost";

	/** Choose the right HttpTransport depending on Android version. */
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();

	/** The JSON factory to use to marshall data onto the wire. */
	final JsonFactory jsonFactory = new AndroidJsonFactory();

	/** Facade object for Blogger API v3. */
	public Blogger service;

	/** Account Manager to request auth from for Google Accounts. */
	GoogleAccountManager accountManager;

	/** Shared Preferences for storing auth credentials. */
	SharedPreferences settings;

	/** Shared Preferences editor for editing auth credentials. */
	SharedPreferences.Editor editor;

	/** Selected account name we are authorizing as. */
	String accountName;

	/** Selected blog name */
	String blogTitle;

	/** HTTP rewriter responsible for managing lifetime of oauth2 credentials. */
	GoogleCredential credential = new GoogleCredential();

	/**
	 * Create Post button. Used to prevent user from attempting to create posts until after they are auth'd. View Posts button. Used to prevent user from attempting other posts until after they are auth'd.
	 */
	Button createPostButton, viewPostsButton;

	/**
	 * Rich Text buttons. Used to allow user to format their text.
	 */
	private ToggleButton boldToggle;
	private ToggleButton italicsToggle;
	// private ToggleButton underlineToggle;

	/**
	 * EditText Control with rich text implementation.
	 */
	DroidWriterEditText postContent;

	/**
	 * EditText Control for post's title.
	 */
	EditText postTitle;

	/**
	 * title: The TextView component which displays the blog title;
	 */
	TextView title;

	/**
	 * myMultiAutoCompleteTextView: The MultiAutoCompleteTextView component which displays the blog title;
	 */
	MultiAutoCompleteTextView labelsMultiAutoComplete;

	/** Whether we have received a 401. Used to initiate re-authorising the authToken. */
	private boolean received401;

	private static boolean blogChosen = false, accountChosen = false;

	private String[] labels;

	/**
	 * create: Whether we are creating or updating a post;
	 */
	Boolean create = true;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_update_post);

		// Initialization of the buttons
		init();

		Log.v(TAG, "Building the Blogger API v3 service facade");
		service = new com.google.api.services.blogger.Blogger.Builder(transport, jsonFactory, credential).setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY)).setApplicationName("Blogger-me/1.0").build();
		Log.v(TAG, "Getting the private SharedPreferences instance");
		settings = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);

		getBlogTitle();

		Log.v(TAG, "Retrieving the account name from settings");
		accountName = settings.getString(Constants.PREF_ACCOUNT_NAME, null);
		Log.v(TAG, "accountName: " + accountName);
		credential.setAccessToken(settings.getString(Constants.PREF_AUTH_TOKEN, null));
		Logger.getLogger("com.google.api.client").setLevel(Constants.LOGGING_LEVEL);
		accountManager = new GoogleAccountManager(this);
		gotAccount();
		gotBlog();


		if (accountChosen && !blogChosen)
		{
			startBlogsListActivity();
		}
		else if (accountChosen && blogChosen)
		{

			labelsMultiAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

			new AsyncLoadLabels(this).execute();
			// do
			// {
			// new AsyncLoadLabels(this).execute();
			// }
			// while (labelsMultiAutoComplete.getAdapter().isEmpty());
			// {
			// long t0, t1;
			// t0 = System.currentTimeMillis();
			// do
			// {
			// t1 = System.currentTimeMillis();
			// }
			// while (t1 - t0 < 3000);
			//
			// }
		}
	}

	private void init()
	{
		postContent = (DroidWriterEditText) findViewById(R.id.post_body);

		boldToggle = (ToggleButton) findViewById(R.id.BoldButton);
		italicsToggle = (ToggleButton) findViewById(R.id.ItalicsButton);
		// underlineToggle = (ToggleButton) findViewById(R.id.UnderlineButton);
		postContent.setBoldToggleButton(boldToggle);
		postContent.setItalicsToggleButton(italicsToggle);
		// postContent.setUnderlineToggleButton(underlineToggle);

		postTitle = (EditText) findViewById(R.id.post_title);
		labelsMultiAutoComplete = (MultiAutoCompleteTextView) findViewById(R.id.post_labels);

		Log.v(TAG, "Capturing publishbutton");
		createPostButton = (Button) findViewById(R.id.publishbutton);
		Log.v(TAG, "Setting publishbutton's OnClickListener");
		createPostButton.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				String title = postTitle.getText().toString();
				String content = postContent.getTextHTML();
				List<String> labels_list = new ArrayList<String>();
				StringTokenizer st = new StringTokenizer(labelsMultiAutoComplete.getText().toString(), ",");
				while (st.hasMoreTokens())
				{
					String labelToken = st.nextToken().trim();
					labels_list.add(labelToken);
				}

				Post post = new Post().setTitle(title).setContent(content).setLabels(labels_list);
				(new AsyncCreatePost(EditorActivity.this, create)).execute(post);
			}
		});

		Log.v(TAG, "Disabling publishbutton");
		createPostButton.setEnabled(false);

		Log.v(TAG, "Capturing postsbutton");
		viewPostsButton = (Button) findViewById(R.id.postsbutton);
		Log.v(TAG, "Setting postsbutton's OnClickListener");
		viewPostsButton.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(EditorActivity.this, PostListActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

		Log.v(TAG, "Disabling postsbutton");
		viewPostsButton.setEnabled(false);

		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			updatePostDetails(extras);
		}
		else
		{
			create = true;
		}
	}

	private void updatePostDetails(Bundle extras)
	{
		// TODO Auto-generated method stub
		create = false;
		String title = extras.getString("Title");
		String content = extras.getString("Content");
		String labels = extras.getString("Labels");
		postTitle.setText(title);
		postContent.setTextHTML(content);
		if (labels != null)
		{
			Log.d(TAG, "Labels: " + labels);
			labelsMultiAutoComplete.setText(labels);
		}
	}

	void gotAccount()
	{
		Log.v(TAG, "Retrieving the account for " + accountName);
		Account account = accountManager.getAccountByName(accountName);
		if (account == null)
		{
			Log.v(TAG, "account was null, forcing user to choose account");
			// chooseAccount();
			goLoginActivity();
			return;
		}
		if (credential.getAccessToken() != null)
		{
			Log.v(TAG, "We have an AccessToken");
			onAuthToken();
			accountChosen = true;
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
						// Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
						// intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
						// Log.v(TAG, "We need AccountManager to talk to the user. Starting Activity.");
						// startActivityForResult(intent, REQUEST_AUTHENTICATE);
						// accountChosen = true;
						goLoginActivity();
					}
					else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN))
					{
						Log.v(TAG, "AccountManager handed us a AuthToken, storing for future reference");
						setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
						onAuthToken();
						accountChosen = true;
					}
				}
				catch (Exception e)
				{
					Log.e(TAG, e.getMessage(), e);
					accountChosen = false;
				}
			}
		}, null);
	}

	private void goLoginActivity()
	{
		Intent login = new Intent(EditorActivity.this, LoginActivity.class);
		login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(login);
		finish();
	}

	private void getBlogTitle()
	{
		blogTitle = settings.getString(Constants.PREF_BLOG_NAME, "");
		if (!blogTitle.isEmpty())
		{
			title = (TextView) findViewById(R.id.blogTitle);
			title.setText(blogTitle);
		}

	}

	private void chooseAccount()
	{
		Log.v(TAG, "Asking the AccountManager to find us an account to auth as");
		accountManager.getAccountManager().getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE, Constants.AUTH_TOKEN_TYPE, null, EditorActivity.this, null, null, new AccountManagerCallback<Bundle>()
		{
			public void run(AccountManagerFuture<Bundle> future)
			{
				Bundle bundle;
				try
				{
					Log.v(TAG, "Requesting result");
					bundle = future.getResult();
					Log.v(TAG, "Retrieving Account Name");
					setAccountName(bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
					Log.v(TAG, "Retrieving Auth Token");
					setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
					Log.v(TAG, "Stored for future reference");
					onAuthToken();
				}
				catch (OperationCanceledException e)
				{
					// user canceled
					accountChosen = false;
				}
				catch (AuthenticatorException e)
				{
					Log.e(TAG, e.getMessage(), e);
					accountChosen = false;
				}
				catch (IOException e)
				{
					Log.e(TAG, e.getMessage(), e);
					accountChosen = false;
				}
			}
		}, null);
	}

	void gotBlog()
	{
		Log.v(TAG, "Retrieving the blog for " + accountName);
		Constants.BLOG_ID = settings.getString(Constants.PREF_BLOG_ID, null);
		if (Constants.BLOG_ID == null || Constants.BLOG_ID.isEmpty())
		{
			blogChosen = false;
		}
		else
		{
			blogChosen = true;
		}
	}

	void startBlogsListActivity()
	{
		Intent intent = new Intent(EditorActivity.this, BlogListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	void setAccountName(String accountName)
	{
		editor = settings.edit();
		editor.putString(Constants.PREF_ACCOUNT_NAME, accountName);
		editor.commit();
		this.accountName = accountName;
		Log.v(TAG, "Stored accountName: " + accountName);
	}

	void setAuthToken(String authToken)
	{
		editor = settings.edit();
		editor.putString(Constants.PREF_AUTH_TOKEN, authToken);
		editor.commit();
		credential.setAccessToken(authToken);
		Log.v(TAG, "Stored authToken");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.v(TAG, "Returning from another Activity");
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case Constants.REQUEST_AUTHENTICATE:
				Log.v(TAG, "request code is REQUEST_AUTHENTICATE");
				if (resultCode == RESULT_OK)
				{
					Log.v(TAG, "Result was RESULT_OK");
					gotAccount();
					if (accountChosen && !blogChosen)
					{
						startBlogsListActivity();
					}

				}
				else
				{
					Log.v(TAG, "Result was NOT RESULT_OK");
					// chooseAccount();
					goLoginActivity();
				}
				break;
		}
	}

	void onAuthToken()
	{
		Log.v(TAG, "Enabling publishbutton");
		createPostButton.setEnabled(true);

		Log.v(TAG, "Enabling postsbutton");
		viewPostsButton.setEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_create_post, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_accounts:
				chooseAccount();
				break;
			case R.id.menu_logout:
				doLogout();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void doLogout()
	{
		editor = settings.edit();
		editor.remove(Constants.PREF_ACCOUNT_NAME);
		editor.remove(Constants.PREF_AUTH_TOKEN);
		editor.remove(Constants.PREF_BLOG_ID);
		editor.remove(Constants.PREF_BLOG_NAME);
		editor.commit();
		Intent intent = new Intent(EditorActivity.this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	public void display(Post result)
	{
		Log.v(TAG, "Displaying " + result.getTitle());
		// ((EditText) findViewById(R.id.post_title)).setText(result.getTitle());
		// ((EditText) findViewById(R.id.post_body)).setText(result.getContent());

	}

	void onRequestCompleted(Post result)
	{
		Log.v(TAG, "Request completed, throwing away 401 state");
		received401 = false;
		if (result != null)
		{
			Log.v(TAG, "Post result not null");
			if (result.getId() != null)
			{
				String postId = result.getId().toString();
				String title = result.getTitle();
				Log.v(TAG, "postId: " + postId + " selected '" + title + "'");

				Intent i = new Intent(getApplicationContext(), PostDisplayActivity.class);
				i.putExtra(Constants.POST_ID_KEY, postId);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();
			}
		}
	}

	// void onRequestCompleted(String result)
	// {
	// Log.v(TAG, "Request completed, throwing away 401 state");
	// received401 = false;
	// // if (!result.isEmpty())
	// // {
	// Log.v(TAG, "Error retrieving Labels.");
	// // if (result.getId() != null)
	// // {
	// // String postId = result.getId().toString();
	// // String title = result.getTitle();
	// // Log.v(TAG, "postId: " + postId + " selected '" + title + "'");
	// //
	// // Intent i = new Intent(getApplicationContext(), PostDisplayActivity.class);
	// // i.putExtra(Constants.POST_ID_KEY, postId);
	// // i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	// // startActivity(i);
	// // finish();
	// // }
	// // }
	// }

	// @Override
	// public void onResume()
	// {
	// super.onResume();
	// if (accountChosen && !blogChosen)
	// {
	// startBlogsListActivity();
	// }
	// }

	/**
	 * Handle an IO exception encountered by the background AsyncTask. It may be because the stored AuthToken is stale.
	 * 
	 * @param e
	 *            The exception caught in the AsyncTask.
	 * @return Returns if we believe we have a valid AuthToken (for re-try purposes).
	 */
	void handleGoogleException(IOException e)
	{
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
				gotAccount();
				return;
			}
		}
		Log.e(TAG, e.getMessage(), e);
	}

	public String getBlogID()
	{
		// TODO Auto-generated method stub
		return settings.getString(Constants.PREF_BLOG_ID, null);
	}

	public void setModel(List<String> result)
	{
		labels = new String[result.size()];
		// for (int i = 0; i < labels.length; i++)
		// {
		// for (String label : result)
		// {
		// labels[i] = label;
		// }
		// }
		result.toArray(labels);

		labelsMultiAutoComplete.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, labels));

	}
}
