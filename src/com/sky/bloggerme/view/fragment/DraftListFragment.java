package com.sky.bloggerme.view.fragment;

import java.util.List;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.sky.bloggerme.R;
import com.sky.bloggerme.db.DatabaseManager;
import com.sky.bloggerme.model.DraftPost;
import com.sky.bloggerme.util.Communicator;

/**
 * A placeholder fragment containing a simple view.
 */
public class DraftListFragment extends Fragment implements OnItemClickListener
{

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
		init();

	}

	private void init()
	{
		// If we are not updating a post, we can do a check in the db to check if there's any draft the user has saved, and if there is, get the first draft and populate the fields.
		draftlist = (ListView) getActivity().findViewById(R.id.draftlist);
		draftlist.setOnItemClickListener(this);
		comm = (Communicator) getActivity();
		draftPosts = DatabaseManager.getInstance().getAllDraftPosts();
		// List<String> titles = new ArrayList<String>();
		// List<String> labels = new ArrayList<String>();
		// List<String> contents = new ArrayList<String>();
		String[] columns = new String[] { "_id", "title" };
		int[] to = null;
		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, columns, to, 0);
		draftlist.setAdapter(adapter);
		/** Creating a loader for populating listview from sqlite database */
		/** This statement, invokes the method onCreatedLoader() */
		// getLoaderManager().initLoader(0, null, this);
		// if (!draftPosts.isEmpty())
		// {
		// // int lastEntry = draftPosts.size() - 1;
		// for (DraftPost dpost : draftPosts)
		// {
		// titles.add(dpost.getTitle());
		// labels.add(dpost.getLabels());
		// contents.add(dpost.getContent());
		// }
		// if (!titles.isEmpty())
		// {
		// postTitle.setText(titles.get(lastEntry));
		// }
		// if (!labels.isEmpty())
		// {
		// labelsMultiAutoComplete.setText(labels.get(lastEntry));
		// }
		// if (!contents.isEmpty())
		// {
		// postContent.setTextHTML(contents.get(lastEntry));
		// }
		// }

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
//		Toast.makeText(getActivity(), "Selected ID: " + draftPosts.get(position).get_Id(), Toast.LENGTH_LONG).show();
		
		comm.respond(draftPosts.get(position));
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

		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, from, to, 0);
		draftlist.setAdapter(adapter);
	}
}
