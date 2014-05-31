package com.sky.bloggerme.view;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sky.bloggerme.R;
import com.sky.bloggerme.db.Contract;
import com.sky.bloggerme.db.DatabaseManager;
import com.sky.bloggerme.model.DraftPost;
import com.sky.bloggerme.util.Alert;
import com.sky.bloggerme.util.Communicator;
import com.sky.bloggerme.util.Constants;
import com.sky.bloggerme.view.fragment.DraftListFragment;

public class DraftListActivity extends Activity implements LoaderCallbacks<Cursor>, Communicator
{

	private static final String TAG = DraftListActivity.class.getCanonicalName();
	// SimpleCursorAdapter adapter;
	DraftListFragment draftListFragment;

	/**
	 * Drawer variables
	 */
	private String[] drawerPages;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draft_list);

		if (savedInstanceState == null)
		{
			Log.d(TAG, "before getting Fragment Manager");
			draftListFragment = new DraftListFragment();
			getFragmentManager().beginTransaction().add(R.id.container, draftListFragment).commit();
			initDrawer();
			// draftListFragment = (DraftListFragment) findFragmentById(R.id.draftfragment);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.draft_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		switch(id)
		{
			case R.id.action_clearall:
				Alert.showAlert(this, "Remove All Drafts", "Remove All Drafts?", "YES", new OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt)
					{
						DatabaseManager.getInstance().clearDatabase(getApplicationContext());
					}
				}, "NO", new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt)
					{
						paramDialogInterface.dismiss();
					}
					
				});
				break;
			case R.id.action_settings:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1)
	{
		// String[] projection = { "id", "title", "labels", "content" };
		String[] projection = null;
		CursorLoader cursorLoader = new CursorLoader(this, Contract.DraftPost.contentUri, projection, null, null, null);
		Cursor c = getContentResolver().query(Contract.DraftPost.contentUri, null, null, null, null);
		while (c.moveToNext())
		{
			for (int i = 0; i < c.getColumnCount(); i++)
			{
				Log.d(getClass().getSimpleName(), c.getColumnName(i) + " : " + c.getString(i));
			}
		}
		c.close();
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		if (draftListFragment != null)
		{
			Log.d(getClass().getSimpleName(), "load finished");
			draftListFragment.loadFinished(data);
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0)
	{
		if (draftListFragment != null)
		{
			draftListFragment.loadReset();
		}

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// Starts a new or restarts an existing Loader in this manager
		getLoaderManager().restartLoader(0, null, DraftListActivity.this);
	}

	// send extra back to editor with the key draft and the contents etc.
	@Override
	public void respond(DraftPost dPost)
	{
		Intent intent = getIntent();
		intent.putExtra(Constants.DRAFTPOST_ID, dPost.get_Id());
		intent.putExtra(Constants.DRAFTPOST_TITLE, dPost.getTitle());
		intent.putExtra(Constants.DRAFTPOST_LABELS, dPost.getLabels());
		intent.putExtra(Constants.DRAFTPOST_CONTENT, dPost.getContent());
		intent.putExtra(Constants.DRAFTPOST_CREATEDAT, dPost.getCreatedAt());
		if (!dPost.getBlogPostId().isEmpty())
		{
			intent.putExtra(Constants.DRAFTPOST_BLOGPOSTID, dPost.getBlogPostId());
		}
		setResult(Activity.RESULT_OK, intent);
		this.finish();

	}

	/**
	 * initialise the left drawer
	 */
	public void initDrawer()
	{
		mTitle = mDrawerTitle = getTitle();
		drawerPages = getResources().getStringArray(R.array.drawer_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.drafts_drawer);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerPages));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		)
		{
			public void onDrawerClosed(View view)
			{
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView)
			{
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		selectItem(Constants.DRAWERLIST.DRAFTS.getDrawerList());

	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			selectItem(position);
		}
	}

	/** Go to another activity */
	private void selectItem(int position)
	{
		// TODO: Change to the relevant activity on select
		switch (position)
		{
			case 0:
				// Editor Activity
				Intent editorIntent = new Intent(DraftListActivity.this, EditorActivity.class);
				startActivity(editorIntent);
				finish();
				break;
			case 1:
				// Blogs Activity
				Intent blogIntent = new Intent(DraftListActivity.this, BlogListActivity.class);
				blogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(blogIntent);
				finish();
				break;
			case 2:
				// Posts Activity
				Intent postIntent = new Intent(DraftListActivity.this, PostListActivity.class);
				startActivity(postIntent);
				finish();
				break;
			case 3:
				// Drafts Activity
				// goToDrafts();
				break;
			default:
				break;
		}
		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(drawerPages[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title)
	{
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
}
