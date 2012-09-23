package com.sky.bloggerapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class ViewPost extends Activity
{
	// private final String TAG = "ViewPost";
	private final String MSG_KEY = "value";
	private ProgressDialog viewProgress = null;
	int viewStatus = 0;
	private int attempt = 0;
	private static boolean viewOk = false;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewpost);

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
				Intent i = new Intent(ViewPost.this, LoginActivity.class);
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
