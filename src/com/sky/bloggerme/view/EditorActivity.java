/*
 * 
 */
package com.sky.bloggerme.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;
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
import com.sky.bloggerme.ClientCredentials;
import com.sky.bloggerme.R;
import com.sky.bloggerme.db.DatabaseManager;
import com.sky.bloggerme.model.DraftPost;
import com.sky.bloggerme.model.Label;
import com.sky.bloggerme.util.Alert;
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

	/** The Constant DRAFTLIST. */
	private static final int DRAFTLIST = 1;

	private static final int CREATE = 0;

	private static final int DRAFT = 1;

	private static final int POST = 2;

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

	/** Selected blog name. */
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

	/** The italics toggle. */
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

	// /** title: The TextView component which displays the blog title;. */
	// TextView title;

	/** myMultiAutoCompleteTextView: The MultiAutoCompleteTextView component which displays the blog title;. */
	MultiAutoCompleteTextView labelsMultiAutoComplete;

	/** Whether we have received a 401. Used to initiate re-authorising the authToken. */
	private boolean received401;

	/** The account chosen. */
	private static boolean blogChosen = false, accountChosen = false;

	/** The labels. */
	private String[] labels;

	/** create: Whether we are creating or updating a post;. */
	Boolean create = true;

	/** The draft post. */
	private DraftPost draftPost;

	private String blogPostId;

	private boolean draft;

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

			List<Label> labels = DatabaseManager.getInstance(this).getAllLabels();
			List<String> labelNames = new ArrayList<String>();
			if (labels != null && !labels.isEmpty())
			{
				for (Label label : labels)
				{
					labelNames.add(label.getName());
				}
				setModel(labelNames);
			}
			// new AsyncLoadLabels(this).execute();
		}
	}

	/**
	 * Initialisation.
	 */
	private void init()
	{

		DatabaseManager.init(this);
		postContent = (DroidWriterEditText) findViewById(R.id.post_body);

		boldToggle = (ToggleButton) findViewById(R.id.BoldButton);
		italicsToggle = (ToggleButton) findViewById(R.id.ItalicsButton);
		// underlineToggle = (ToggleButton) findViewById(R.id.UnderlineButton);
		postContent.setBoldToggleButton(boldToggle);
		postContent.setItalicsToggleButton(italicsToggle);
		// postContent.setUnderlineToggleButton(underlineToggle);

		postTitle = (EditText) findViewById(R.id.post_title);
		labelsMultiAutoComplete = (MultiAutoCompleteTextView) findViewById(R.id.post_labels);

		/*
		 * Log.v(TAG, "Capturing publishbutton"); createPostButton = (Button) findViewById(R.id.publishbutton); Log.v(TAG, "Setting publishbutton's OnClickListener"); createPostButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { String title = postTitle.getText().toString(); String content = postContent.getTextHTML(); List<String> labels_list = new ArrayList<String>(); StringTokenizer st = new StringTokenizer(labelsMultiAutoComplete.getText().toString(), ","); while (st.hasMoreTokens()) { String labelToken = st.nextToken().trim(); labels_list.add(labelToken); }
		 * 
		 * Post post = new Post().setTitle(title).setContent(content).setLabels(labels_list); (new AsyncCreatePost(EditorActivity.this, create)).execute(post); } });
		 * 
		 * Log.v(TAG, "Disabling publishbutton"); createPostButton.setEnabled(false);
		 * 
		 * Log.v(TAG, "Capturing postsbutton"); viewPostsButton = (Button) findViewById(R.id.postsbutton); Log.v(TAG, "Setting postsbutton's OnClickListener"); viewPostsButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent = new Intent(EditorActivity.this, PostListActivity.class); intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(intent); finish(); } });
		 * 
		 * Log.v(TAG, "Disabling postsbutton"); viewPostsButton.setEnabled(false);
		 */

		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			Log.d(TAG, "We have extra. ");

			if (extras.containsKey("editExisting"))
			{
				draft = false;
				create = false;
				updatePostDetails(extras);
			}
			else
			{
				draft = true;
				updateDraftDetails(extras);
			}
		}
		else
		{
			Log.d(TAG, "We do not have any extra");
			create = true;
		}
		initDrawer();
	}

	private void updateDraftDetails(Bundle extras)
	{
		int draftPostId = extras.getInt(Constants.DRAFTPOST_ID, -1);
		String draftTitle = extras.getString(Constants.DRAFTPOST_TITLE);
		String draftLabels = extras.getString(Constants.DRAFTPOST_LABELS);
		String draftContent = extras.getString(Constants.DRAFTPOST_CONTENT);
		String draftCreatedAt = extras.getString(Constants.DRAFTPOST_CREATEDAT);
		draftPost = new DraftPost();
		if (extras.containsKey(Constants.DRAFTPOST_BLOGPOSTID))
		{
			setBlogPostId(extras.getString(Constants.DRAFTPOST_BLOGPOSTID));
			create = false;
			draftPost.setBlogPostId(getBlogPostId());
		}
		else
		{
			create = true;
		}
		draftPost.set_Id(draftPostId);
		draftPost.setTitle(draftTitle);
		draftPost.setLabels(draftLabels);
		draftPost.setContent(draftContent);
		draftPost.setCreatedAt(draftCreatedAt);
		setupDraftPost();
	}

	/**
	 * initialise the left drawer
	 */
	private void initDrawer()
	{
		mTitle = mDrawerTitle = getTitle();
		drawerPages = getResources().getStringArray(R.array.drawer_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.create_drawer);

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

		selectItem(Constants.DRAWERLIST.CREATE.getDrawerList());

	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		// If the nav drawer is open, hide action items related to the content view
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Update draft post.
	 * 
	 * @param title
	 *            the title of the draft
	 * @param labels
	 *            the labels of the draft
	 * @param content
	 *            the content of the draft
	 * @param createdAt
	 *            the created date and time of the draft
	 * @param blogPostId
	 *            the postid of the draft in blogger
	 * @return true if db is updated successfully
	 */
	private Boolean updateDraftPost(String title, String labels, String content, String createdAt, String blogPostId)
	{
		Boolean updated = false;
		if (draftPost != null)
		{
			draftPost.setTitle(title);
			draftPost.setLabels(labels);
			Log.d(TAG, "content in updateDraftPost: " + content);
			draftPost.setContent(content);
			draftPost.setCreatedAt(createdAt);
			draftPost.setBlogPostId(blogPostId);
			Log.d(TAG, "draftPost id: " + draftPost.get_Id());
			DatabaseManager.getInstance(this).updateDraftPost(draftPost);
		}
		else
		{
			Log.d(TAG, "DraftPost object is null");
		}
		return updated;
	}

	/**
	 * Creates the new draft post.
	 * 
	 * @param title
	 *            the title of the draft
	 * @param labels
	 *            the labels of the draft
	 * @param content
	 *            the content of the draft
	 * @param createdAt
	 *            the created date and time of the draft
	 * @param blogPostId
	 *            the postid of the draft in blogger
	 * @return true if db is updated successfully
	 */
	private Boolean createNewDraftPost(String title, String labels, String content, String createdAt, String blogPostId)
	{
		draftPost = new DraftPost();
		draftPost.setTitle(title);
		draftPost.setLabels(labels);
		draftPost.setContent(content);
		draftPost.setCreatedAt(createdAt);
		draftPost.setBlogPostId(blogPostId);
		return DatabaseManager.getInstance(this).addDraftPost(draftPost);
	}

	/**
	 * Update post details.
	 * 
	 * @param extras
	 *            the extras
	 */
	private void updatePostDetails(Bundle extras)
	{
		create = false;
		String title = extras.getString("Title");
		String content = extras.getString("Content");
		String labels = extras.getString("Labels");
		setBlogPostId(extras.getString("BlogPostId"));
		if (title != null && !title.isEmpty())
			postTitle.setText(title);
		if (content != null && !content.isEmpty())
			postContent.setTextHTML(content);
		if (labels != null)
		{
			Log.d(TAG, "Labels: " + labels);
			labelsMultiAutoComplete.setText(labels);
		}
	}

	/**
	 * We have an account in our DB.
	 */
	void gotAccount()
	{
		Log.v(TAG, "Retrieving the account for " + accountName);
		Account account = accountManager.getAccountByName(accountName);
		if (account == null)
		{
			Log.v(TAG, "account was null, forcing user to choose account");
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

	/**
	 * Launches the login activity and finishes the current activity.
	 */
	private void goLoginActivity()
	{
		Intent login = new Intent(EditorActivity.this, LoginActivity.class);
		login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(login);
		finish();
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
			// title = (TextView) findViewById(R.id.blogTitle);
			// title.setText(blogTitle);
		}

	}

	/**
	 * Retrieve an account via the AccountManager.
	 */
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

	/**
	 * We have a blog in our DB.
	 */
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

	/**
	 * Start blogs list activity and finishes the current activity.
	 */
	void startBlogsListActivity()
	{
		Intent intent = new Intent(EditorActivity.this, BlogListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	/**
	 * Sets the account name.
	 * 
	 * @param accountName
	 *            the new account name
	 */
	void setAccountName(String accountName)
	{
		editor = settings.edit();
		editor.putString(Constants.PREF_ACCOUNT_NAME, accountName);
		editor.commit();
		this.accountName = accountName;
		Log.v(TAG, "Stored accountName: " + accountName);
	}

	/**
	 * Sets the auth token.
	 * 
	 * @param authToken
	 *            the new auth token
	 */
	void setAuthToken(String authToken)
	{
		editor = settings.edit();
		editor.putString(Constants.PREF_AUTH_TOKEN, authToken);
		editor.commit();
		credential.setAccessToken(authToken);
		Log.v(TAG, "Stored authToken");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
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
		// case DRAFTLIST:
		// // get the ID of the chosen draft and populate the relevant fields
		// if (resultCode == RESULT_OK)
		// {
		// int draftPostId = data.getIntExtra(Constants.DRAFTPOST_ID, -1);
		// String draftTitle = data.getStringExtra(Constants.DRAFTPOST_TITLE);
		// String draftLabels = data.getStringExtra(Constants.DRAFTPOST_LABELS);
		// String draftContent = data.getStringExtra(Constants.DRAFTPOST_CONTENT);
		// String draftCreatedAt = data.getStringExtra(Constants.DRAFTPOST_CREATEDAT);
		// draftPost = new DraftPost();
		// if (data.hasExtra(Constants.DRAFTPOST_BLOGPOSTID))
		// {
		// setBlogPostId(data.getStringExtra(Constants.DRAFTPOST_BLOGPOSTID));
		// create = false;
		// draftPost.setBlogPostId(getBlogPostId());
		// }
		// draftPost.set_Id(draftPostId);
		// draftPost.setTitle(draftTitle);
		// draftPost.setLabels(draftLabels);
		// draftPost.setContent(draftContent);
		// draftPost.setCreatedAt(draftCreatedAt);
		// setupDraftPost();
		// }
		// break;
		}
	}

	/**
	 * On auth token.
	 */
	void onAuthToken()
	{
		// Log.v(TAG, "Enabling publishbutton");
		// createPostButton.setEnabled(true);
		//
		// Log.v(TAG, "Enabling postsbutton");
		// viewPostsButton.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_create_post, menu);
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
			case R.id.menu_publish:
				publishPost();
				break;
			case R.id.menu_cancel:
				cancelPost();
				break;
			case R.id.menu_accounts:
				chooseAccount();
				break;
			case R.id.menu_settings:
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				startActivity(settingsIntent);
				break;
			case R.id.menu_savedraft:
				if (saveDraft())
				{
					Log.d("EditorActivity", "Updated: " + true);
					Toast.makeText(this, "Draft saved", Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.menu_drafts:
				goToDrafts();
				break;
			case R.id.menu_logout:
				doLogout();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void cancelPost()
	{
		// If editing in progress, clicking this will prompt user whether to cancel the edit and go back to either post or draft screen

		// If normal creation of new post, clicking this will prompt user that it will clear the input but remain in current screen
		if (create)
		{
			displayAlert(CREATE);
		}
		else
		{
			if (draft)
			{
				displayAlert(DRAFT);
			}
			else
			{
				displayAlert(POST);
			}
		}
	}

	private void displayAlert(int type)
	{
		switch (type)
		{
			case CREATE:
				Alert.showAlert(this, "Clear Everything", "Pressing the OK button will clear everything. Are you sure?", "OK", new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt)
					{
						clearEverything();
						paramDialogInterface.dismiss();
					}
				}, "Cancel", new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt)
					{
						paramDialogInterface.dismiss();
					}

				});
				break;
			case DRAFT:
				Alert.showAlert(this, "Undo Edit", "Pressing the OK button will undo the edit and returns to previous screen. Are you sure?", "OK", new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt)
					{
						clearEverything();
						paramDialogInterface.dismiss();
						goToDrafts();
					}
				}, "Cancel", new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt)
					{
						paramDialogInterface.dismiss();
					}

				});
				break;
			case POST:
				Alert.showAlert(this, "Undo Edit", "Pressing the OK button will undo the edit and returns to previous screen. Are you sure?", "OK", new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt)
					{
						clearEverything();
						paramDialogInterface.dismiss();
						goToPost();
					}
				}, "Cancel", new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt)
					{
						paramDialogInterface.dismiss();
					}

				});
				break;
		}
	}

	private void goToPost()
	{
		Intent i = new Intent(getApplicationContext(), PostDisplayActivity.class);
		i.putExtra(Constants.POST_ID_KEY, getBlogPostId());
		startActivity(i);
	}

	private void clearEverything()
	{
		postTitle.setText(StringUtils.EMPTY);
		postContent.setTextHTML(StringUtils.EMPTY);
		labelsMultiAutoComplete.setText(StringUtils.EMPTY);
	}

	/**
	 * 
	 */
	private void publishPost()
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

	private void goToDrafts()
	{
		Intent draftList = new Intent(EditorActivity.this, DraftListActivity.class);
		startActivity(draftList);
	}

	/**
	 * Save draft.
	 * 
	 * @return true if draft is saved successfully
	 */
	private Boolean saveDraft()
	{
		Boolean updated;
		if (checkDraft())
		{
			String title = postTitle.getText().toString();
			Log.d(TAG, "Title: " + title);
			String labels = labelsMultiAutoComplete.getText().toString();
			String content = postContent.getTextHTML();
			Log.d(TAG, "Content: " + content);
			String createdAt;

			Time today = new Time(Time.getCurrentTimezone());
			today.setToNow();
			createdAt = today.format2445();

			if (draftPost != null)
			{
				if (!draftPost.getCreatedAt().isEmpty())
				{
					createdAt = draftPost.getCreatedAt();
				}
				Log.d(TAG, "in save draft before updating draftpost");
				if (getBlogPostId() != null)
				{
					Log.d(TAG, "constants post id is not null");
					updated = updateDraftPost(title, labels, content, createdAt, getBlogPostId());
				}
				else
				{
					updated = updateDraftPost(title, labels, content, createdAt, "");
				}
			}
			else
			{
				Log.d(TAG, "Draft Post object is null");
				if (getBlogPostId() != null)
				{
					updated = createNewDraftPost(title, labels, content, createdAt, getBlogPostId());
				}
				else
				{
					updated = createNewDraftPost(title, labels, content, createdAt, "");
				}
			}
			return updated;
		}
		else
			return false;
	}

	/**
	 * User will be logged out. Launches the login activity.
	 */
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

	/**
	 * A method for debugging purposes. To display the post's title.
	 * 
	 * @param result
	 *            the result
	 */
	public void display(Post result)
	{
		Log.v(TAG, "Displaying " + result.getTitle());
		// ((EditText) findViewById(R.id.post_title)).setText(result.getTitle());
		// ((EditText) findViewById(R.id.post_body)).setText(result.getContent());

	}

	/**
	 * On request completed.
	 * 
	 * @param result
	 *            the post result
	 */
	void onRequestCompleted(Post result)
	{
		Log.v(TAG, "Request completed, throwing away 401 state");
		received401 = false;
		if (result != null)
		{
			Log.v(TAG, "Post result not null");
			if (result.getId() != null)
			{
				// Remove the current draft entry
				if (draftPost != null)
				{
					Log.d(TAG, "draftPost id: " + draftPost.get_Id());
					DatabaseManager.getInstance(this).removeDraftPost(draftPost.get_Id());
				}
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

	/**
	 * Check for any input by the user in any of the following fields: Post Title / Label / Content.
	 * 
	 * @return true if there is input
	 */
	private Boolean checkDraft()
	{
		if (postTitle.getText().toString().isEmpty() && postContent.getTextHTML().isEmpty() && labelsMultiAutoComplete.getText().toString().isEmpty())
		{
			Log.d(TAG, "check draft, returning false");
			return false;
		}
		return true;

	}

	/**
	 * Setup draft post.
	 */
	private void setupDraftPost()
	{
		// Bundle bundle = getIntent().getExtras();
		// if (null != bundle && bundle.containsKey(Constants.DRAFTPOST_ID))
		// {
		// int draftPostId = bundle.getInt(Constants.DRAFTPOST_ID);
		// draftPost = DatabaseManager.getInstance().getDraftPostWithId(draftPostId);
		if (draftPost != null)
		{
			String title = draftPost.getTitle();
			String labels = draftPost.getLabels();
			String content = draftPost.getContent();
			if (title != null)
			{
				postTitle.setText(title);
			}
			if (labels != null)
			{
				labelsMultiAutoComplete.setText(labels);
			}
			if (content != null)
			{
				postContent.setTextHTML(content);
			}
		}
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

	/**
	 * Gets the blog id.
	 * 
	 * @return the blog id
	 */
	public String getBlogID()
	{
		// TODO Auto-generated method stub
		return settings.getString(Constants.PREF_BLOG_ID, null);
	}

	/**
	 * Sets the model.
	 * 
	 * @param result
	 *            the new model
	 */
	public void setModel(List<String> result)
	{
		labels = new String[result.size()];
		result.toArray(labels);

		labelsMultiAutoComplete.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, labels));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause()
	{
		Log.d(TAG, "in on pause");
		saveDraft();
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		selectItem(Constants.DRAWERLIST.CREATE.getDrawerList());
		List<Label> labels = DatabaseManager.getInstance(this).getAllLabels();
		List<String> labelNames = new ArrayList<String>();
		if (labels != null && !labels.isEmpty())
		{
			for (Label label : labels)
			{
				labelNames.add(label.getName());
			}
			setModel(labelNames);
		}
		super.onResume();
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
				// Current Editor Activity
				break;
			case 1:
				// Blogs Activity
				Intent blogIntent = new Intent(EditorActivity.this, BlogListActivity.class);
				blogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(blogIntent);
				finish();
				break;
			case 2:
				// Posts Activity
				Intent intent = new Intent(EditorActivity.this, PostListActivity.class);
				startActivity(intent);
				finish();
				break;
			case 3:
				// Drafts Activity
				goToDrafts();
				break;
			default:
				break;
		}
		// // Create a new fragment and specify the planet to show based on position
		// Fragment fragment = new PlanetFragment();
		// Bundle args = new Bundle();
		// args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		// fragment.setArguments(args);
		//
		// // Insert the fragment by replacing any existing fragment
		// FragmentManager fragmentManager = getFragmentManager();
		// fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
		//
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

	/**
	 * @return the blogPostId
	 */
	public String getBlogPostId()
	{
		return blogPostId;
	}

	/**
	 * @param blogPostId
	 *            the blogPostId to set
	 */
	public void setBlogPostId(String blogPostId)
	{
		this.blogPostId = blogPostId;
	}
}
