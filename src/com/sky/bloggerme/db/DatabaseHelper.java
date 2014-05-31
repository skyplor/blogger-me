package com.sky.bloggerme.db;

import java.sql.SQLException;

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

/**
 * The Class DatabaseHelper.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper
{

	// the DAO object we use to access the SimpleData table
	/** The draft post dao. */
	private Dao<DraftPost, Integer> draftPostDao = null;

	/**
	 * Instantiates a new database helper.
	 * 
	 * @param context
	 *            the context
	 */
	public DatabaseHelper(Context context)
	{
		super(context, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
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
			TableUtils.dropTable(connectionSource, DraftPost.class, true);
			TableUtils.createTable(connectionSource, DraftPost.class);
		}
		catch (SQLException e)
		{
			Log.e(DatabaseHelper.class.getName(), "exception during onUpgrade", e);
			throw new RuntimeException(e);
		}

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
	

}
