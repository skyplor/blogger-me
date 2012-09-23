package com.sky.bloggerapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.sky.bloggerapp.util.Alert;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class ViewPosts extends ListActivity
{
	private ProgressDialog viewProgress = null;
	private final String MSG_KEY = "value";
	private static Cursor setting = null;
	int viewStatus = 0;
	private int attempt = 0;
	private Button blogsBtn, createBtn;

	final Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			Bundle content = msg.getData();
			String progressId = content.getString(MSG_KEY);
			if (progressId != null)
			{
				if (progressId.equals("1"))
				{
					viewProgress.setMessage("Preparing blog config...");
				}
				else if (progressId.equals("2"))
				{
					viewProgress.setMessage("Authenticating...");
				}
				else if (progressId.equals("3"))
				{
					viewProgress.setMessage("Receiving blog entries...");
				}
				else if (progressId.equals("4"))
				{
					viewProgress.setMessage("Done...");
				}
			}
		}
	};

	final Runnable mViewResults = new Runnable()
	{
		public void run()
		{
			showViewStatus();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewposts);

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
//		resultFeed = Editor.resultFeed;
		// Log.i(TAG, "ResultFeed obtained from MainActivity");

//		int w = this.getWindow().getWindowManager().getDefaultDisplay().getWidth() - 12;
		blogsBtn = (Button) this.findViewById(R.id.blogsbutton);
		createBtn = (Button) this.findViewById(R.id.createbutton);

		TextView blogTitle = (TextView) ViewPosts.this.findViewById(R.id.blogTitle);
//		blogTitle.setText(resultFeed.getTitle().getPlainText());
//
//		List<Map<String, Object>> resourceNames = new ArrayList<Map<String, Object>>();
//		Map<String, Object> data;
//		int maxCharTitle = 22;
//		Entry entry = null;
//		String nontruncatedTitle = null;
//		String truncatedTitle = null;
//		DateTime dateTime = null;
//		String dateAndTime = null;
//		String date = null;
//		String time = null;
//		for (int j = 0; j < resultFeed.getEntries().size(); j++)
//		{
//			data = new HashMap<String, Object>();
//			entry = resultFeed.getEntries().get(j);
//			try
//			{
//				nontruncatedTitle = entry.getTitle().getPlainText();
//				if (nontruncatedTitle.length() == 0)
//				{
//					truncatedTitle = "<Empty title>";
//				}
//				else if (nontruncatedTitle.length() > maxCharTitle)
//				{
//					truncatedTitle = nontruncatedTitle.substring(0, maxCharTitle) + "...";
//				}
//				else
//				{
//					truncatedTitle = nontruncatedTitle;
//				}
//				data.put("line1", truncatedTitle);
//				dateTime = entry.getUpdated();
//				dateTime.setTzShift(TimeZone.getDefault().getRawOffset() / 60000);
//				dateAndTime = dateTime.toString();
//				date = dateAndTime.substring(0, 10);
//				time = dateAndTime.substring(11, 19);
//				data.put("line3", date + " " + time);
//				resourceNames.add(data);
//			}
//			catch (Resources.NotFoundException nfe)
//			{
//				// Log.e(TAG, "NotFoundException " + nfe.getMessage());
//			}
//		}
//
//		SimpleAdapter notes = new SimpleAdapter(this, resourceNames, R.layout.row, new String[] { "line1", "line3" }, new int[] { R.id.text1, R.id.text3 });
//		setListAdapter(notes);

		blogsBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(ViewPosts.this, ViewBlogs.class);
				startActivity(i);
				finish();
			}
		});
//		this.findViewById(R.id.list).setOnClickListener(new OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				viewBlogPosts();
//			}
//
//		});
		
		createBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(ViewPosts.this, Editor.class);
				startActivity(i);
				finish();
			}

		});
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
				Intent i = new Intent(ViewPosts.this, LoginActivity.class);
				startActivity(i);
				finish();
				break;				
			case R.id.menu_settings:
				Toast.makeText(ViewPosts.this, "You pressed the Settings!", Toast.LENGTH_SHORT).show();
				break;
		}
		return true;
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	private void showViewStatus()
	{
		viewProgress.dismiss();
		if (viewStatus != 5)
		{
			Alert.showAlert(this, "Viewing failed", "Error code " + viewStatus, "Try again", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					viewBlogPosts();
				}
			}, "Cancel", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.cancel();
				}
			});
		}
	}

	protected void viewBlogPosts()
	{
		viewProgress = ProgressDialog.show(ViewPosts.this, "Viewing blog entries", "Starting to view blog entries...");

		Thread viewThread = new Thread()
		{
			public void run()
			{
				Bundle status = new Bundle();
				mHandler.getLooper();
				Looper.prepare();
				Message statusMsg = mHandler.obtainMessage();
				viewStatus = 0;
				status.putString(MSG_KEY, "1");
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				boolean viewOk = false;
				
				CharSequence postconfig = "";
				status.putString(MSG_KEY, "2");
				statusMsg = mHandler.obtainMessage();
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				String auth_id = null;
				boolean authFlag = false;
				attempt = 0;
				
				viewStatus = 1;
				// Log.d(TAG, "Got auth token:" + auth_id);
				viewStatus = 2;
				if (auth_id != null)
				{
					status.putString(MSG_KEY, "3");
					statusMsg = mHandler.obtainMessage();
					statusMsg.setData(status);
					mHandler.sendMessage(statusMsg);
					authFlag = false;
					attempt = 0;
					
				}
				else
				{
					viewStatus = 3;
				}
				status.putString(MSG_KEY, "4");
				statusMsg = mHandler.obtainMessage();
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				if (viewOk)
				{
					// Log.d(TAG, "Success!");
					viewStatus = 5;
				}
				else
				{
					// Log.d(TAG, "Viewing of the blog failed!");
					viewStatus = 4;
				}
				mHandler.post(mViewResults);

				}
		};
		viewThread.start();
		viewProgress.setMessage("Viewing in progress...");
	}

//	public boolean onKeyDown(int keyCode, KeyEvent event)
//	{
//		if (keyCode == KeyEvent.KEYCODE_BACK)
//		{
//			Intent i = new Intent(ViewPosts.this, Editor.class);
//			startActivity(i);
//			finish();
//			return true;
//		}
//		return false;
//	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
//		currentEntry = resultFeed.getEntries().get((int) id);
		Intent i = new Intent(ViewPosts.this, ViewPost.class);
		startActivity(i);
		finish();
	}
}
