package com.sky.bloggerme;

import com.sky.bloggerme.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class ViewBlogs extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewblogs);
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
				Intent i = new Intent(ViewBlogs.this, LoginActivity.class);
				startActivity(i);
				finish();
				break;				
			case R.id.menu_settings:
				Toast.makeText(ViewBlogs.this, "You pressed the Settings!", Toast.LENGTH_SHORT).show();
				break;
		}
		return true;
	}

}
