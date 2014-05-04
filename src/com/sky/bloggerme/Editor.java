package com.sky.bloggerme;

import com.sky.bloggerme.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Editor extends Activity implements OnClickListener
{
	// final static String TAG = "Editor";
	private ProgressDialog publishProgress = null;
	private final String MSG_KEY = "value";
	int viewStatus = 0, publishStatus = 0;
	public static final int AMOUNTOFATTEMPTS = 7;
	private int attempt = 0;
	// private BlogEntry myEntry = null;
	private String title;
	private String content;
	private boolean isDraft = false, loginStatus = false, chosenBlog = false;

	private EditText postContent, postTitle, postTags;
	private Button publishBtn, postsBtn;

	// final Handler mHandler = new Handler()
	// {
	// @Override
	// public void handleMessage(Message msg)
	// {
	// Bundle content = msg.getData();
	// String progressId = content.getString(MSG_KEY);
	// if (progressId != null)
	// {
	// if (progressId.equals("1"))
	// {
	// publishProgress.setMessage("Preparing blog config...");
	// }
	// else if (progressId.equals("2"))
	// {
	// publishProgress.setMessage("Authenticating...");
	// }
	// else if (progressId.equals("3"))
	// {
	// publishProgress.setMessage("Contacting server...");
	// }
	// else if (progressId.equals("4"))
	// {
	// publishProgress.setMessage("Creating new entry...");
	// }
	// else if (progressId.equals("5"))
	// {
	// publishProgress.setMessage("Done...");
	// }
	// }
	// }
	// };

	// final Runnable mPublishResults = new Runnable()
	// {
	// public void run()
	// {
	// showPublishedStatus();
	// }
	// };

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);

		postContent = (EditText) this.findViewById(R.id.post_body);
		postTitle = (EditText) this.findViewById(R.id.post_title);
		postTags = (EditText) this.findViewById(R.id.post_labels);
		publishBtn = (Button) this.findViewById(R.id.publishbutton);
		postsBtn = (Button) this.findViewById(R.id.postsbutton);

		publishBtn.setOnClickListener(this);
		postsBtn.setOnClickListener(this);

		SharedPreferences pref = getSharedPreferences("com.sky.bloggerme", MODE_PRIVATE);
		loginStatus = pref.getBoolean("LoggedIn", false);
		chosenBlog = pref.getBoolean("Blog", false);

		if (!loginStatus || !chosenBlog)
		{
			if (!loginStatus && !chosenBlog)
			{
				Intent intent = new Intent(Editor.this, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			else if (loginStatus && !chosenBlog)
			{
				Intent intent = new Intent(Editor.this, ViewBlogs.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}

		}
		// mDbTextHelper = new DBTextAdapter(this);
		// try
		// {
		// mDbTextHelper.open();
		// }
		// catch (SQLException e)
		// {
		// // Log.e(TAG, "Database has not opened");
		// }
		// post = mDbTextHelper.fetchPostdById(1);
		// startManagingCursor(post);
		// if (post.getCount() != 0)
		// {
		// try
		// {
		// title = postTitle.getText().toString();
		// content = postContent.getText().toString();
		// // Log.i(TAG, "Title of post: " + title + ". Content of post: "+
		// // content);
		// }
		// catch (IllegalArgumentException e)
		// {
		// // Log.e(TAG, "IllegalArgumentException (DataBase failed)");
		// }
		// catch (Exception e)
		// {
		// // Log.e(TAG, "Exception (DataBase failed)");
		// }
		// }
		// mDbTextHelper.close();
		// post.close();

		// this.findViewById(R.id.postbutton).setOnClickListener(new OnClickListener()
		// {
		// public void onClick(View v)
		// {
		// mDbHelper = new DBAdapter(Editor.this);
		// try
		// {
		// mDbHelper.open();
		// }
		// catch (SQLException e)
		// {
		// // Log.e(TAG, "Database has not opened");
		// }
		// setting = mDbHelper.fetchSettindById(1);
		// startManagingCursor(setting);
		// if (setting.getCount() != 0)
		// {
		// if ((setting.getString(setting.getColumnIndexOrThrow(DBAdapter.KEY_LOGIN)).length() == 0) && (setting.getString(setting.getColumnIndexOrThrow(DBAdapter.KEY_PASSWORD)).length() == 0))
		// {
		// mDbHelper.close();
		// setting.close();
		// Alert.showAlert(Editor.this, "Profile is not created", "Please, input 'login/password' in settings");
		// }
		// else
		// {
		// mDbHelper.close();
		// setting.close();
		// viewBlogPosts();
		// }
		// }
		// else
		// {
		// mDbHelper.close();
		// setting.close();
		// Alert.showAlert(Editor.this, "Profile is not created", "Please, input 'login/password' in settings");
		// }
		// Intent i = new Intent(Editor.this, ViewPosts.class);
		// startActivity(i);
		// finish();
		//
		// }
		// });

		// this.findViewById(R.id.publishbutton).setOnClickListener(new OnClickListener()
		// {
		// public void onClick(View v)
		// {

		// Intent i = new Intent(PreviewAndPublish.this, CreateBlogEntry.class);
		// startActivity(i);
		// finish();
		// }

		// });
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
				Intent i = new Intent(Editor.this, LoginActivity.class);
				startActivity(i);
				finish();
				break;
			case R.id.menu_settings:
				Toast.makeText(Editor.this, "You pressed the Settings!", Toast.LENGTH_SHORT).show();
				break;
		}
		return true;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	// private void publishBlogEntry()
	// {
	// // TODO Auto-generated method stub
	// final Activity thread_parent = this;
	// publishProgress = ProgressDialog.show(Editor.this, "Publishing blog entry", "Starting to publish blog entry...");
	// Thread publish = new Thread()
	// {
	// @SuppressWarnings("static-access")
	// public void run()
	// {
	// Bundle status = new Bundle();
	// Looper loop = mHandler.getLooper();
	// loop.prepare();
	// Message statusMsg = mHandler.obtainMessage();
	// publishStatus = 0;
	// status.putString(MSG_KEY, "1");
	// statusMsg.setData(status);
	// mHandler.sendMessage(statusMsg);
	// boolean publishOk = false;
	// BlogConfigBlogger.BlogInterfaceType typeEnum = BlogConfigBlogger.getInterfaceTypeByNumber(1);
	// BlogInterface blogapi = null;
	// blogapi = BlogInterfaceFactory.getInstance(typeEnum);
	// // Log.d(TAG, "Using interface type: " + typeEnum);
	// blogapi.setInstanceConfig("");
	// status.putString(MSG_KEY, "2");
	// statusMsg = mHandler.obtainMessage();
	// statusMsg.setData(status);
	// mHandler.sendMessage(statusMsg);
	// String auth_id = null;
	// boolean authFlag = false;
	// attempt = 0;
	// while ((attempt <= Editor.AMOUNTOFATTEMPTS) && (!authFlag))
	// {
	// try
	// {
	// mDbHelper = new DBAdapter(Editor.this);
	// try
	// {
	// mDbHelper.open();
	// }
	// catch (SQLException e)
	// {
	// // Log.e(TAG, "Database has not opened");
	// }
	// setting = mDbHelper.fetchSettindById(1);
	// startManagingCursor(setting);
	// auth_id = blogapi.getAuthId(setting.getString(setting.getColumnIndexOrThrow(DBAdapter.KEY_LOGIN)), setting.getString(setting.getColumnIndexOrThrow(DBAdapter.KEY_PASSWORD)));
	// mDbHelper.close();
	// setting.close();
	// authFlag = true;
	// attempt = 0;
	// }
	// catch (com.google.gdata.util.AuthenticationException e)
	// {
	// attempt++;
	// // Log.e(TAG, "AuthenticationException " +
	// // e.getMessage());
	// }
	// catch (SQLException e)
	// {
	// // Log.e(TAG, "SQLException: " + e.getMessage());
	// }
	// catch (Exception e)
	// {
	// // Log.e(TAG, "Exception: " + e.getMessage());
	// Alert.showAlert(Editor.this, "Network connection failed", "Please, check network settings of your device");
	// finish();
	// }
	//
	// }
	// publishStatus = 1;
	// // Log.d(TAG, "Got auth token:" + auth_id);
	// publishStatus = 2;
	// if (auth_id != null)
	// {
	// status.putString(MSG_KEY, "3");
	// statusMsg = mHandler.obtainMessage();
	// statusMsg.setData(status);
	// mHandler.sendMessage(statusMsg);
	// String postUri = null;
	// authFlag = false;
	// attempt = 0;
	// while ((attempt <= Editor.AMOUNTOFATTEMPTS) && (!authFlag))
	// {
	// try
	// {
	// postUri = blogapi.getPostUrl();
	// authFlag = true;
	// attempt = 0;
	// }
	// catch (ServiceException e)
	// {
	// // Log.e(TAG, "ServiceException " + e.getMessage());
	// attempt++;
	// }
	// catch (IOException e)
	// {
	// // Log.e(TAG, "IOException " + e.getMessage());
	// attempt++;
	// }
	// catch (Exception e)
	// {
	// // Log.e(TAG, "Exception: " + e.getMessage());
	// Alert.showAlert(thread_parent, "Network connection failed", "Please, check network settings of your device");
	// finish();
	// }
	// }
	// // SpannableBufferHelper helper = new SpannableBufferHelper();
	// // CharSequence cs = myEntry.getBlogEntry();
	// // EditText et = new EditText(Editor.this);
	// // et.setText(cs);
	// // Spannable spa = et.getText();
	// // spa.setSpan(cs, 0, 1, 1);
	// // String entry = helper.SpannableToXHTML(spa);
	// status.putString(MSG_KEY, "4");
	// statusMsg = mHandler.obtainMessage();
	// statusMsg.setData(status);
	// mHandler.sendMessage(statusMsg);
	// authFlag = false;
	// attempt = 0;
	// while ((attempt <= Editor.AMOUNTOFATTEMPTS) && (!authFlag))
	// {
	// try
	// {
	// mDbHelper = new DBAdapter(Editor.this);
	// try
	// {
	// mDbHelper.open();
	// }
	// catch (SQLException e)
	// {
	// // Log.e(TAG, "Database has not opened");
	// }
	// setting = mDbHelper.fetchSettindById(1);
	// startManagingCursor(setting);
	// publishOk = blogapi.createPost(thread_parent, auth_id, postUri, null, title, null, content, setting.getString(setting.getColumnIndexOrThrow(DBAdapter.KEY_LOGIN)), setting.getString(setting.getColumnIndexOrThrow(DBAdapter.KEY_PASSWORD)), isDraft);
	// mDbHelper.close();
	// setting.close();
	// authFlag = true;
	// attempt = 0;
	// }
	// catch (ServiceException e)
	// {
	// // Log.e(TAG, "ServiceException: " +
	// // e.getMessage());
	// attempt++;
	// }
	// catch (SQLException e)
	// {
	// // Log.e(TAG, "SQLException: " + e.getMessage());
	// }
	// catch (Exception e)
	// {
	// // Log.e(TAG, "Exception: " + e.getMessage());
	// Alert.showAlert(Editor.this, "Network connection failed", "Please, check network settings of your device");
	// mDbHelper.close();
	// setting.close();
	// finish();
	// }
	// }
	// }
	// else
	// {
	// publishStatus = 3;
	// mDbHelper.close();
	// setting.close();
	// }
	// status.putString(MSG_KEY, "5");
	// statusMsg = mHandler.obtainMessage();
	// statusMsg.setData(status);
	// mHandler.sendMessage(statusMsg);
	// if (publishOk)
	// {
	// // Log.d(TAG, "Post published successfully!");
	// publishStatus = 5;
	// }
	// else
	// {
	// // Log.d(TAG, "Publishing of the post failed!");
	// publishStatus = 4;
	// }
	// mHandler.post(mPublishResults);
	// }
	// };
	// publish.start();
	// publishProgress.setMessage("Publishing in progress...");
	// }
	//
	// private void showPublishedStatus()
	// {
	// publishProgress.dismiss();
	// if (publishStatus == 5)
	// {
	// try
	// {
	// mDbTextHelper = new DBTextAdapter(this);
	// try
	// {
	// mDbTextHelper.open();
	// }
	// catch (SQLException e)
	// {
	// // Log.e(TAG, "Database has not opened");
	// }
	// post = mDbTextHelper.fetchPostdById(1);
	// startManagingCursor(post);
	// mDbTextHelper.updatePostById((long) 1, "", "");
	// mDbTextHelper.close();
	// post.close();
	// }
	// catch (SQLException e)
	// {
	// // Log.e(TAG, "SQLException: " + e.getMessage());
	// }
	// catch (Exception e)
	// {
	// // Log.e(TAG, "Exception: " + e.getMessage());
	// }
	// final Dialog dlg = new AlertDialog.Builder(Editor.this).setIcon(com.sky.bloggerme.R.drawable.ic_dialog_alert).setTitle("Publish status").setPositiveButton("OK", null).setMessage("Published").create();
	// // dlg.setOnDismissListener(new OnDismissListener()
	// // {
	// // @Override
	// // public void onDismiss(DialogInterface dialog)
	// // {
	// // Intent i = new Intent(Editor.this, MainActivity.class);
	// // startActivity(i);
	// // finish();
	// // }
	// // });
	// dlg.show();
	// }
	// else
	// {
	// mDbTextHelper.close();
	// post.close();
	// attempt = 0;
	// Alert.showAlert(this, "Publishing failed", "Error code " + publishStatus, "Try again", new DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// {
	// dialog.dismiss();
	// publishBlogEntry();
	// }
	// }, "Cancel", new DialogInterface.OnClickListener()
	// {
	// @Override
	// public void onClick(DialogInterface dialog, int which)
	// {
	// dialog.cancel();
	// }
	// });
	// }
	// }

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		if (v == publishBtn)
		{
			title = postTitle.getText().toString();
			content = postContent.getText().toString();

			// this.publishBlogEntry();
		}

		if (v == postsBtn)
		{
			Intent i = new Intent(Editor.this, ViewPosts.class);
			startActivity(i);
			finish();
		}
	}
}