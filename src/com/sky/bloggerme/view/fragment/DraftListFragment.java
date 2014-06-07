package com.sky.bloggerme.view.fragment;

import java.util.List;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.sky.bloggerme.R;
import com.sky.bloggerme.db.DatabaseManager;
import com.sky.bloggerme.model.DraftPost;
import com.sky.bloggerme.util.Communicator;

/**
 * A placeholder fragment containing a simple view.
 */
public class DraftListFragment extends Fragment implements OnItemClickListener, OnItemLongClickListener, MultiChoiceModeListener
{

	private static final String STATE_CHOICE_MODE = "choiceMode";
	private static final String STATE_MODEL = "model";
	private ActionMode activeMode = null;
	ListView draftlist;
	SimpleCursorAdapter adapter;
	List<DraftPost> draftPosts;
	Communicator comm;

	public DraftListFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_draft_list, container, false);
		return rootView;
	}

	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		init(savedInstanceState);

	}

	private void init(Bundle savedInstanceState)
	{
		// If we are not updating a post, we can do a check in the db to check if there's any draft the user has saved, and if there is, get the first draft and populate the fields.
		draftlist = (ListView) getActivity().findViewById(R.id.draftlist);
		draftlist.setOnItemClickListener(this);
		draftlist.setOnItemLongClickListener(this);
		int choiceMode = (savedInstanceState == null ? ListView.CHOICE_MODE_NONE : savedInstanceState.getInt(STATE_CHOICE_MODE));
		draftlist.setChoiceMode(choiceMode);
		draftlist.setMultiChoiceModeListener(this);
		comm = (Communicator) getActivity();
		draftPosts = DatabaseManager.getInstance().getAllDraftPosts();
		String[] columns = new String[] { "_id", "title", "labels", "content", "createdAt", "blogPostId" };
		int[] to = null;
//		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, columns, to, 0);
		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_activated_1, null, columns, to, 0);
		draftlist.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// Toast.makeText(getActivity(), "Selected ID: " + draftPosts.get(position).get_Id(), Toast.LENGTH_LONG).show();

		// TODO:position is not draftPosts' position as users can delete the posts and when they do that, subsequent posts' id will not be in sequence

		Cursor c = (Cursor) adapter.getItem(position);
		DraftPost draft = DraftPost.newInstance(c);
		comm.respond(draft);
	}

	public void loadFinished(Cursor data)
	{
		setAdapter();
		adapter.swapCursor(data);
	}

	public void loadReset()
	{
		adapter.swapCursor(null);
	}

	public void setAdapter()
	{
		Log.d(getClass().getCanonicalName(), "in setAdapter");
		String[] from = new String[1];

		from[0] = "title";
		int[] to = { android.R.id.text1 };

//		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, from, to, 0);
		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_activated_1, null, from, to, 0);
		draftlist.setAdapter(adapter);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		Log.d(getClass().getCanonicalName(), "Item long Clicks");
		draftlist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		draftlist.setItemChecked(position, true);
		return true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu)
	{
		MenuInflater inflater = getActivity().getMenuInflater();

		inflater.inflate(R.menu.draft_context, menu);
		mode.setTitle(R.string.draft_menu_title);
		activeMode = mode;
		updateSubtitle(activeMode);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu)
	{
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item)
	{
		boolean result = performActions(item);
		Log.d(getClass().getSimpleName(), "return from performing action");
		// updateSubtitle(activeMode);
		mode.finish();

		return result;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode)
	{
		if (activeMode != null)
		{
			activeMode = null;
			draftlist.setChoiceMode(ListView.CHOICE_MODE_NONE);
			draftlist.setAdapter(draftlist.getAdapter());
		}

	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
	{
		if (activeMode != null)
		{
			updateSubtitle(mode);
		}

	}

	private void updateSubtitle(ActionMode mode)
	{
		Log.d(getClass().getSimpleName(), "Before updating subtitle");
		mode.setSubtitle("(" + draftlist.getCheckedItemCount() + ")");
	}

	public boolean performActions(MenuItem item)
	{
		// ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
		SparseBooleanArray checked = draftlist.getCheckedItemPositions();

		switch (item.getItemId())
		{
			case R.id.draft_delete:
				boolean success = false;
				// Get the id of all that are selected, pass to databasemanager to delete the respective drafts
				for (int i = 0; i < checked.size(); i++)
				{
					// item position in adapter
					int position = checked.keyAt(i);
					Log.d(getClass().getCanonicalName(), "position: " + position);
					boolean valueat = checked.valueAt(i);
					boolean get = checked.get(i);
					Log.d(getClass().getCanonicalName(), "get: " + get + "valueat: " + valueat);
					if (valueat)
					{
						// DraftPost draft = (DraftPost) draftlist.getItemAtPosition(pos);
						Cursor c = (Cursor) adapter.getItem(position);
						DraftPost draft = DraftPost.newInstance(c);
						success = DatabaseManager.getInstance().removeDraftPost(draft.get_Id());
						if (success)
						{
							comm.restartLoader();
						}

					}
				}

				Toast.makeText(getActivity(), "Draft(s) deleted", Toast.LENGTH_LONG).show();

				return success;

			case R.id.draft_cancel:
				/*
				 * ArrayList<Integer> positions = new ArrayList<Integer>();
				 * 
				 * for (int i = 0; i < checked.size(); i++) { if (checked.valueAt(i)) { positions.add(checked.keyAt(i)); } }
				 */

				/*
				 * Collections.sort(positions, Collections.reverseOrder());
				 * 
				 * for (int position : positions) { adapter.remove(words.get(position)); }
				 */

				draftlist.clearChoices();

				return true;
		}

		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle state)
	{
		super.onSaveInstanceState(state);
		state.putInt(STATE_CHOICE_MODE, draftlist.getChoiceMode());
	}
}
