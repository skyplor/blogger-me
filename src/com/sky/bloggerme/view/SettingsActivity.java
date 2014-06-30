package com.sky.bloggerme.view;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.extensions.android3.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.blogger.Blogger;
import com.sky.bloggerme.ClientCredentials;
import com.sky.bloggerme.R;
import com.sky.bloggerme.db.DatabaseManager;
import com.sky.bloggerme.model.Label;
import com.sky.bloggerme.util.Constants;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On handset devices, settings are presented as a single list. On tablets, settings are split by category, with category headers shown to the left of the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android Design: Settings</a> for design guidelines and the <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
{
	/**
	 * Determines whether to always show the simplified settings UI, where settings are presented in a single list. When false, settings are shown as a master/detail two-pane view on tablets. When true, a single pane is shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	private final String TAG = getClass().getSimpleName();

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

	/** Whether we have received a 401. Used to initiate re-authorising the authToken. */
	private boolean received401;

	/** HTTP rewriter responsible for managing lifetime of oauth2 credentials. */
	GoogleCredential credential = new GoogleCredential();

	/** The account chosen. */
	private static boolean blogChosen = false, accountChosen = false;

	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the device configuration dictates that a simplified, single-pane UI should be shown.
	 */
	private void setupSimplePreferencesScreen()
	{
		if (!isSimplePreferences(this))
		{
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead use the older PreferenceActivity APIs.

		PreferenceCategory header = new PreferenceCategory(this);

		// Add 'data and sync' preferences, and a corresponding header.
		header.setTitle(R.string.pref_header_data_sync);
		addPreferencesFromResource(R.xml.pref_data_sync);
		// getPreferenceScreen().addPreference(header);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("sync_frequency"));
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane()
	{
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context)
	{
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device doesn't have newer APIs like {@link PreferenceFragment}, or the device doesn't have an extra-large screen. In these cases, a single-pane "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context)
	{
		return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target)
	{
		if (!isSimplePreferences(this))
		{
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener()
	{
		@Override
		public boolean onPreferenceChange(Preference preference, Object value)
		{
			String stringValue = value.toString();

			if (preference instanceof ListPreference)
			{
				// For list preferences, look up the correct display value in the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

			}
			else
			{
				// For all other preferences, set the summary to the value's simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the preference's value is changed, its summary (line of text below the preference title) is updated to reflect the value. The summary is also immediately updated upon calling this method. The exact display format is dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference)
	{
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
	}

	/**
	 * This fragment shows data and sync preferences only. It is used when the activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DataSyncPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_data_sync);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design guidelines.
			bindPreferenceSummaryToValue(findPreference("sync_frequency"));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_settings, menu);
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
		switch (item.getItemId())
		{
			case R.id.menu_sync:
				retrieveLabels();
				break;

		}
		return super.onOptionsItemSelected(item);
	}

	private void retrieveLabels()
	{

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
			new AsyncLoadLabels(this).execute();
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
			// title = (TextView) findViewById(R.id.blogTitle);
			// title.setText(blogTitle);
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
	 * Launches the login activity and finishes the current activity.
	 */
	private void goLoginActivity()
	{
		Intent login = new Intent(this, LoginActivity.class);
		login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(login);
		finish();
	}

	/**
	 * Start blogs list activity and finishes the current activity.
	 */
	void startBlogsListActivity()
	{
		Intent intent = new Intent(this, BlogListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
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

	/**
	 * Sets the model.
	 * 
	 * @param result
	 *            the new model
	 */
	public void setModel(List<String> result)
	{
		// pass into database
		if (result != null && !result.isEmpty())
		{
			for (int i = 0; i < result.size(); i++)
			{
				Label label = new Label();
				label.setName(result.get(i));
				// check for duplicated labels by retrieving from database first
				if (!isLabelDuplicated(label))
					DatabaseManager.getInstance(this).addLabels(label);
			}
		}
	}

	/**
	 * Checks if label is duplicated.
	 * 
	 * @param label
	 *            the label
	 * @return true, if label is duplicated
	 */
	private boolean isLabelDuplicated(Label label)
	{
		boolean isDuplicated = false;
		List<Label> labels = DatabaseManager.getInstance(this).getAllLabels();
		for (Label l : labels)
		{
			if (l.getName().equalsIgnoreCase(label.getName()))
			{
				isDuplicated = true;
				break;
			}
		}
		return isDuplicated;
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
}
