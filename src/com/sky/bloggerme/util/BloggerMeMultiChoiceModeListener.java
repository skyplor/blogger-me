package com.sky.bloggerme.util;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

import com.sky.bloggerme.R;
import com.sky.bloggerme.view.fragment.DraftListFragment;

public class BloggerMeMultiChoiceModeListener implements MultiChoiceModeListener
{
	DraftListFragment listFragment;
	ActionMode activeMode;
	ListView lv;

	BloggerMeMultiChoiceModeListener(DraftListFragment listFragment, ListView lv)
	{
		this.listFragment = listFragment;
		this.lv = lv;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu)
	{
		MenuInflater inflater = listFragment.getActivity().getMenuInflater();

		inflater.inflate(R.menu.draft_context, menu);
		mode.setTitle(R.string.draft_menu_title);
		mode.setSubtitle("(1)");
		activeMode = mode;

		return (true);
	}

	@Override
	public boolean onPrepareActionMode(ActionMode paramActionMode, Menu paramMenu)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode paramActionMode, MenuItem paramMenuItem)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode paramActionMode)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemCheckedStateChanged(ActionMode paramActionMode, int paramInt, long paramLong, boolean paramBoolean)
	{
		// TODO Auto-generated method stub

	}

}
