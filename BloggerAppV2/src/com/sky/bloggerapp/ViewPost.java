package com.sky.bloggerapp;

import java.io.IOException;
import java.util.TimeZone;

import com.google.gdata.data.DateTime;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.HtmlTextConstruct;
import com.google.gdata.util.ServiceException;
import com.sky.bloggerapp.db.DBAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message; //import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class ViewPost extends Activity
{
	// private final String TAG = "ViewPost";
	private final String MSG_KEY = "value";
	private ProgressDialog viewProgress = null;
	int viewStatus = 0;
	public static Entry currentEntry = null;
	public static Feed resultCommentFeed = null;
	private int attempt = 0;
	private DBAdapter mDbHelper;
	private static Cursor setting = null;
	private static boolean viewOk = false;

//	final Handler mHandler = new Handler()
//	{
//		@Override
//		public void handleMessage(Message msg)
//		{
//			Bundle content = msg.getData();
//			String progressId = content.getString(MSG_KEY);
//			if (progressId != null)
//			{
//				if (progressId.equals("1"))
//				{
//					viewProgress.setMessage("Preparing blog config...");
//				}
//				else if (progressId.equals("2"))
//				{
//					viewProgress.setMessage("Authenticating...");
//				}
//				else if (progressId.equals("3"))
//				{
//					viewProgress.setMessage("Receiving post comments...");
//				}
//				else if (progressId.equals("4"))
//				{
//					viewProgress.setMessage("Done...");
//				}
//			}
//		}
//	};
//
//	final Runnable mViewResults = new Runnable()
//	{
//		public void run()
//		{
//			showViewStatus();
//		}
//	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewpost);

//		mDbHelper = new DBAdapter(this);
//		try
//		{
//			mDbHelper.open();
//		}
//		catch (SQLException e)
//		{
//			// Log.e(TAG, "Database has not opened");
//		}
//		setting = mDbHelper.fetchSettindById(1);
//		startManagingCursor(setting);
//
//		currentEntry = ViewPosts.currentEntry;
//		// Log.i(TAG, "CurrentEntry obtained from ViewBlog");
//
//		int maxCharTitle = 30;
//		if (this.getWindow().getWindowManager().getDefaultDisplay().getOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
//		{
//			maxCharTitle = 80;
//		}
//		else if (this.getWindow().getWindowManager().getDefaultDisplay().getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
//		{
//			maxCharTitle = 30;
//		}
//		String nontruncatedTitle = null;
//		String truncatedTitle = null;
//		nontruncatedTitle = currentEntry.getTitle().getPlainText();
//		if (nontruncatedTitle.length() == 0)
//		{
//			truncatedTitle = "<Empty title>";
//		}
//		else if (nontruncatedTitle.length() > maxCharTitle)
//		{
//			truncatedTitle = nontruncatedTitle.substring(0, maxCharTitle) + "...";
//		}
//		else
//		{
//			truncatedTitle = nontruncatedTitle;
//		}
//		TextView postTitle = (TextView) (this.findViewById(R.id.PostTitle));
//		postTitle.setText(truncatedTitle);
//		TextView postAuthor = (TextView) (this.findViewById(R.id.PostAuthor));
//		if (currentEntry.getAuthors().get(0).getName().length() != 0)
//		{
//			postAuthor.setText(currentEntry.getAuthors().get(0).getName());
//		}
//		else
//		{
//			postAuthor.setText("<No author>");
//		}
//
//		DateTime dateTime = null;
//		String dateAndTime = null;
//		String date = null;
//		String time = null;
//		TextView postPublishDate = (TextView) (this.findViewById(R.id.PostPublishDate));
//		dateTime = currentEntry.getPublished();
//		dateTime.setTzShift(TimeZone.getDefault().getRawOffset() / 60000);
//		dateAndTime = dateTime.toString();
//		date = dateAndTime.substring(0, 10);
//		time = dateAndTime.substring(11, 19);
//		postPublishDate.setText(date + " " + time);
//
//		TextView postUpdateDate = (TextView) (this.findViewById(R.id.PostUpdateDate));
//		dateTime = currentEntry.getUpdated();
//		dateTime.setTzShift(TimeZone.getDefault().getRawOffset() / 60000);
//		dateAndTime = dateTime.toString();
//		date = dateAndTime.substring(0, 10);
//		time = dateAndTime.substring(11, 19);
//		postUpdateDate.setText(date + " " + time);
//
//		webview = (WebView) findViewById(R.id.webview);
//		webview.loadDataWithBaseURL(null, ((HtmlTextConstruct) currentEntry.getTextContent().getContent()).getHtml(), "text/html", "UTF-8", "about:blank");
//		WebSettings websettings = webview.getSettings();
//		websettings.setJavaScriptEnabled(true);
//		websettings.setJavaScriptCanOpenWindowsAutomatically(true);
//		webview.setClickable(true);
//		websettings.setLightTouchEnabled(true);
//
//		int w = this.getWindow().getWindowManager().getDefaultDisplay().getWidth() - 12;
//		((Button) this.findViewById(R.id.BackToViewBlog)).setWidth(w / 2);
//		((Button) this.findViewById(R.id.Comments)).setWidth(w / 2);
//
//		if (this.getWindow().getWindowManager().getDefaultDisplay().getOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
//		{
//			((LinearLayout) this.findViewById(R.id.LayoutForHeadline)).setPadding(0, 0, 0, 0);
//			((LinearLayout) this.findViewById(R.id.LayoutForHeadline)).setBackgroundDrawable(null);
//			((LinearLayout) this.findViewById(R.id.LayoutForHeadline)).removeAllViews();
//			((LinearLayout) this.findViewById(R.id.LayoutForWebWiew)).setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 210));
//		}
//		else if (this.getWindow().getWindowManager().getDefaultDisplay().getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
//		{
//			((LinearLayout) this.findViewById(R.id.LayoutForWebWiew)).setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 260));
//		}
//
//		this.findViewById(R.id.LayoutForHeadline).setOnClickListener(new OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				System.out.println("click");
//				LinearLayout layoutForHeadline = (LinearLayout) v.findViewById(R.id.LayoutForHeadline);
//				if (layoutForHeadline.getVisibility() == 1)
//				{
//					layoutForHeadline.setVisibility(0);
//				}
//				else
//				{
//					layoutForHeadline.setVisibility(1);
//				}
//			}
//		});
//
//		this.findViewById(R.id.BackToViewBlog).setOnClickListener(new OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				Intent i = new Intent(ViewPost.this, ViewBlog.class);
//				startActivity(i);
//				finish();
//			}
//		});
//
//		this.findViewById(R.id.Comments).setOnClickListener(new OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				// attempt++;
//				viewPostComments();
//			}
//		});
//	}
//
//	@Override
//	protected void onDestroy()
//	{
//		super.onDestroy();
//		tracker.stop();
//	}
//
//	protected void viewPostComments()
//	{
//		viewProgress = ProgressDialog.show(ViewPost.this, "Viewing post comments", "Starting to view post comments...");
//
//		Thread viewThread = new Thread()
//		{
//			public void run()
//			{
//				Bundle status = new Bundle();
//				mHandler.getLooper();
//				Looper.prepare();
//				Message statusMsg = mHandler.obtainMessage();
//				viewStatus = 0;
//				status.putString(MSG_KEY, "1");
//				statusMsg.setData(status);
//				mHandler.sendMessage(statusMsg);
//				String username = setting.getString(setting.getColumnIndexOrThrow(DBAdapter.KEY_LOGIN));
//				String password = setting.getString(setting.getColumnIndexOrThrow(DBAdapter.KEY_PASSWORD));
//				mDbHelper.close();
//				setting.close();
//				String postID = currentEntry.getId().split("post-")[1];
//				BlogInterface blogapi = null;
//				BlogConfigBLOGGER.BlogInterfaceType typeEnum = BlogConfigBLOGGER.getInterfaceTypeByNumber(1);
//				blogapi = BlogInterfaceFactory.getInstance(typeEnum);
//				// Log.d(TAG, "Using interface type: " + typeEnum);
//				blogapi.setInstanceConfig("");
//				status.putString(MSG_KEY, "2");
//				statusMsg = mHandler.obtainMessage();
//				statusMsg.setData(status);
//				mHandler.sendMessage(statusMsg);
//				String auth_id = null;
//				boolean authFlag = false;
//				attempt = 0;
//				while ((attempt <= MainActivity.AMOUNTOFATTEMPTS) && (!authFlag))
//				{
//					try
//					{
//						auth_id = blogapi.getAuthId(username, password);
//						authFlag = true;
//						attempt = 0;
//					}
//					catch (com.google.gdata.util.AuthenticationException e)
//					{
//						// Log.e(TAG, "AuthenticationException " +
//						// e.getMessage());
//						attempt++;
//					}
//					catch (Exception e)
//					{
//						// Log.e(TAG, "Exception: " + e.getMessage());
//						Alert.showAlert(ViewPost.this, "Network connection failed", "Please, check network settings of your device");
//						finish();
//					}
//				}
//				viewStatus = 1;
//				// Log.d(TAG, "Got auth token:" + auth_id);
//				viewStatus = 2;
//				if (auth_id != null)
//				{
//					status.putString(MSG_KEY, "3");
//					statusMsg = mHandler.obtainMessage();
//					statusMsg.setData(status);
//					mHandler.sendMessage(statusMsg);
//					attempt++;
//					authFlag = false;
//					while ((attempt <= MainActivity.AMOUNTOFATTEMPTS) && (!authFlag))
//					{
//						try
//						{
//							resultCommentFeed = blogapi.getAllPostComments(username, password, postID);
//							// Log.i(TAG,
//							// "Post comments successfully received");
//							viewOk = true;
//							authFlag = true;
//							attempt = 0;
//						}
//						catch (ServiceException e)
//						{
//							e.printStackTrace();
//							attempt++;
//							// Log.e(TAG, "ServiceException " + e.getMessage());
//						}
//						catch (IOException e)
//						{
//							e.printStackTrace();
//							attempt++;
//							// Log.e(TAG, "IOException " + e.getMessage());
//						}
//						catch (Exception e)
//						{
//							// Log.e(TAG, "Exception: " + e.getMessage());
//							Alert.showAlert(ViewPost.this, "Network connection failed", "Please, check network settings of your device");
//							finish();
//						}
//					}
//
//				}
//				else
//				{
//					viewStatus = 3;
//				}
//				status.putString(MSG_KEY, "4");
//				statusMsg = mHandler.obtainMessage();
//				statusMsg.setData(status);
//				mHandler.sendMessage(statusMsg);
//				if (viewOk)
//				{
//					// Log.d(TAG, "Success!");
//					viewStatus = 5;
//				}
//				else
//				{
//					// Log.d(TAG, "Viewing of comments failed!");
//					viewStatus = 4;
//				}
//				mHandler.post(mViewResults);
//
//				if ((resultCommentFeed != null) && (viewOk))
//				{
//					Intent i = new Intent(ViewPost.this, ViewComments.class);
//					startActivity(i);
//					finish();
//				}
//			}
//		};
//		viewThread.start();
//		viewProgress.setMessage("Viewing in progress...");
//	}
//
//	public boolean onKeyDown(int keyCode, KeyEvent event)
//	{
//		if (keyCode == KeyEvent.KEYCODE_BACK)
//		{
//			Intent i = new Intent(ViewPost.this, ViewBlog.class);
//			startActivity(i);
//			finish();
//			return true;
//		}
//		return false;
//	}
//
//	private void showViewStatus()
//	{
//		viewProgress.dismiss();
//		if (attempt > MainActivity.AMOUNTOFATTEMPTS)
//		{
//			Alert.showAlert(this, "Viewing failed", "Error code " + viewStatus, "Try again", new DialogInterface.OnClickListener()
//			{
//				@Override
//				public void onClick(DialogInterface dialog, int which)
//				{
//					dialog.dismiss();
//					viewPostComments();
//				}
//			}, "Cancel", new DialogInterface.OnClickListener()
//			{
//				@Override
//				public void onClick(DialogInterface dialog, int which)
//				{
//					dialog.cancel();
//				}
//			});
//		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.logout:
				Intent i = new Intent(ViewPost.this, Login.class);
				startActivity(i);
				finish();
				break;				
			case R.id.menu_settings:
				Toast.makeText(ViewPost.this, "You pressed the Settings!", Toast.LENGTH_SHORT).show();
				break;
		}
		return true;
	}

}
