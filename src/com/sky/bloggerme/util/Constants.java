package com.sky.bloggerme.util;

import java.util.logging.Level;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.google.api.services.blogger.BloggerScopes;
import com.sky.bloggerme.R;

/**
 * The Class Constants.
 */
@ReportsCrashes(formKey = "", // not in use anymore
formUri = "https://darkads.cloudant.com/acra-blogger-me/_design/acra-storage/_update/report", // url for sending to the backend server
reportType = org.acra.sender.HttpSender.Type.JSON, // send to backend server as a JSON message
httpMethod = org.acra.sender.HttpSender.Method.PUT, // to send http put
formUriBasicAuthLogin = "alledlymerimandiseentese", // optional
formUriBasicAuthPassword = "dn866AXpMLUl2wq41DMjJqbE", // optional
mode = ReportingInteractionMode.DIALOG, resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_info, // optional. default is a warning sign
resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
resDialogOkToast = R.string.crash_dialog_ok_toast) // optional. displays a Toast message when the user accepts to send a report.

public class Constants extends Application
{

	/** OAuth2.0 scope for Read/Write access to Blogger. */
	public static final String AUTH_TOKEN_TYPE = "oauth2:" + BloggerScopes.BLOGGER;

	/** Intent Key for round tripping login. */
	public static final int REQUEST_AUTHENTICATE = 0;

	/** Shared Preferences key for the selected user's account name. */
	public static final String PREF_ACCOUNT_NAME = "accountName";

	/** Shared Preferences key for storing the user's OAuth token. */
	public static final String PREF_AUTH_TOKEN = "authToken";

	/** Shared Preferences key for storing the user's blog ID. */
	public static final String PREF_BLOG_ID = "blogID";

	/** REPLACE ME WITH A BLOG ID OF SOMETHING YOU HAVE WRITE PRIVS ON. */
	// TODO Get Blog id of the chosen blog and replace this
	public static String BLOG_ID = "";

	/** Intent key for passing over blog name to drill down activity. */
	public static final String PREF_BLOG_NAME = "BLOG_NAME";

	/** Intent key for passing over postId to drill down activity. */
	public static final String POST_ID_KEY = "POST_ID";

	/** Intent key for passing over postId to drill down activity. */
	public static final Boolean CREATE_UPDATE = true;

	/** Logging level for HTTP requests/responses. */
	public static final Level LOGGING_LEVEL = Level.ALL;

	/**  The ID of the Post object to be updated. */
	public static String POST_ID;
	
	/** The id of the draft post stored in the database. */
	public static String DRAFTPOST_ID = "draftPostID";
	
	/** The title of the draft post stored in the database. */
	public static String DRAFTPOST_TITLE = "draftPostTitle";

	/** The labels of the draft post stored in the database. */
	public static String DRAFTPOST_LABELS = "draftPostLabels";

	/** The content of the draft post stored in the database. */
	public static String DRAFTPOST_CONTENT = "draftPostContent";

	/** The createdAt of the draft post stored in the database. */
	public static String DRAFTPOST_CREATEDAT = "draftPostCreatedAt";

	/** The blogpostid of the draft post stored in the database. */
	public static final String DRAFTPOST_BLOGPOSTID = "draftPostBlogPostId";
	
	public static enum DRAWERLIST
	{
		CREATE(0), BLOGS(1), POSTS(2), DRAFTS(3);
		private int drawerlist;
		private DRAWERLIST(int drawerlist)
		{
			this.drawerlist = drawerlist;
		};
		
		public int getDrawerList()
		{
			return drawerlist;
		}
	}

	/** The singleton. */
	private static Constants singleton;

	/**
	 * Gets the single instance of Constants.
	 *
	 * @return single instance of Constants
	 */
	public static Constants getInstance()
	{
		return singleton;
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();
		ACRA.init(this);
		singleton = this;
	}
}
