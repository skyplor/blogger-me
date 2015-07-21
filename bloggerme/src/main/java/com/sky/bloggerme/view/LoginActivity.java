package com.sky.bloggerme.view;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.BloggerScopes;
import com.sky.bloggerme.ClientCredentials;
import com.sky.bloggerme.R;

import java.io.IOException;

;

// TODO: Auto-generated Javadoc

/**
 * The Class LoginActivity.
 */
public class LoginActivity extends Activity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    /**
     * The Constant TAG.
     */
    private static final String TAG = "Login Activity";
//	private EditText textUsername, textPassword;
//	private ProgressDialog verifyProgress = null;

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;

    private static final int DIALOG_PLAY_SERVICES_ERROR = 0;

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    private static final String SAVED_PROGRESS = "sign_in_progress";
    private static final String DIALOG_TYPE = "dialogType";
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;


    // We use mSignInProgress to track whether user has clicked sign in.
    // mSignInProgress can be one of three values:
    //
    //       STATE_DEFAULT: The default state of the application before the user
    //                      has clicked 'sign in', or after they have clicked
    //                      'sign out'.  In this state we will not attempt to
    //                      resolve sign in errors and so will display our
    //                      Activity in a signed out state.
    //       STATE_SIGN_IN: This state indicates that the user has clicked 'sign
    //                      in', so resolve successive errors preventing sign in
    //                      until the user has successfully authorized an account
    //                      for our app.
    //   STATE_IN_PROGRESS: This state indicates that we have started an intent to
    //                      resolve an error, and so we should not start further
    //                      intents until the current intent completes.
    private int mSignInProgress;


    // Used to store the PendingIntent most recently returned by Google Play
    // services until the user clicks 'sign in'.
    private PendingIntent mSignInIntent;

    // Used to store the error code most recently returned by Google Play services
    // until the user clicks 'sign in'.
    private int mSignInError;

    /**
     * Account Manager to request auth from for Google Accounts.
     */
    GoogleAccountManager accountManager;

    /**
     * Shared Preferences for storing auth credentials.
     */
    SharedPreferences settings;

    /**
     * Shared Preferences editor for editing auth credentials.
     */
    SharedPreferences.Editor editor;

    /**
     * Selected account name we are authorizing as.
     */
    String accountName;

    /**
     * HTTP rewriter responsible for managing lifetime of oauth2 credentials.
     */
    GoogleCredential credential = new GoogleCredential();

    /**
     * Choose the right HttpTransport depending on Android version.
     */
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();

    /**
     * The JSON factory to use to marshall data onto the wire.
     */
    final JsonFactory jsonFactory = new AndroidJsonFactory();

    /**
     * Facade object for Blogger API v3.
     */
    public Blogger service;

    /**
     * OAuth2.0 scope for Read/Write access to Blogger.
     */
    static final String AUTH_TOKEN_TYPE = "oauth2:" + BloggerScopes.BLOGGER;

    /**
     * Intent Key for round tripping login.
     */
    static final int REQUEST_AUTHENTICATE = 0;

    /**
     * Shared Preferences key for the selected user's account name.
     */
    static final String PREF_ACCOUNT_NAME = "accountName";

    /**
     * Shared Preferences key for storing the user's OAuth token.
     */
    static final String PREF_AUTH_TOKEN = "authToken";

    /**
     * Shared Preferences key for storing the user's blog ID.
     */
    static final String PREF_BLOG_ID = "blogID";

    /**
     * The Constant BACK_KEY_PRESSED.
     */
    private static final int BACK_KEY_PRESSED = 20;

    /**
     * REPLACE ME WITH A BLOG ID OF SOMETHING YOU HAVE WRITE PRIVS ON.
     */
    // TODO Get Blog id of the chosen blog and replace this
    public static String BLOG_ID = "";

    /**
     * The backkeypressed.
     */
    private static boolean accountChosen = false, backkeypressed = false;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        mGoogleApiClient = buildGoogleApiClient();
//        onLoggedIn();
    }

    private void onLoggedIn() {
        Log.v(TAG, "Building the Blogger API v3 service facade");
        service = new Blogger.Builder(transport, jsonFactory, credential).setGoogleClientRequestInitializer(new CommonGoogleClientRequestInitializer(ClientCredentials.KEY)).setApplicationName("Blogger-me/1.0").build();
        Log.v(TAG, "Getting the private SharedPreferences instance");
        settings = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);
        Log.v(TAG, "Retrieving the account name from settings");
        accountName = settings.getString(PREF_ACCOUNT_NAME, null);
        Log.v(TAG, "accountName: " + accountName);
        credential.setAccessToken(settings.getString(PREF_AUTH_TOKEN, null));
        // Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
        accountManager = new GoogleAccountManager(this);
        gotAccount();
    }

    private GoogleApiClient buildGoogleApiClient() {
        // When we build the GoogleApiClient we specify where connected and
        // connection failed callbacks should be returned, which Google APIs our
        // app uses and which OAuth 2.0 scopes our app requests.
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Got account.
     */
    void gotAccount() {
        Log.v(TAG, "Retrieving the account for " + accountName);
        Account account = accountManager.getAccountByName(accountName);
        if (account == null) {
            Log.v(TAG, "account was null, forcing user to choose account");
            chooseAccount();
            return;
        }
        if (credential.getAccessToken() != null) {
            Log.v(TAG, "We have an AccessToken");
            onAuthToken();
            accountChosen = true;
            return;
        }
        Log.v(TAG, "We have an account, but no stored Access Token. Requesting from the AccountManager");
        accountManager.getAccountManager().getAuthToken(account, AUTH_TOKEN_TYPE, null, true, new AccountManagerCallback<Bundle>() {

            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bundle = future.getResult();
                    if (bundle.containsKey(AccountManager.KEY_INTENT)) {
                        Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
                        intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
                        Log.v(TAG, "We need AccountManager to talk to the user. Starting Activity.");
                        startActivityForResult(intent, REQUEST_AUTHENTICATE);
                        accountChosen = true;
                    } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                        Log.v(TAG, "AccountManager handed us a AuthToken, storing for future reference");
                        setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
                        accountChosen = true;
                        onAuthToken();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    accountChosen = false;
                }
            }
        }, null);
    }

    /**
     * Choose account.
     */
    private void chooseAccount() {
        Log.v(TAG, "Asking the AccountManager to find us an account to auth as");
        accountManager.getAccountManager().getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE, AUTH_TOKEN_TYPE, null, LoginActivity.this, null, null, new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
                Bundle bundle;
                try {
                    Log.v(TAG, "Requesting result");
                    bundle = future.getResult();
                    Log.v(TAG, "Retrieving Account Name");
                    setAccountName(bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
                    Log.v(TAG, "Retrieving Auth Token");
                    setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
                    Log.v(TAG, "Stored for future reference");
                    accountChosen = true;
                    onAuthToken();
                } catch (OperationCanceledException e) {
                    // user canceled
                    Log.v(TAG, "Operation Cancelled Exception");
                    backkeypressed = true;
                    accountChosen = false;
                } catch (AuthenticatorException e) {
                    Log.e(TAG, e.getMessage(), e);
                    accountChosen = false;
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    accountChosen = false;
                }
            }
        }, null);
    }

    /**
     * Start blogs list activity.
     */
    void startBlogsListActivity() {
        Intent intent = new Intent(LoginActivity.this, BlogListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Sets the account name.
     *
     * @param accountName the new account name
     */
    void setAccountName(String accountName) {
        editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.commit();
        this.accountName = accountName;
        Log.v(TAG, "Stored accountName: " + accountName);
    }

    /**
     * Sets the auth token.
     *
     * @param authToken the new auth token
     */
    void setAuthToken(String authToken) {
        editor = settings.edit();
        editor.putString(PREF_AUTH_TOKEN, authToken);
        editor.commit();
        credential.setAccessToken(authToken);
        Log.v(TAG, "Stored authToken");
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.v(TAG, "Returning from another Activity");
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case REQUEST_AUTHENTICATE:
//                Log.v(TAG, "request code is REQUEST_AUTHENTICATE");
//                if (resultCode == RESULT_OK) {
//                    Log.v(TAG, "Result was RESULT_OK");
////					gotAccount();
//                    if (accountChosen) {
//                        startBlogsListActivity();
//                    }
//
//                } else if (resultCode == BACK_KEY_PRESSED) {
//                    Log.v(TAG, "Result was BACK_KEY_PRESSED");
//                } else {
//                    Log.v(TAG, "Result was NOT RESULT_OK");
//                    chooseAccount();
//                }
//                break;
//        }
//    }

    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
//    @Override
//    public void onResume() {
//        super.onResume();
//        accountName = settings.getString(PREF_ACCOUNT_NAME, null);
//        if (accountName != null) {
//            startBlogsListActivity();
//        } else if (backkeypressed) {
//            backkeypressed = false;
//            finish();
//        }
////		else
////		{
////			Log.v(TAG, "in onResume method and accountName is null");
////			gotAccount();
////		}
//    }

    /**
     * On auth token.
     */
    void onAuthToken() {
        Log.v(TAG, "In on Authentication Token");
        if (accountChosen) {
            startBlogsListActivity();
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            System.out.println("KEYEVENT: " + event);
            moveTaskToBack(true);
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    /* onConnectionFailed is called when our Activity could not connect to Google
       * Play services.  onConnectionFailed indicates that the user needs to select
       * an account, grant permissions or resolve an error in order to sign in.
       */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    /* onConnected is called when our Activity successfully connects to Google
     * Play services.  onConnected indicates that an account was selected on the
     * device, that the selected account has granted any requested permissions to
     * our app and that we were able to establish a service connection to Google
     * Play services.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Reaching onConnected means we consider the user signed in.
        Log.i(TAG, "onConnected");

        // Retrieve some profile information to personalize our app for the user.
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

        // Indicate that the sign in process is complete.
        mSignInProgress = STATE_DEFAULT;
        onLoggedIn();

    }

    /* Starts an appropriate intent or dialog for user interaction to resolve
     * the current error preventing the user from being signed in.  This could
     * be a dialog allowing the user to select an account, an activity allowing
     * the user to consent to the permissions being requested by your app, a
     * setting to enable device networking, etc.
     */
    private void resolveSignInError() {
        if (mSignInIntent != null) {
            // We have an intent which will allow our user to sign in or
            // resolve an error.  For example if the user needs to
            // select an account to sign in with, or if they need to consent
            // to the permissions your app is requesting.

            try {
                // Send the pending intent that we stored on the most recent
                // OnConnectionFailed callback.  This will allow the user to
                // resolve the error currently preventing our connection to
                // Google Play services.
                mSignInProgress = STATE_IN_PROGRESS;
                startIntentSenderForResult(mSignInIntent.getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (SendIntentException e) {
                Log.i(TAG, "Sign in intent could not be sent: "
                        + e.getLocalizedMessage());
                // The intent was canceled before it was sent.  Attempt to connect to
                // get an updated ConnectionResult.
                mSignInProgress = STATE_SIGN_IN;
                mGoogleApiClient.connect();
            }
        } else {
            // Google Play services wasn't able to provide an intent for some
            // error types, so we show the default Google Play services error
            // dialog which may still start an intent on our behalf if the
            // user can resolve the issue.
            showErrorDialog(DIALOG_PLAY_SERVICES_ERROR);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    private void onSignedOut() {
        // Update the UI to reflect that the user is signed out.
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        // We call connect() to attempt to re-establish the connection or get a
        // ConnectionResult that we can attempt to resolve.
        mGoogleApiClient.connect();
    }

    // Creates a dialog for an alert message
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }


    //  A fragment to display an alert dialog
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((LoginActivity) getActivity()).onDialogDismissed();
        }
    }
}