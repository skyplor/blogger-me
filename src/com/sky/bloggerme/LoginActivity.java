package com.sky.bloggerme;

//public class Login extends Activity
//{
//
//	/** Called when the activity is first created. */
//	@Override
//	public void onCreate(Bundle savedInstanceState)
//	{
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.login);
//		// TODO Auto-generated method stub
//	}
//
//	private String username, password;
//	private static GoogleService myService;
//	private boolean connected;
//	private final String APPNAME = "SkyBlogger";
//	private final String SERVICENAME = "blogger";
//
//	public Login(String username, String password)
//	{
//		this.username = username;
//		this.password = password;
//		myService = new GoogleService(SERVICENAME, APPNAME);
//		if (myService != null)
//		{
//			try
//			{
//				myService.setUserCredentials(this.username, this.password);
//				connected = true;
//			}
//			catch (com.google.gdata.util.AuthenticationException e)
//			{
//				// Log.e(TAG, "Authentication failed for user " + this.userid+ ".");
//			}
//		}
//		else
//		{
//			// Log.e(TAG, "Unable to create GoogleService!");
//		}
//	}
//
//}

import java.io.IOException;
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
import android.view.KeyEvent;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.extensions.android3.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.BloggerScopes;
import com.sky.bloggerme.R;

public class LoginActivity extends Activity
{
	private static final String TAG = "Login Activity";
//	private EditText textUsername, textPassword;
//	private ProgressDialog verifyProgress = null;

	/** Account Manager to request auth from for Google Accounts. */
	GoogleAccountManager accountManager;

	/** Shared Preferences for storing auth credentials. */
	SharedPreferences settings;

	/** Shared Preferences editor for editing auth credentials. */
	SharedPreferences.Editor editor;

	/** Selected account name we are authorizing as. */
	String accountName;

	/** HTTP rewriter responsible for managing lifetime of oauth2 credentials. */
	GoogleCredential credential = new GoogleCredential();

	/** Choose the right HttpTransport depending on Android version. */
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();

	/** The JSON factory to use to marshall data onto the wire. */
	final JsonFactory jsonFactory = new AndroidJsonFactory();

	/** Facade object for Blogger API v3. */
	public Blogger service;

	/** OAuth2.0 scope for Read/Write access to Blogger. */
	static final String AUTH_TOKEN_TYPE = "oauth2:" + BloggerScopes.BLOGGER;

	/** Intent Key for round tripping login. */
	static final int REQUEST_AUTHENTICATE = 0;

	/** Shared Preferences key for the selected user's account name. */
	static final String PREF_ACCOUNT_NAME = "accountName";

	/** Shared Preferences key for storing the user's OAuth token. */
	static final String PREF_AUTH_TOKEN = "authToken";

	/** Shared Preferences key for storing the user's blog ID. */
	static final String PREF_BLOG_ID = "blogID";
	
	private static final int BACK_KEY_PRESSED = 20;

	/** REPLACE ME WITH A BLOG ID OF SOMETHING YOU HAVE WRITE PRIVS ON. */
	// TODO Get Blog id of the chosen blog and replace this
	public static String BLOG_ID = "";

	private static boolean accountChosen = false, backkeypressed = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Log.v(TAG, "Building the Blogger API v3 service facade");
		service = new com.google.api.services.blogger.Blogger.Builder(transport, jsonFactory, credential).setApplicationName("Google-BloggerAndroidSample/1.0").build();
		Log.v(TAG, "Getting the private SharedPreferences instance");
		settings = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);
		Log.v(TAG, "Retrieving the account name from settings");
		accountName = settings.getString(PREF_ACCOUNT_NAME, null);
		Log.v(TAG, "accountName: " + accountName);
		credential.setAccessToken(settings.getString(PREF_AUTH_TOKEN, null));
		// Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
		accountManager = new GoogleAccountManager(this);
		gotAccount();
		// ((EditText) this.findViewById(R.id.login_email)).setOnKeyListener(new OnKeyListener()
		// {
		// @Override
		// public boolean onKey(View v, int keyCode, KeyEvent event)
		// {
		// ((Button) LoginActivity.this.findViewById(R.id.login_btn)).setEnabled(true);
		// return false;
		// }
		// });
		// ((EditText) this.findViewById(R.id.login_pwd)).setOnKeyListener(new OnKeyListener()
		// {
		// @Override
		// public boolean onKey(View v, int keyCode, KeyEvent event)
		// {
		// ((Button) LoginActivity.this.findViewById(R.id.login_btn)).setEnabled(true);
		// // mDbHelper = new DBAdapter(Login.this);
		// // try
		// // {
		// // mDbHelper.open();
		// // }
		// // catch (SQLException e)
		// // {
		// // // Log.e(TAG, "Database has not opened");
		// // }
		// // setting = mDbHelper.fetchSettindById(1);
		// // startManagingCursor(setting);
		// // mDbHelper.close();
		// // setting.close();
		// return false;
		// }
		// });
		//
		// this.findViewById(R.id.login_btn).setOnClickListener(new OnClickListener()
		// {
		// public void onClick(View v)
		// {
		// verify();
		// // save();
		// login();
		//
		// }
		//
		// private void verify()
		// {
		// // TODO Auto-generated method stub
		// String username = null;
		// String password = null;
		// EditText usernameView = (EditText) LoginActivity.this.findViewById(R.id.login_email);
		// if (usernameView.getText() == null || usernameView.getText().length() < 1)
		// {
		// Alert.showAlert(LoginActivity.this, "Username needed", "You need to give your Google username in order to continue!");
		// return;
		// }
		// else
		// {
		// username = usernameView.getText().toString();
		// }
		// usernameView = null;
		// EditText passwordView = (EditText) LoginActivity.this.findViewById(R.id.login_pwd);
		// if (passwordView.getText() == null || passwordView.getText().length() < 1)
		// {
		// Alert.showAlert(LoginActivity.this, "Password needed", "You need to give your Google password in order to continue!");
		// return;
		// }
		// else
		// {
		// password = passwordView.getText().toString();
		// }
		// verifyProgress = ProgressDialog.show(LoginActivity.this, "Verifying the data", "Starting to verify blogs...");
		//
		// verifyProgress.setMessage("Started to verify your blogs...");
		// }
		//
		// private void save()
		// {
		// // TODO Auto-generated method stub
		// textUsername = (EditText) findViewById(R.id.login_email);
		// textPassword = (EditText) findViewById(R.id.login_pwd);
		// if (textPassword == null || textPassword.getText() == null)
		// {
		// // Log.d(TAG,"password editor view is null when trying to read!");
		// return;
		// }
		// if (textUsername == null || textUsername.getText() == null)
		// {
		// // Log.d(TAG,"Username editor view is null when trying to read!");
		// return;
		// }
		// String usernameStr = textUsername.getText().toString();
		// String passwordStr = textPassword.getText().toString();
		// if (usernameStr.length() < 1)
		// {
		// usernameStr = "";
		// }
		// if (passwordStr.length() < 1)
		// {
		// passwordStr = "";
		// }
		// final Dialog dlg;
		//
		// dlg = new AlertDialog.Builder(LoginActivity.this).setIcon(com.sky.bloggerme.R.drawable.ic_dialog_alert).setTitle("Success").setPositiveButton("OK", null).setMessage("Your profile has been successfully saved.").create();
		//
		// dlg.show();
		//
		// }
		//
		// private void login()
		// {
		// // TODO Auto-generated method stub
		// SharedPreferences login = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);
		// SharedPreferences.Editor editor = login.edit();
		// editor.putBoolean("LoggedIn", true);
		// editor.commit();
		// Intent i = new Intent(LoginActivity.this, Editor.class);
		// startActivity(i);
		// finish();
		// }
		// });
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	void gotAccount()
	{
		Log.v(TAG, "Retrieving the account for " + accountName);
		Account account = accountManager.getAccountByName(accountName);
		if (account == null)
		{
			Log.v(TAG, "account was null, forcing user to choose account");
			chooseAccount();
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
		accountManager.getAccountManager().getAuthToken(account, AUTH_TOKEN_TYPE, null, true, new AccountManagerCallback<Bundle>()
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
						startActivityForResult(intent, REQUEST_AUTHENTICATE);
						accountChosen = true;
					}
					else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN))
					{
						Log.v(TAG, "AccountManager handed us a AuthToken, storing for future reference");
						setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
						accountChosen = true;
						onAuthToken();
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

	private void chooseAccount()
	{
		Log.v(TAG, "Asking the AccountManager to find us an account to auth as");
		accountManager.getAccountManager().getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE, AUTH_TOKEN_TYPE, null, LoginActivity.this, null, null, new AccountManagerCallback<Bundle>()
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
					accountChosen = true;
					onAuthToken();
				}
				catch (OperationCanceledException e)
				{
					// user canceled
					Log.v(TAG, "Operation Cancelled Exception");
					backkeypressed = true;
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

	void startBlogsListActivity()
	{
		Intent intent = new Intent(LoginActivity.this, BlogListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	void setAccountName(String accountName)
	{
		editor = settings.edit();
		editor.putString(PREF_ACCOUNT_NAME, accountName);
		editor.commit();
		this.accountName = accountName;
		Log.v(TAG, "Stored accountName: " + accountName);
	}

	void setAuthToken(String authToken)
	{
		editor = settings.edit();
		editor.putString(PREF_AUTH_TOKEN, authToken);
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
			case REQUEST_AUTHENTICATE:
				Log.v(TAG, "request code is REQUEST_AUTHENTICATE");
				if (resultCode == RESULT_OK)
				{
					Log.v(TAG, "Result was RESULT_OK");
//					gotAccount();
					if (accountChosen)
					{
						startBlogsListActivity();
					}

				}
				else if (resultCode == BACK_KEY_PRESSED)
				{
					Log.v(TAG, "Result was BACK_KEY_PRESSED");
				}
				else
				{
					Log.v(TAG, "Result was NOT RESULT_OK");
					chooseAccount();
				}
				break;
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		accountName = settings.getString(PREF_ACCOUNT_NAME, null);
		if (accountName != null)
		{
			startBlogsListActivity();
		}
		else if (backkeypressed)
		{
			backkeypressed = false;
			finish();
		}
//		else
//		{
//			Log.v(TAG, "in onResume method and accountName is null");
//			gotAccount();
//		}
	}

	void onAuthToken()
	{
		Log.v(TAG, "In on Authentication Token");
		if (accountChosen)
		{
			startBlogsListActivity();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			System.out.println("KEYEVENT: " + event);
			moveTaskToBack(true);
			return true;
		}
		return false;
	}
//	@Override
//	public void onBackPressed() {
////	    Bundle bundle = new Bundle();
////	    bundle.putString(FIELD_A, mA.getText().toString());
//
//	    Intent mIntent = new Intent();
////	    mIntent.putExtras(bundle);
//	    setResult(BACK_KEY_PRESSED, mIntent);
//	    super.onBackPressed();
//	}
}