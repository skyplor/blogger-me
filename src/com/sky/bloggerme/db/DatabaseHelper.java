package com.sky.bloggerme.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.AndroidDatabaseConnection;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;
import com.sky.bloggerme.model.DraftPost;
import com.sky.bloggerme.model.Label;

/**
 * The Class DatabaseHelper.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper
{
	Context context;

	// the DAO object we use to access the SimpleData table
	/** The draft post dao. */
	private Dao<DraftPost, Integer> draftPostDao = null;
	// the DAO object we use to access the SimpleData table
	/** The labelDao dao. */
	private Dao<Label, Integer> labelDao = null;

	/**
	 * Instantiates a new database helper.
	 * 
	 * @param context
	 *            the context
	 */
	public DatabaseHelper(Context context)
	{
		super(context, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase, com.j256.ormlite.support.ConnectionSource)
	 */
	@Override
	public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource)
	{
		try
		{
			TableUtils.createTable(connectionSource, DraftPost.class);
			TableUtils.createTable(connectionSource, Label.class);
		}
		catch (SQLException e)
		{
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, com.j256.ormlite.support.ConnectionSource, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion)
	{
		try
		{
			Log.d(DatabaseHelper.class.getName(), "in onUpgrade");
			// TableUtils.dropTable(connectionSource, DraftPost.class, true);
			// TableUtils.createTable(connectionSource, DraftPost.class);
			// retrieve the labels, find duplicated entries, exclude those, return the non-duplicated entries, drop table, create table, repopulate the table
			List<String> labelnamelist = removeDuplicate();
			TableUtils.dropTable(connectionSource, Label.class, true);
			TableUtils.createTable(connectionSource, Label.class);
			for (String labelname : labelnamelist)
			{
				Label label = new Label();
				label.setName(labelname);
				DatabaseManager.getInstance(this.context).addLabels(label);
			}
		}
		catch (SQLException e)
		{
			Log.e(DatabaseHelper.class.getName(), "exception during onUpgrade", e);
			throw new RuntimeException(e);
		}

	}

	private List<String> removeDuplicate()
	{
		List<String> labelnames = new ArrayList<String>();
		List<Label> labels = DatabaseManager.getInstance(this.context).getAllLabels();
		for (Label label : labels)
		{
			labelnames.add(label.getName());
		}
		Set<String> lhs = new LinkedHashSet<String>();
		lhs.addAll(labelnames);
		labelnames.clear();
		labelnames.addAll(lhs);
		return labelnames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onDowngrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		ConnectionSource cs = getConnectionSource();
		/*
		 * The method is called by Android database helper's get-database calls when Android detects that we need to create or update the database. So we have to use the database argument and save a connection to it on the AndroidConnectionSource, otherwise it will go recursive if the subclass calls getConnectionSource().
		 */
		DatabaseConnection conn = cs.getSpecialConnection();
		boolean clearSpecial = false;
		if (conn == null)
		{
			conn = new AndroidDatabaseConnection(db, true);
			try
			{
				cs.saveSpecialConnection(conn);
				clearSpecial = true;
			}
			catch (SQLException e)
			{
				throw new IllegalStateException("Could not save special connection", e);
			}
		}
		try
		{
			Log.d(DatabaseHelper.class.getName(), "in onDowngrade");
			TableUtils.dropTable(cs, DraftPost.class, true);
			TableUtils.createTable(cs, DraftPost.class);
			TableUtils.dropTable(cs, Label.class, true);
			TableUtils.createTable(cs, Label.class);
		}
		catch (SQLException e)
		{
			Log.e(DatabaseHelper.class.getName(), "exception during onDowngrade", e);
			throw new RuntimeException(e);
		}
		finally
		{
			if (clearSpecial)
			{
				cs.clearSpecialConnection(conn);
			}
		}
	}

	/**
	 * Gets the draft post dao.
	 * 
	 * @return the draft post dao
	 */
	public Dao<DraftPost, Integer> getDraftPostDao()
	{
		if (null == draftPostDao)
		{
			try
			{
				draftPostDao = getDao(DraftPost.class);
			}
			catch (java.sql.SQLException e)
			{
				e.printStackTrace();
			}
		}
		return draftPostDao;
	}

	/**
	 * Gets the labels dao.
	 * 
	 * @return the labels dao
	 */
	public Dao<Label, Integer> getLabelsDao()
	{
		if (null == labelDao)
		{
			try
			{
				labelDao = getDao(Label.class);
			}
			catch (java.sql.SQLException e)
			{
				e.printStackTrace();
			}
		}
		return labelDao;
	}

	/**
	 * drops the table.
	 * 
	 * @param ctx
	 *            the context
	 * @return true, if successful
	 * @throws SQLException
	 */
	public <T> int dropTable(Class<T> dataClass) throws SQLException
	{
		return TableUtils.dropTable(connectionSource, dataClass, false);
	}

	/**
	 * Recreate database.
	 * 
	 * @param <T>
	 * 
	 * @param ctx
	 *            the ctx
	 * @throws SQLException
	 */
	public <T> int recreateTable(Class<T> dataClass) throws SQLException
	{
		return TableUtils.createTable(connectionSource, dataClass);
	}

}
