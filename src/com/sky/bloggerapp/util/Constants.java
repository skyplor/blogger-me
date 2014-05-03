package com.sky.bloggerapp.util;

import java.util.logging.Level;

import android.app.Application;

import com.google.api.services.blogger.BloggerScopes;

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
	
	/** The ID of the Post object to be updated */
	public static String POST_ID;

	private static Constants singleton;

	public static Constants getInstance()
	{
		return singleton;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		singleton = this;
	}
}
